package com.example.quizmeup.dto;

import lombok.Data;

import java.util.List;

/**
 * 知识树节点 DTO（用于前端选择）
 */
@Data
public class KnowledgeTreeNode {
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
     * 熟练程度（0.00-100.00，保留两位小数）
     * 如果用户未学习过，则为 null 或 0
     */
    private java.math.BigDecimal proficiency;

    /**
     * 子节点列表（递归结构）
     */
    private List<KnowledgeTreeNode> children;
}
