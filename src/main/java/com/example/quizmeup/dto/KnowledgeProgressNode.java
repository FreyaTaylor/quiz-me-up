package com.example.quizmeup.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 知识点进度节点 DTO
 * 用于返回知识树结构及每个节点的掌握度
 */
@Data
public class KnowledgeProgressNode {
    /**
     * 知识点ID
     */
    private String id;

    /**
     * 知识点名称
     */
    private String name;

    /**
     * 知识点描述
     */
    private String description;

    /**
     * 掌握度（0.00-100.00，保留两位小数）
     */
    private BigDecimal proficiency;

    /**
     * 是否为叶节点
     */
    private Boolean isLeaf;

    /**
     * 重要性（1-5）
     */
    private Integer importance;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 子节点列表（递归结构）
     */
    private List<KnowledgeProgressNode> children;
}
