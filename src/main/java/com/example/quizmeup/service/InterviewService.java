package com.example.quizmeup.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.quizmeup.ai.LlmClient;
import com.example.quizmeup.ai.PromptService;
import com.example.quizmeup.domain.entity.Knowledge;
import com.example.quizmeup.domain.entity.QuestionRecord;
import com.example.quizmeup.domain.entity.UserMastery;
import com.example.quizmeup.infra.mapper.KnowledgeMapper;
import com.example.quizmeup.infra.mapper.QuestionRecordMapper;
import com.example.quizmeup.infra.mapper.UserMasteryMapper;
import com.example.quizmeup.service.dto.AnswerRequest;
import com.example.quizmeup.service.dto.AnswerResult;
import com.example.quizmeup.service.dto.QuestionDTO;
import com.example.quizmeup.service.dto.QuestionGenRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 面试模拟器核心业务。
 */
@Service
@RequiredArgsConstructor
public class InterviewService {

    private static final String CACHE_KEY_PREFIX = "aimock:user:questions:";

    private final KnowledgeMapper knowledgeMapper;
    private final UserMasteryMapper userMasteryMapper;
    private final QuestionRecordMapper questionRecordMapper;
    private final PromptService promptService;
    private final LlmClient llmClient;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 生成个性化面试题。
     */
    public List<QuestionRecord> generateQuestions(QuestionGenRequest req) {
        List<UserMastery> masteryList = userMasteryMapper.selectList(
                Wrappers.<UserMastery>lambdaQuery().eq(UserMastery::getUserId, req.getUserId())
                        .orderByAsc(UserMastery::getMasteryScore).last("limit 30"));
        List<Long> weakIds = masteryList.stream().map(UserMastery::getKnowledgeId).toList();

        List<Long> recentAnswered = questionRecordMapper.selectList(
                        Wrappers.<QuestionRecord>lambdaQuery()
                                .eq(QuestionRecord::getUserId, req.getUserId())
                                .select(QuestionRecord::getKnowledgeId))
                .stream().map(QuestionRecord::getKnowledgeId).toList();

        List<Long> targetIds = weakIds.stream()
                .filter(id -> !recentAnswered.contains(id)).limit(10).toList();
        if (targetIds.isEmpty()) {
            targetIds = weakIds.stream().limit(10).toList();
        }

        List<Knowledge> knowledges = CollectionUtils.isEmpty(targetIds)
                ? knowledgeMapper.selectList(Wrappers.<Knowledge>lambdaQuery().last("limit 10"))
                : knowledgeMapper.selectBatchIds(targetIds);

        String prompt = promptService.render("QUESTION_GEN", Map.of(
                "topic", req.getTopic(),
                "knowledge", knowledges
        ));
        String raw = llmClient.call(prompt);
        List<QuestionDTO> qs = JSON.parseArray(raw, QuestionDTO.class);

        List<QuestionRecord> saved = new ArrayList<>();
        for (QuestionDTO q : qs) {
            QuestionRecord record = new QuestionRecord();
            record.setUserId(req.getUserId());
            record.setKnowledgeId(q.getKnowledgeId());
            record.setTopic(req.getTopic());
            record.setQuestion(q.getQuestion());
            questionRecordMapper.insert(record);
            saved.add(record);
        }
        redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + req.getUserId(), saved, Duration.ofHours(2));
        return saved;
    }

    /**
     * 提交答题，调用 LLM 评估并更新掌握度。
     */
    public AnswerResult submitAnswer(AnswerRequest req) {
        QuestionRecord record = questionRecordMapper.selectById(req.getQuestionId());
        if (record == null || !record.getUserId().equals(req.getUserId())) {
            throw new IllegalArgumentException("Question not found or unauthorized");
        }
        String prompt = promptService.render("ANSWER_REVIEW", Map.of(
                "question", record.getQuestion(),
                "answer", req.getAnswer()
        ));
        String raw = llmClient.call(prompt);
        AnswerResult result = JSON.parseObject(raw, AnswerResult.class);

        record.setAnswer(req.getAnswer());
        record.setScore(result.getScore());
        record.setAnalysis(result.getAnalysis());
        questionRecordMapper.updateById(record);

        UserMastery mastery = userMasteryMapper.selectOne(
                Wrappers.<UserMastery>lambdaQuery()
                        .eq(UserMastery::getUserId, req.getUserId())
                        .eq(UserMastery::getKnowledgeId, record.getKnowledgeId()));
        if (mastery == null) {
            mastery = new UserMastery();
            mastery.setUserId(req.getUserId());
            mastery.setKnowledgeId(record.getKnowledgeId());
            mastery.setMasteryScore(Math.max(0, Math.min(100, Optional.ofNullable(result.getScore()).orElse(0))));
            mastery.setLastAnswerTime(LocalDateTime.now());
            userMasteryMapper.insert(mastery);
        } else {
            int score = Optional.ofNullable(result.getScore()).orElse(0);
            int newScore = (int) (mastery.getMasteryScore() * 0.7 + score * 0.3);
            mastery.setMasteryScore(Math.min(100, Math.max(0, newScore)));
            mastery.setLastAnswerTime(LocalDateTime.now());
            userMasteryMapper.updateById(mastery);
        }
        return result;
    }

    /**
     * 获取知识点树及掌握度。
     */
    public List<Map<String, Object>> knowledgeTree(Long userId) {
        List<Knowledge> all = knowledgeMapper.selectList(null);
        Map<Long, Integer> mastery = userMasteryMapper.selectList(
                        Wrappers.<UserMastery>lambdaQuery().eq(UserMastery::getUserId, userId))
                .stream().collect(Collectors.toMap(UserMastery::getKnowledgeId, UserMastery::getMasteryScore));
        Map<Long, List<Knowledge>> children = all.stream()
                .collect(Collectors.groupingBy(k -> Optional.ofNullable(k.getParentId()).orElse(0L)));
        return buildTree(children, mastery, 0L);
    }

    private List<Map<String, Object>> buildTree(Map<Long, List<Knowledge>> children,
                                                Map<Long, Integer> mastery,
                                                Long pid) {
        List<Knowledge> nodes = children.getOrDefault(pid, List.of());
        List<Map<String, Object>> res = new ArrayList<>();
        for (Knowledge k : nodes) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", k.getId());
            node.put("name", k.getName());
            node.put("mastery", mastery.getOrDefault(k.getId(), 0));
            node.put("children", buildTree(children, mastery, k.getId()));
            res.add(node);
        }
        return res;
    }
}
