package com.example.quizmeup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.quizmeup.entity.Knowledge;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识点 Mapper 接口
 */
@Mapper
public interface KnowledgeMapper extends BaseMapper<Knowledge> {
}
