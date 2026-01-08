package com.example.quizmeup.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Prompt 模板表，用于集中管理提示词。
 */
@Data
@TableName("lc_prompt_template")
public class PromptTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String content;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
