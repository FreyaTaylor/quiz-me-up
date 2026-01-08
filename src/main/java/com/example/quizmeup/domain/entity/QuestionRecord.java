package com.example.quizmeup.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目记录，用于题目生成与答题结果追踪。
 */
@Data
@TableName("lc_question_record")
public class QuestionRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long knowledgeId;
    private String topic;
    private String question;
    private String answer;
    private Integer score;
    private String analysis;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
