package com.example.quizmeup.dto;

import lombok.Data;

/**
 * 开始学习响应 DTO
 */
@Data
public class StartLearningResponse {
    /**
     * 题目ID
     */
    private String questionId;

    /**
     * 题目内容
     */
    private String questionText;
}
