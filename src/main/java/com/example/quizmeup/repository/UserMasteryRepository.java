package com.example.quizmeup.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.quizmeup.entity.UserMastery;
import com.example.quizmeup.mapper.UserMasteryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户掌握度数据访问层
 * 封装所有对 UserMastery 实体的增删改查逻辑
 */
@Repository
@RequiredArgsConstructor
public class UserMasteryRepository {

    private final UserMasteryMapper userMasteryMapper;

    /**
     * 根据用户ID和知识点ID查询掌握度
     *
     * @param userId      用户ID
     * @param knowledgeId 知识点ID
     * @return 用户掌握度信息，不存在返回 null
     */
    public UserMastery findByUserIdAndKnowledgeId(Long userId, String knowledgeId) {
        LambdaQueryWrapper<UserMastery> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMastery::getUserId, userId)
                .eq(UserMastery::getKnowledgeId, knowledgeId);
        return userMasteryMapper.selectOne(wrapper);
    }

    public List<UserMastery> findByUserId(Long userId) {
        LambdaQueryWrapper<UserMastery> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMastery::getUserId, userId);
        return userMasteryMapper.selectList(wrapper);
    }

    /**
     * 根据用户ID和知识点ID查询掌握度（加锁，用于并发更新）
     * @param userId      用户ID
     * @param knowledgeId 知识点ID
     * @return 用户掌握度信息，不存在返回 null
     */
    public UserMastery findByUserIdAndKnowledgeIdForUpdate(Long userId, String knowledgeId) {
        return userMasteryMapper.selectForUpdate(userId, knowledgeId);
    }

    /**
     * 保存用户掌握度记录
     * 如果已存在则更新，否则插入
     *
     * @param mastery 用户掌握度信息
     * @return 影响行数
     */
    public int save(UserMastery mastery) {
        UserMastery existing = findByUserIdAndKnowledgeId(mastery.getUserId(), mastery.getKnowledgeId());
        if (existing != null) {
            // 更新
            LambdaUpdateWrapper<UserMastery> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserMastery::getUserId, mastery.getUserId())
                    .eq(UserMastery::getKnowledgeId, mastery.getKnowledgeId())
                    .set(UserMastery::getProficiency, mastery.getProficiency())
                    .set(UserMastery::getUpdatedAt, mastery.getUpdatedAt());
            return userMasteryMapper.update(null, updateWrapper);
        } else {
            // 插入
            return userMasteryMapper.insert(mastery);
        }
    }

    /**
     * 更新用户掌握度
     *
     * @param mastery 用户掌握度信息
     * @return 影响行数
     */
    public int updateProficiency(UserMastery mastery) {
        LambdaUpdateWrapper<UserMastery> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserMastery::getUserId, mastery.getUserId())
                .eq(UserMastery::getKnowledgeId, mastery.getKnowledgeId())
                .set(UserMastery::getProficiency, mastery.getProficiency())
                .set(UserMastery::getUpdatedAt, mastery.getUpdatedAt());
        return userMasteryMapper.update(null, updateWrapper);
    }
}
