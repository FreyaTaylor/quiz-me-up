package com.example.quizmeup.dto;

import lombok.Data;

/**
 * 知识点初始化请求 DTO
 */
@Data
public class KnowledgeInitRequest {
    /**
     * 知识树根节点名称（如 "Java", "Python"）
     */
    private String knowledgeRoot;
    
    /**
     * 知识树节点数量（建议值）
     */
    private Integer count;
}
