package com.example.quizmeup.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识点定义。
 */
@Data
@TableName("lc_knowledge")
public class Knowledge {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String name;
    private String description;
    private Integer level;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
