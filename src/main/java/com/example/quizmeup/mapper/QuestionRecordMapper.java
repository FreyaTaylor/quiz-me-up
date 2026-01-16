package com.example.quizmeup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.quizmeup.entity.QuestionRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 答题记录 Mapper 接口
 */
@Mapper
public interface QuestionRecordMapper extends BaseMapper<QuestionRecord> {
}
