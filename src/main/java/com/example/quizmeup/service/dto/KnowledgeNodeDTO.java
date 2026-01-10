package com.example.quizmeup.service.dto;

import lombok.Data;
import java.util.List;

/**
 * 知识点节点DTO（用于前端展示）。
 */
@Data
public class KnowledgeNodeDTO {
    private String id;
    private String name;
    private String description; // 中文描述
    private String parentId;
    private Integer level;
    private Integer importance;
    private Boolean isLeaf;
    private Integer proficiency; // 掌握度 0-100
    private Integer totalQuestions; // 总题数
    private Integer practicedCount; // 已答题数
    private Double weightedProgress; // 加权进度
    private List<KnowledgeNodeDTO> children;
}
