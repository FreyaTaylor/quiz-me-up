package com.example.quizmeup.dto;

import lombok.Data;

import java.util.List;

/**
 * 答题结果，包含评分、分析、反馈项和推荐回答。
 */
@Data
public class AnswerResult {
    /**
     * 总分（0-100）
     */
    private Integer score;

    /**
     * 总体分析
     */
    private String analysis;

    /**
     * 反馈项列表：每个评价标准的覆盖情况
     */
    private List<FeedbackItem> feedbackItems;

    /**
     * 系统推荐回答
     */
    private String recommendedAnswer;
}
