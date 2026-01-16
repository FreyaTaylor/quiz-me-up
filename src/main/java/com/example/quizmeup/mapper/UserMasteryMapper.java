package com.example.quizmeup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.quizmeup.entity.UserMastery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户掌握度 Mapper 接口
 * 注意：UserMastery 使用复合主键，MyBatis-Plus 不支持自动处理复合主键
 * 需要通过 LambdaQueryWrapper 和 LambdaUpdateWrapper 进行查询和更新
 */
@Mapper
public interface UserMasteryMapper extends BaseMapper<UserMastery> {

    @Select("SELECT * FROM user_mastery WHERE user_id = #{userId} AND knowledge_id = #{knowledgeId} FOR UPDATE")
    UserMastery selectForUpdate(@Param("userId") Long userId, @Param("knowledgeId") String knowledgeId);
}
