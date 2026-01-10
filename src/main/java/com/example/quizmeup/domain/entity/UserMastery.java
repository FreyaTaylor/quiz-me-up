package com.example.quizmeup.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户知识点掌握度（按knowledge_id聚合）。
 */
@Data
@TableName("lc_user_mastery")
public class UserMastery {
    private Long userId;
    private String knowledgeId;
    private Integer proficiency; // 0-100，该知识点下所有题的平均分
    private Integer totalQuestions; // 该知识点总题数
    private Integer practicedCount; // 已答题数
    private LocalDateTime updatedAt;
}
