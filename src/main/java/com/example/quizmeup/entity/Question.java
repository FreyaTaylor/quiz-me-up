package com.example.quizmeup.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目实体类
 */
@Data
@TableName("lc_questions")
public class Question {
    @TableId(type = IdType.INPUT)
    private String id;
    
    private String knowledgeId;
    
    private String questionText;
    
    private String modelAnswer;
    
    private Integer importance;
    
    private Integer difficulty;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
