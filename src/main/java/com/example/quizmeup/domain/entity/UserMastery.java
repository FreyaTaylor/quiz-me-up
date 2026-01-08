package com.example.quizmeup.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户知识点掌握度。
 */
@Data
@TableName("lc_user_mastery")
public class UserMastery {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long knowledgeId;
    private Integer masteryScore;
    private LocalDateTime lastAnswerTime;
}
