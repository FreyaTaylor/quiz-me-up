package com.example.quizmeup.service;

import com.example.quizmeup.ai.LlmClient;
import com.example.quizmeup.ai.PromptService;
import com.example.quizmeup.common.AiResponse;
import com.example.quizmeup.common.Constants;
import com.example.quizmeup.dto.QuestionWithScore;
import com.example.quizmeup.dto.SubmitAnswerResponse;
import com.example.quizmeup.entity.Knowledge;
import com.example.quizmeup.entity.Question;
import com.example.quizmeup.entity.QuestionRecord;
import com.example.quizmeup.entity.UserMastery;
import com.example.quizmeup.repository.KnowledgeRepository;
import com.example.quizmeup.repository.QuestionRecordRepository;
import com.example.quizmeup.repository.QuestionRepository;
import com.example.quizmeup.repository.UserMasteryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学习服务类
 */
@Service
@RequiredArgsConstructor
public class LearningService {

    private final KnowledgeRepository knowledgeRepository;
    private final QuestionRepository questionRepository;
    private final QuestionRecordRepository questionRecordRepository;
    private final UserMasteryRepository userMasteryRepository;
    private final PromptService promptService;
    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    /**
     * 开始学习：根据知识点ID获取或生成题目
     *
     * @param userId      用户ID
     * @param knowledgeId 知识点ID
     * @return 题目信息（包含最近一次得分）
     * @throws IllegalArgumentException 如果知识点不存在或不是叶节点
     */
    @Transactional
    public List<QuestionWithScore> startLearning(Long userId, String knowledgeId) {
        // 1. 校验知识点是否存在且为叶节点
        Knowledge knowledge = knowledgeRepository.findById(knowledgeId);
        if (knowledge == null) {
            throw new IllegalArgumentException("知识点不存在");
        }
        if (!Boolean.TRUE.equals(knowledge.getIsLeaf())) {
            throw new IllegalArgumentException("该知识点不是叶节点，无法生成题目");
        }

        // 2. 查询该知识点下的题目
        List<Question> questions = questionRepository.findByKnowledgeId(knowledgeId);

        // 3. 如果没有题目，则生成新题目
        if (questions.isEmpty()) {
            questions = generateQuestion(knowledge);
            questionRepository.saveBatch(questions);
        }

        // 4. 查询每个题目的最近一次得分
        List<String> questionIds = new ArrayList<>();
        for (Question question : questions) {
            questionIds.add(question.getId());
        }

        List<QuestionRecord> latestRecords = questionRecordRepository.findLatestScoresByUserIdAndQuestionIds(userId, questionIds);
        
        // 构建题目ID到得分的映射
        Map<String, BigDecimal> scoreMap = new HashMap<>();
        for (QuestionRecord record : latestRecords) {
            if (record.getQuestionId() != null) {
                BigDecimal score = record.getScore() != null ? record.getScore() : BigDecimal.ZERO;
                // 如果已存在，保留第一个（更早的记录）
                scoreMap.putIfAbsent(record.getQuestionId(), score);
            }
        }

        // 5. 构建返回结果
        List<QuestionWithScore> result = new ArrayList<>();
        for (Question question : questions) {
            BigDecimal lastScore = scoreMap.getOrDefault(question.getId(), BigDecimal.ZERO);
            result.add(new QuestionWithScore(question, lastScore));
        }

        return result;
    }

