package com.example.quizmeup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.quizmeup.entity.QuestionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionRecordMapper extends BaseMapper<QuestionRecord> {
    List<QuestionRecord> selectByUserIdAndKnowledgeId(@Param("userId") Long userId,
                                                      @Param("knowledgeId") String knowledgeId);

    List<QuestionRecord> selectByUserIdAndQuestionId(@Param("userId") Long userId,
                                                      @Param("questionId") String questionId);

    int insert(QuestionRecord record);
}
