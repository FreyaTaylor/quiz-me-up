package com.example.quizmeup.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Prompt 模板实体类
 */
@Data
@TableName("lc_prompt_template")
public class PromptTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板代码（如 "INTERVIEW_Q_GEN", "ANSWER_EVALUATION"）
     */
    private String code;

    /**
     * 模板内容
     */
    private String content;

    /**
     * 描述
     */
    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
