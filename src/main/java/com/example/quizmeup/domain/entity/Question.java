package com.example.quizmeup.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目表（关联知识点叶节点）。
 */
@Data
@TableName("lc_questions")
public class Question {
    @TableId(type = IdType.INPUT)
    private String id;
    private String knowledgeId;
    private String questionText;
    private String modelAnswer;
    private Integer difficulty; // 1-5
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
