package com.example.quizmeup.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.quizmeup.ai.LlmClient;
import com.example.quizmeup.ai.PromptService;
import com.example.quizmeup.common.AiResponse;
import com.example.quizmeup.dto.SubmitAnswerResponse;
import com.example.quizmeup.entity.Knowledge;
import com.example.quizmeup.entity.Question;
import com.example.quizmeup.entity.QuestionRecord;
import com.example.quizmeup.entity.UserMastery;
import com.example.quizmeup.mapper.KnowledgeMapper;
import com.example.quizmeup.mapper.QuestionMapper;
import com.example.quizmeup.mapper.QuestionRecordMapper;
import com.example.quizmeup.mapper.UserMasteryMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

/**
 * 学习服务类
 */
@Service
public class LearningService {

    @Autowired
    private KnowledgeMapper knowledgeMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private PromptService promptService;

    @Autowired
    private LlmClient llmClient;

    @Autowired
    private QuestionRecordMapper questionRecordMapper;

    @Autowired
    private UserMasteryMapper userMasteryMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 开始学习：根据知识点ID获取或生成题目
     *
     * @param userId      用户ID
     * @param knowledgeId 知识点ID
     * @return 题目信息
     * @throws IllegalArgumentException 如果知识点不存在或不是叶节点
     */
    @Transactional
    public List<Question> startLearning(Long userId, String knowledgeId) {
        // 1. 校验知识点是否存在且为叶节点
        Knowledge knowledge = knowledgeMapper.selectById(knowledgeId);
        if (knowledge == null) {
            throw new IllegalArgumentException("知识点不存在");
        }
        if (!Boolean.TRUE.equals(knowledge.getIsLeaf())) {
            throw new IllegalArgumentException("该知识点不是叶节点，无法生成题目");
        }

        // 2. 查询该知识点下的题目
        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getKnowledgeId, knowledgeId)
                        .orderByDesc(Question::getCreatedAt)
        );

        // 3. 如果没有题目，则生成新题目
        if (questions.isEmpty()) {
            questions = generateQuestion(knowledge);
            questionMapper.insert(questions);
            return questions;
        }

        return questions;
    }

    /**
     * 生成新题目
     *
     * @param knowledge 知识点信息
     * @return List<Question>
     */
    private List<Question> generateQuestion(Knowledge knowledge) {
        Map<String, Object> params = new HashMap<>();
        params.put("topic", knowledge.getName());
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
            question.setId(knowledge.getId()+"_Q"+index);
            question.setKnowledgeId(knowledge.getId());
            question.setCreatedAt(LocalDateTime.now());
            question.setUpdatedAt(LocalDateTime.now());

            // 设置默认值
            if (question.getDifficulty() == null) {
                question.setDifficulty(3);
            }
            if (question.getImportance() == null) {
                question.setImportance(3);
            }
            index++;
        }

        return questions;
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
        Question question = questionMapper.selectById(questionId);
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
        questionRecordMapper.insert(record);

        // 6. 更新 lc_user_mastery.proficiency = 该知识点下所有题目的平均分
        updateUserMastery(userId, question.getKnowledgeId());

        return response;
    }



    /**
     * 更新用户掌握度
     * proficiency = 该知识点下所有题目的平均分（考虑所有历史答题记录）
     *
     * @param userId      用户ID
     * @param knowledgeId 知识点ID
     */
    private void updateUserMastery(Long userId, String knowledgeId) {
        // 查询该知识点下所有题目的最新答题记录
        List<QuestionRecord> records = questionRecordMapper.selectByUserIdAndKnowledgeId(userId, knowledgeId);
        List<QuestionRecord> latestRecords = records.stream()
                .collect(Collectors.toMap(
                        QuestionRecord::getQuestionId,           // key：questionId
                        record -> record,                        // value：当前记录
                        (QuestionRecord existing, QuestionRecord replacement) ->               // 合并函数：保留 submittedAt 更晚的
                                existing.getSubmittedAt() == null ? replacement :
                                        replacement.getSubmittedAt() == null ? existing :
                                                existing.getSubmittedAt().compareTo(replacement.getSubmittedAt()) >= 0 ? existing : replacement,
                        LinkedHashMap::new                       // 保持插入顺序（可选）
                ))
                .values()
                .stream()
                .collect(Collectors.toList());

        if (records.isEmpty()) {
            // 没有答题记录，不更新掌握度
            return;
        }


        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getKnowledgeId, knowledgeId)
                        .orderByDesc(Question::getCreatedAt)
        );
        int questionCount = questions.size();



        // 计算平均分（除以整体题量）
        double sumScore = records.stream()
                .mapToDouble(record -> record.getScore() != null ? record.getScore().doubleValue() : 0.0)
                .sum();

        double avgScore = sumScore/questionCount;

        BigDecimal proficiency = BigDecimal.valueOf(avgScore)
                .setScale(2, RoundingMode.HALF_UP);

        // 查询或创建用户掌握度记录
        UserMastery mastery = userMasteryMapper.selectByUserIdAndKnowledgeId(userId, knowledgeId);
        if (mastery == null) {
            mastery = new UserMastery();
            mastery.setUserId(userId);
            mastery.setKnowledgeId(knowledgeId);
            mastery.setProficiency(proficiency);
            mastery.setUpdatedAt(LocalDateTime.now());
            userMasteryMapper.insert(mastery);
        } else {
            mastery.setProficiency(proficiency);
            mastery.setUpdatedAt(LocalDateTime.now());
            userMasteryMapper.updateProficiency(mastery);
        }
    }
}
