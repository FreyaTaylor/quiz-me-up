package com.example.quizmeup.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 答题记录实体类
 */
@Data
@TableName("lc_question_record")
public class QuestionRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String questionId;
    
    private BigDecimal score;
    
    private LocalDateTime submittedAt;
    
    /**
     * 获取整数类型的分数（用于计算）
     * 注意：Lombok 会生成 getScore() 返回 BigDecimal，这里提供一个便捷方法返回 int
     */
    public int getScoreInt() {
        return score != null ? score.intValue() : 0;
    }
}
