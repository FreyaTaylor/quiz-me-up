package com.example.quizmeup.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.quizmeup.ai.LlmClient;
import com.example.quizmeup.ai.PromptService;
import com.example.quizmeup.domain.entity.Knowledge;
import com.example.quizmeup.domain.entity.Question;
import com.example.quizmeup.domain.entity.QuestionRecord;
import com.example.quizmeup.domain.entity.UserMastery;
import com.example.quizmeup.infra.mapper.KnowledgeMapper;
import com.example.quizmeup.infra.mapper.QuestionMapper;
import com.example.quizmeup.infra.mapper.QuestionRecordMapper;
import com.example.quizmeup.infra.mapper.UserMasteryMapper;
import com.example.quizmeup.service.dto.AnswerRequest;
import com.example.quizmeup.service.dto.AnswerResult;
import com.example.quizmeup.service.dto.QuestionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 面试模拟器核心业务。
 */
@Service
@RequiredArgsConstructor
public class InterviewService {

    private final KnowledgeMapper knowledgeMapper;
    private final QuestionMapper questionMapper;
    private final QuestionRecordMapper questionRecordMapper;
    private final UserMasteryMapper userMasteryMapper;
    private final PromptService promptService;
    private final LlmClient llmClient;

    /**
     * 获取用户的知识点列表（叶节点，作为topic选择）。
     */
    public List<Map<String, Object>> getUserTopics(Long userId) {
        // 获取用户有掌握度记录的所有叶节点知识点
        List<UserMastery> masteryList = userMasteryMapper.selectList(
                Wrappers.<UserMastery>lambdaQuery()
                        .eq(UserMastery::getUserId, userId)
        );
        
        if (masteryList.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> knowledgeIds = masteryList.stream()
                .map(UserMastery::getKnowledgeId)
                .collect(Collectors.toList());
        
        // 获取这些知识点（只取叶节点）
        List<Knowledge> leafNodes = knowledgeMapper.selectList(
                Wrappers.<Knowledge>lambdaQuery()
                        .in(Knowledge::getId, knowledgeIds)
                        .eq(Knowledge::getIsLeaf, true)
        );
        
        return leafNodes.stream().map(k -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", k.getId());
            map.put("name", k.getName());
            UserMastery mastery = masteryList.stream()
                    .filter(m -> m.getKnowledgeId().equals(k.getId()))
                    .findFirst()
                    .orElse(null);
            if (mastery != null) {
                map.put("practicedCount", mastery.getPracticedCount());
                map.put("totalQuestions", mastery.getTotalQuestions());
                map.put("proficiency", mastery.getProficiency());
            }
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 获取指定知识点的所有题目（首次调用时生成）。
     */
    @Transactional
    public List<QuestionDTO> getQuestionsByKnowledge(Long userId, String knowledgeId) {
        // 1. 检查该知识点是否已有题目
        List<Question> existingQuestions = questionMapper.selectList(
                Wrappers.<Question>lambdaQuery()
                        .eq(Question::getKnowledgeId, knowledgeId)
        );
        
        // 2. 如果没有题目，调用LLM生成
        if (existingQuestions.isEmpty()) {
            Knowledge knowledge = knowledgeMapper.selectById(knowledgeId);
            if (knowledge == null) {
                throw new IllegalArgumentException("知识点不存在: " + knowledgeId);
            }
            
            // 调用LLM生成题目
            String prompt = promptService.render("QUESTION_GEN_BY_KNOWLEDGE", Map.of(
                    "knowledgeName", knowledge.getName(),
                    "knowledgeDescription", knowledge.getDescription() != null ? knowledge.getDescription() : ""
            ));
            String raw = llmClient.call(prompt);
            JSONArray questionsJson = JSON.parseArray(raw);
            
            // 保存题目
            for (int i = 0; i < questionsJson.size(); i++) {
                JSONObject q = questionsJson.getJSONObject(i);
                Question question = new Question();
                question.setId(knowledgeId + "_q" + (i + 1));
                question.setKnowledgeId(knowledgeId);
                question.setQuestionText(q.getString("questionText"));
                question.setModelAnswer(q.getString("modelAnswer"));
                question.setDifficulty(q.getInteger("difficulty") != null ? q.getInteger("difficulty") : 3);
                question.setCreatedAt(LocalDateTime.now());
                question.setUpdatedAt(LocalDateTime.now());
                
                questionMapper.insert(question);
            }
            
            // 更新 lc_user_mastery 的 total_questions
            UserMastery mastery = userMasteryMapper.selectOne(
                    Wrappers.<UserMastery>lambdaQuery()
                            .eq(UserMastery::getUserId, userId)
                            .eq(UserMastery::getKnowledgeId, knowledgeId)
            );
            if (mastery != null) {
                mastery.setTotalQuestions(questionsJson.size());
                // 使用复合主键更新
                userMasteryMapper.update(mastery, Wrappers.<UserMastery>lambdaUpdate()
                        .eq(UserMastery::getUserId, userId)
                        .eq(UserMastery::getKnowledgeId, knowledgeId));
            }
            
            // 重新查询
            existingQuestions = questionMapper.selectList(
                    Wrappers.<Question>lambdaQuery()
                            .eq(Question::getKnowledgeId, knowledgeId)
            );
        }
        
        // 3. 转换为DTO返回
        return existingQuestions.stream().map(q -> {
            QuestionDTO dto = new QuestionDTO();
            dto.setId(q.getId());
            dto.setKnowledgeId(q.getKnowledgeId());
            dto.setQuestionText(q.getQuestionText());
            dto.setDifficulty(q.getDifficulty());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 提交答题，调用 LLM 评估并更新掌握度。
     */
    @Transactional
    public AnswerResult submitAnswer(AnswerRequest req) {
        // 1. 获取题目
        Question question = questionMapper.selectById(req.getQuestionId());
        if (question == null) {
            throw new IllegalArgumentException("题目不存在");
        }
        
        // 2. 调用LLM评分
        Map<String, Object> promptVars = new HashMap<>();
        promptVars.put("question", question.getQuestionText());
        promptVars.put("answer", req.getAnswer());
        promptVars.put("modelAnswer", question.getModelAnswer());
        
        String prompt = promptService.render("ANSWER_REVIEW", promptVars);
        String raw = llmClient.call(prompt);
        AnswerResult result = JSON.parseObject(raw, AnswerResult.class);
        
        // 确保字段不为空
        if (result.getFeedbackItems() == null) {
            result.setFeedbackItems(new ArrayList<>());
        }
        if (result.getRecommendedAnswer() == null || result.getRecommendedAnswer().isEmpty()) {
            result.setRecommendedAnswer(question.getModelAnswer());
        }
        if (result.getAnalysis() == null || result.getAnalysis().isEmpty()) {
            result.setAnalysis("暂无详细分析");
        }
        if (result.getScore() == null) {
            result.setScore(0);
        }
        
        // 3. 保存答题记录
        QuestionRecord record = new QuestionRecord();
        record.setUserId(req.getUserId());
        record.setQuestionId(req.getQuestionId());
        record.setScore(result.getScore());
        record.setSubmittedAt(LocalDateTime.now());
        questionRecordMapper.insert(record);
        
        // 4. 更新知识点掌握度（proficiency = 该知识点下所有题的平均分）
        updateKnowledgeProficiency(req.getUserId(), question.getKnowledgeId());
        
        return result;
    }

    /**
     * 更新知识点掌握度。
     */
    private void updateKnowledgeProficiency(Long userId, String knowledgeId) {
        // 获取该知识点下的所有答题记录
        List<Question> questions = questionMapper.selectList(
                Wrappers.<Question>lambdaQuery()
                        .eq(Question::getKnowledgeId, knowledgeId)
        );
        
        if (questions.isEmpty()) {
            return;
        }
        
        List<String> questionIds = questions.stream()
                .map(Question::getId)
                .collect(Collectors.toList());
        
        List<QuestionRecord> records = questionRecordMapper.selectList(
                Wrappers.<QuestionRecord>lambdaQuery()
                        .eq(QuestionRecord::getUserId, userId)
                        .in(QuestionRecord::getQuestionId, questionIds)
        );
        
        // 计算平均分
        int proficiency = 0;
        if (!records.isEmpty()) {
            proficiency = (int) records.stream()
                    .mapToInt(QuestionRecord::getScore)
                    .average()
                    .orElse(0.0);
        }
        
        // 更新或插入 lc_user_mastery
        UserMastery mastery = userMasteryMapper.selectOne(
                Wrappers.<UserMastery>lambdaQuery()
                        .eq(UserMastery::getUserId, userId)
                        .eq(UserMastery::getKnowledgeId, knowledgeId)
        );
        
        if (mastery == null) {
            mastery = new UserMastery();
            mastery.setUserId(userId);
            mastery.setKnowledgeId(knowledgeId);
            mastery.setProficiency(proficiency);
            mastery.setTotalQuestions(questions.size());
            mastery.setPracticedCount(records.size());
            mastery.setUpdatedAt(LocalDateTime.now());
            userMasteryMapper.insert(mastery);
        } else {
            mastery.setProficiency(proficiency);
            mastery.setPracticedCount(records.size());
            mastery.setUpdatedAt(LocalDateTime.now());
            // 使用复合主键更新
            userMasteryMapper.update(mastery, Wrappers.<UserMastery>lambdaUpdate()
                    .eq(UserMastery::getUserId, userId)
                    .eq(UserMastery::getKnowledgeId, knowledgeId));
        }
    }
}
