package com.example.quizmeup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.quizmeup.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目 Mapper 接口
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    /**
     * 根据知识点ID查询题目列表
     *
     * @param knowledgeId 知识点ID
     * @return 题目列表
     */
    List<Question> selectByKnowledgeId(@Param("knowledgeId") String knowledgeId);
}
