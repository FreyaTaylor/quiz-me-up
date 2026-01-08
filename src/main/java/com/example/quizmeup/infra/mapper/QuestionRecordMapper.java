package com.example.quizmeup.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.quizmeup.domain.entity.QuestionRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionRecordMapper extends BaseMapper<QuestionRecord> {
}
