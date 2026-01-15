package com.example.quizmeup.mapper;

import com.example.quizmeup.entity.UserMastery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户掌握度 Mapper 接口
 * 注意：UserMastery 使用复合主键，需要使用自定义方法
 */
@Mapper
public interface UserMasteryMapper {
    /**
     * 根据用户ID和知识点ID查询掌握度
     *
     * @param userId      用户ID
     * @param knowledgeId 知识点ID
     * @return 用户掌握度信息
     */
    UserMastery selectByUserIdAndKnowledgeId(@Param("userId") Long userId,
                                             @Param("knowledgeId") String knowledgeId);

    /**
     * 根据用户ID和知识点ID查询掌握度（加锁，用于并发更新）
     * 使用 SELECT FOR UPDATE 锁定记录，防止并发更新时的丢失更新问题
     *
     * @param userId      用户ID
     * @param knowledgeId 知识点ID
     * @return 用户掌握度信息
     */
    UserMastery selectByUserIdAndKnowledgeIdForUpdate(@Param("userId") Long userId,
                                                      @Param("knowledgeId") String knowledgeId);

    /**
     * 插入用户掌握度记录
     *
     * @param mastery 用户掌握度信息
     * @return 影响行数
     */
    int insert(UserMastery mastery);

    /**
     * 更新用户掌握度
     *
     * @param mastery 用户掌握度信息
     * @return 影响行数
     */
    int updateProficiency(UserMastery mastery);
}
