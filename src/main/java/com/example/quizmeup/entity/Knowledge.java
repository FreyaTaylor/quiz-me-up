package com.example.quizmeup.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识点实体类
 */
@Data
@TableName("lc_knowledge")
public class Knowledge {
    @TableId(type = IdType.INPUT)
    private String id;
    
    private String parentId;
    
    private String name;
    
    private String description;
    
    private Integer level;
    
    private Integer importance;
    
    private Boolean isLeaf;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
