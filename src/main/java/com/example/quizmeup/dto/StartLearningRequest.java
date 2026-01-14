package com.example.quizmeup.dto;

import lombok.Data;

/**
 * 开始学习请求 DTO
 */
@Data
public class StartLearningRequest {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 知识点ID（必须是叶节点）
     */
    private String knowledgeId;
}
