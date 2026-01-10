package com.example.quizmeup.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.quizmeup.domain.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
