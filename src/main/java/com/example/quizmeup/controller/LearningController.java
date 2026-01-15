package com.example.quizmeup.controller;

import com.example.quizmeup.common.Result;
import com.example.quizmeup.dto.QuestionWithScore;
import com.example.quizmeup.dto.StartLearningRequest;
import com.example.quizmeup.dto.SubmitAnswerRequest;
import com.example.quizmeup.dto.SubmitAnswerResponse;
import com.example.quizmeup.service.LearningService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学习控制器
 */
@RestController
@RequestMapping("/api/v1/learning")
public class LearningController {

    private static final Logger logger = LoggerFactory.getLogger(LearningController.class);

    @Autowired
    private LearningService learningService;

    /**
     * 开始学习接口
     * 根据知识点ID获取或生成题目
     *
     * @param request 请求体，包含 userId 和 knowledgeId
     * @return 题目信息（包含最近一次得分）
     */
    @PostMapping("/start")
    public Result<List<QuestionWithScore>> startLearning(@RequestBody StartLearningRequest request) {
        List<QuestionWithScore> questions = learningService.startLearning(request.getUserId(), request.getKnowledgeId());
        return Result.success(questions);

    }

    /**
     * 提交答案接口
     * 提交用户答案并进行 AI 评分
     *
     * @param request 请求体，包含 userId、questionId 和 answerText
     * @return 评分结果
     */
    @PostMapping("/submit")
    public Result<SubmitAnswerResponse> submitAnswer(@RequestBody SubmitAnswerRequest request) throws JsonProcessingException {
        // 调用服务层提交答案并评分
        SubmitAnswerResponse response = learningService.submitAnswer(
                request.getUserId(),
                request.getQuestionId(),
                request.getAnswerText());
        return Result.success(response);

    }
}
