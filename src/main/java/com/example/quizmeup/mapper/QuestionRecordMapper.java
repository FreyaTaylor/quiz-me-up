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

    /**
     * 查询用户每个题目的最近一次得分
     *
     * @param userId      用户ID
     * @param questionIds 题目ID列表
     * @return 题目ID到最近一次得分的映射（questionId -> score）
     */
    List<QuestionRecord> selectLatestScoresByUserIdAndQuestionIds(@Param("userId") Long userId,
                                                                    @Param("questionIds") List<String> questionIds);

    int insert(QuestionRecord record);
}