    /**
     * 生成新题目
     *
     * @param knowledge 知识点信息
     * @return List<Question>
     */
    private List<Question> generateQuestion(Knowledge knowledge) {
        Map<String, Object> params = new HashMap<>();
        // 获取知识点的完整路径（包含所有父节点），用 "-" 拼接
        String topic = buildKnowledgePath(knowledge);
        params.put("topic", topic);
        String prompt = promptService.render("INTERVIEW_Q_GEN", params);

        String llmResponse = llmClient.call(prompt);

        List<Question> questions = new ArrayList<>();

        try {
            AiResponse<List<Question>> resp = objectMapper.readValue(llmResponse, new TypeReference<>() {});
            questions = resp.getData();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        int index = 1;
        for (Question question : questions) {
            // 设置题目基本信息
            question.setId(knowledge.getId() + Constants.QUESTION_ID_SEPARATOR + index);
            question.setKnowledgeId(knowledge.getId());
            question.setCreatedAt(LocalDateTime.now());
            question.setUpdatedAt(LocalDateTime.now());

            // 设置默认值
            if (question.getDifficulty() == null) {
                question.setDifficulty(Constants.DEFAULT_DIFFICULTY);
            }
            if (question.getImportance() == null) {
                question.setImportance(Constants.DEFAULT_IMPORTANCE);
            }
            index++;
        }

        return questions;
    }

    /**
     * 构建知识点的完整路径（包含所有父节点），用 "-" 拼接
     * 例如：Java - Java 并发 - 线程池
     *
     * @param knowledge 知识点
     * @return 完整路径字符串
     */
    private String buildKnowledgePath(Knowledge knowledge) {
        List<String> pathNames = new ArrayList<>();
        Knowledge current = knowledge;
        
        // 从当前节点开始，向上遍历所有父节点
        while (current != null) {
            pathNames.add(0, current.getName()); // 插入到列表开头，保持从根到叶的顺序
            if (current.getParentId() == null || current.getParentId().isEmpty()) {
                break;
            }
            current = knowledgeRepository.findById(current.getParentId());
        }
        
        // 用 "-" 拼接所有节点名称
        return String.join(" - ", pathNames);
    }

    /**
     * 提交答案并评分
     *
     * @param userId     用户ID
     * @param questionId 题目ID
     * @param answerText 用户答案文本
     * @return 评分结果
     * @throws IllegalArgumentException 如果题目不存在
     */
    @Transactional
    public SubmitAnswerResponse submitAnswer(Long userId, String questionId, String answerText) throws JsonProcessingException {
        // 1. 从 lc_questions 获取题目和标准答案
        Question question = questionRepository.findById(questionId);
        if (question == null) {
            throw new IllegalArgumentException("题目不存在");
        }

        // 2. 调用 PromptService.render() 构造评分 Prompt（禁止硬编码）
        Map<String, Object> params = new HashMap<>();
        params.put("question", question.getQuestionText());
        params.put("answer", answerText);

        String prompt = promptService.render("ANSWER_EVALUATION", params);

        // 3. 用 LlmClient.call() 获取 LLM 评价
        String llmResponse = llmClient.call(prompt);

        // 4. 解析 LLM 输出，提取四部分
        AiResponse<SubmitAnswerResponse> resp = objectMapper.readValue(llmResponse, new TypeReference<>() {});
        SubmitAnswerResponse response = resp.getData();

        // 5. 保存记录到 lc_question_record
        QuestionRecord record = new QuestionRecord();
        record.setUserId(userId);
        record.setQuestionId(questionId);
        record.setScore(BigDecimal.valueOf(response.getScore()).setScale(1, RoundingMode.HALF_UP));
        record.setSubmittedAt(LocalDateTime.now());
        questionRecordRepository.save(record);

        // 6. 更新 lc_user_mastery.proficiency = 该知识点下所有题目的平均分
        updateUserMastery(userId, question.getKnowledgeId());

        return response;
    }



    /**
     * 更新用户掌握度
     * proficiency = 该知识点下所有题目的平均分（考虑所有历史答题记录）
     * 
     * 使用悲观锁（SELECT FOR UPDATE）解决并发更新问题：
     * 1. 锁定掌握度记录，防止其他事务同时更新
     * 2. 重新查询所有答题记录（确保获取最新数据）
     * 3. 计算平均分并更新
     * 
     * 这种方法可以避免"丢失更新"（Lost Update）问题：
     * - 问题：两个请求同时提交答案时，可能都基于旧的答题记录计算平均分
     * - 解决：使用数据库锁确保同一时间只有一个事务能更新掌握度
     *
     * @param userId      用户ID
     * @param knowledgeId 知识点ID
     */
    @Transactional
    private void updateUserMastery(Long userId, String knowledgeId) {
        // 步骤1：使用 SELECT FOR UPDATE 锁定掌握度记录（如果存在）
        // 这样可以防止其他并发事务同时更新同一条记录
        // 注意：即使记录不存在，FOR UPDATE 也会在插入时生效（通过后续的 INSERT ... ON DUPLICATE KEY UPDATE）
        userMasteryRepository.findByUserIdAndKnowledgeIdForUpdate(userId, knowledgeId);
        
        // 步骤2：在锁定期间，重新查询该知识点下所有题目的最新答题记录
        // 这样可以确保获取到所有已提交的答题记录（包括当前事务刚插入的记录）
        List<QuestionRecord> records = questionRecordRepository.findLatestScoresByUserIdAndQuestionIds(userId, List.of(knowledgeId));

        if (records.isEmpty()) {
            // 没有答题记录，不更新掌握度
            return;
        }

        // 步骤3：查询该知识点下的所有题目数量
        List<Question> questions = questionRepository.findByKnowledgeId(knowledgeId);
        int questionCount = questions.size();

        if (questionCount == 0) {
            // 没有题目，不更新掌握度
            return;
        }

        // 步骤4：计算平均分（除以整体题量）
        double sumScore = records.stream()
                .mapToDouble(record -> record.getScore() != null ? record.getScore().doubleValue() : 0.0)
                .sum();

        double avgScore = sumScore / questionCount;

        BigDecimal proficiency = BigDecimal.valueOf(avgScore)
                .setScale(2, RoundingMode.HALF_UP);

        // 步骤5：使用 INSERT ... ON DUPLICATE KEY UPDATE 原子性地插入或更新
        // 由于已经加锁，这里可以安全地更新
        UserMastery mastery = new UserMastery();
        mastery.setUserId(userId);
        mastery.setKnowledgeId(knowledgeId);
        mastery.setProficiency(proficiency);
        mastery.setUpdatedAt(LocalDateTime.now());
        userMasteryRepository.save(mastery);
    }
}
