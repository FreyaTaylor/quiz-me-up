package com.example.quizmeup.service.dto;

import lombok.Data;

/**
 * 题目DTO（用于返回给前端）。
 */
@Data
public class QuestionDTO {
    private String id;
    private String knowledgeId;
    private String questionText;
    private Integer difficulty;
}
