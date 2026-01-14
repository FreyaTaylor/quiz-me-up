package com.example.quizmeup.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.quizmeup.ai.LlmClient;
import com.example.quizmeup.ai.PromptService;
import com.example.quizmeup.dto.AnswerEvaluation;
import com.example.quizmeup.entity.*;
import com.example.quizmeup.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StudyService {

    @Autowired
    private KnowledgeMapper knowledgeMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionRecordMapper questionRecordMapper;

    @Autowired
    private UserMasteryMapper userMasteryMapper;

    @Autowired
    private PromptService promptService;

    @Autowired
    private LlmClient llmClient;

    /**
     * 获取所有叶节点知识点
     */
    public List<Knowledge> getAllLeafKnowledge() {
        return knowledgeMapper.selectAllLeafNodes();
    }



    /**
     * 更新用户掌握度
     */
    @Transactional
    public void updateUserMastery(Long userId, String knowledgeId) {
        List<QuestionRecord> records = questionRecordMapper.selectByUserIdAndKnowledgeId(userId, knowledgeId);

        if (records.isEmpty()) {
            return;
        }

        double avgScore = records.stream()
                .mapToInt(QuestionRecord::getScoreInt)
                .average()
                .orElse(0.0);

        BigDecimal proficiency = BigDecimal.valueOf(avgScore)
                .setScale(2, RoundingMode.HALF_UP);

        UserMastery mastery = userMasteryMapper.selectByUserIdAndKnowledgeId(userId, knowledgeId);
        if (mastery == null) {
            mastery = new UserMastery();
            mastery.setUserId(userId);
            mastery.setKnowledgeId(knowledgeId);
            mastery.setProficiency(proficiency);
            userMasteryMapper.insert(mastery);
        } else {
            mastery.setProficiency(proficiency);
            userMasteryMapper.updateProficiency(mastery);
        }
    }
}
