package com.example.quizmeup.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户掌握度实体类
 * 注意：此表使用复合主键 (user_id, knowledge_id)，MyBatis-Plus 不支持自动处理复合主键
 * 需要使用自定义 Mapper 方法进行查询和更新
 */
@Data
@TableName("lc_user_mastery")
public class UserMastery {
    /**
     * 复合主键字段1：用户ID
     */
    private Long userId;
    
    /**
     * 复合主键字段2：知识点ID
     */
    private String knowledgeId;
    
    private BigDecimal proficiency;
    
    private LocalDateTime updatedAt;
}
