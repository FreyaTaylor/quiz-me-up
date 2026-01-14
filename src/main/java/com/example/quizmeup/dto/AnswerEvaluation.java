package com.example.quizmeup.dto;

import lombok.Data;

/**
 * 答案评价 DTO
 * 用于返回 AI 对用户答案的评价结果
 */
@Data
public class AnswerEvaluation {
    /**
     * 得分（0.0-100.0）
     */
    private Double score;

    /**
     * 判断/结论
     */
    private String judgment;

    /**
     * 对比分析
     */
    private String comparison;

    /**
     * 推荐答案
     */
    private String recommendedAnswer;
}
