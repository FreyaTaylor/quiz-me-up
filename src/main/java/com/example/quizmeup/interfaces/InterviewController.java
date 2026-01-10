package com.example.quizmeup.interfaces;

import com.example.quizmeup.service.InterviewService;
import com.example.quizmeup.service.dto.AnswerRequest;
import com.example.quizmeup.service.dto.AnswerResult;
import com.example.quizmeup.service.dto.QuestionDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /**
     * 获取指定知识点的所有题目。
     */
    @GetMapping("/questions")
    public List<QuestionDTO> getQuestions(
            @RequestParam Long userId,
            @RequestParam String knowledgeId) {
        return interviewService.getQuestionsByKnowledge(userId, knowledgeId);
    }

    /**
     * 提交答案。
     */
    @PostMapping("/submit")
    public AnswerResult submit(@Valid @RequestBody AnswerRequest req) {
        return interviewService.submitAnswer(req);
    }

    /**
     * 获取用户的知识点列表（叶节点，作为topic选择）。
     */
    @GetMapping("/topics")
    public List<Map<String, Object>> getTopics(@RequestParam Long userId) {
        return interviewService.getUserTopics(userId);
    }
}
