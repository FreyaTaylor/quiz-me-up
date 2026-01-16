package com.example.quizmeup.controller;

import com.example.quizmeup.common.FeResponse;
import com.example.quizmeup.dto.QuestionWithScore;
import com.example.quizmeup.dto.StartLearningRequest;
import com.example.quizmeup.dto.SubmitAnswerRequest;
import com.example.quizmeup.dto.SubmitAnswerResponse;
import com.example.quizmeup.service.LearningService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;

    /**
     * 开始学习接口
     * 根据知识点ID获取或生成题目
     *
     * @param request 请求体，包含 userId 和 knowledgeId
     * @return 题目信息（包含最近一次得分）
     */
    @PostMapping("/start")
    public FeResponse<List<QuestionWithScore>> startLearning(@RequestBody StartLearningRequest request) {
        try {
            if (request.getUserId() == null || request.getKnowledgeId() == null) {
                return FeResponse.error("用户ID和知识点ID不能为空");
            }
            List<QuestionWithScore> questions = learningService.startLearning(request.getUserId(), request.getKnowledgeId());
            return FeResponse.success(questions);
        } catch (IllegalArgumentException e) {
            return FeResponse.error(e.getMessage());
        } catch (Exception e) {
            return FeResponse.error("系统错误：" + e.getMessage());
        }
    }

    /**
     * 提交答案接口
     * 提交用户答案并进行 AI 评分
     *
     * @param request 请求体，包含 userId、questionId 和 answerText
     * @return 评分结果
     */
    @PostMapping("/submit")
    public FeResponse<SubmitAnswerResponse> submitAnswer(@RequestBody SubmitAnswerRequest request) {
        try {
            if (request.getUserId() == null || request.getQuestionId() == null || request.getAnswerText() == null) {
                return FeResponse.error("用户ID、题目ID和答案不能为空");
            }
            SubmitAnswerResponse response = learningService.submitAnswer(
                    request.getUserId(),
                    request.getQuestionId(),
                    request.getAnswerText());
            return FeResponse.success(response);
        } catch (IllegalArgumentException e) {
            return FeResponse.error(e.getMessage());
        } catch (Exception e) {
            return FeResponse.error("系统错误：" + e.getMessage());
        }
    }
}
