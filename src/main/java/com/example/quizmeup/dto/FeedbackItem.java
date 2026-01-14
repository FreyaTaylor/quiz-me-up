package com.example.quizmeup.dto;

import lombok.Data;

/**
 * 反馈项，用于评价表格。
 */
@Data
public class FeedbackItem {
    /**
     * 评价标准/关键点（如：核心定义、关键特性、使用场景等）
     */
    private String criterion;

    /**
     * 是否覆盖（用户是否回答了该关键点）
     */
    private Boolean covered;

    /**
     * 用户回答内容（针对该关键点的内容，如果未覆盖则为空字符串）
     */
    private String userContent;
}
