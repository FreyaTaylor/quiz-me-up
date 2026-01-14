package com.example.quizmeup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.quizmeup.entity.PromptTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * Prompt 模板 Mapper 接口
 */
@Mapper
public interface PromptTemplateMapper extends BaseMapper<PromptTemplate> {
}
