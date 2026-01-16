package com.example.quizmeup.dto;

import lombok.Data;

/**
 * 根据根节点获取知识树请求 DTO
 */
@Data
public class KnowledgeTreeByRootRequest {
    /**
     * 根节点ID
     */
    private String rootId;
    
    /**
     * 用户ID（可选，用于获取熟练度）
     */
    private Long userId;
}
