package com.example.quizmeup.interfaces;

import com.example.quizmeup.domain.entity.QuestionRecord;
import com.example.quizmeup.service.InterviewService;
import com.example.quizmeup.service.dto.AnswerRequest;
import com.example.quizmeup.service.dto.AnswerResult;
import com.example.quizmeup.service.dto.QuestionGenRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/questions")
    public List<QuestionRecord> generate(@Valid @RequestBody QuestionGenRequest req) {
        return interviewService.generateQuestions(req);
    }

    @PostMapping("/submit")
    public AnswerResult submit(@Valid @RequestBody AnswerRequest req) {
        return interviewService.submitAnswer(req);
    }

    @GetMapping("/knowledge-tree")
    public List<Map<String, Object>> tree(@RequestParam Long userId) {
        return interviewService.knowledgeTree(userId);
    }
}
