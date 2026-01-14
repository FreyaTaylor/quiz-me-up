package com.example.quizmeup.dto;

import lombok.Data;
import java.util.List;

/**
 * 提交答案响应 DTO
 */
@Data
public class SubmitAnswerResponse {
    /**
     * 得分（0.0-100.0，保留1位小数）
     */
    private Double score;

    /**
     * 对比分析
     */
    private String analysis;

    /**
     * 推荐答案
     */
    private String recommendedAnswer;

    /**
     * 反馈项列表
     */
    private List<FeedbackItem> feedbackItems;

    /**
     * 反馈项内部类
     */
    @Data
    public static class FeedbackItem {
        /**
         * 评分标准/要点
         */
        private String criterion;

        /**
         * 是否已覆盖
         */
        private Boolean covered;

        /**
         * 用户回答的相关内容
         */
        private String userContent;
    }
}
