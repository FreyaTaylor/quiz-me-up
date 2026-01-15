package com.example.quizmeup.dto;

import com.example.quizmeup.entity.Question;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 题目及最近一次得分 DTO
 */
@Data
public class QuestionWithScore {
    /**
     * 题目信息
     */
    private Question question;
    
    /**
     * 最近一次得分（0.0-100.0），如果没有答题记录则为0
     */
    private BigDecimal lastScore;
    
    public QuestionWithScore() {
        this.lastScore = BigDecimal.ZERO;
    }
    
    public QuestionWithScore(Question question, BigDecimal lastScore) {
        this.question = question;
        this.lastScore = lastScore != null ? lastScore : BigDecimal.ZERO;
    }
}
