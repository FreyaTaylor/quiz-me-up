package com.example.quizmeup.dto;

import lombok.Data;

/**
 * 提交答案请求 DTO
 */
@Data
public class SubmitAnswerRequest {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 题目ID
     */
    private String questionId;

    /**
     * 用户答案文本
     */
    private String answerText;
}
