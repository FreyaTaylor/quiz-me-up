package com.example.quizmeup.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.quizmeup.entity.User;
import com.example.quizmeup.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 用户数据访问层
 * 封装所有对 User 实体的增删改查逻辑
 */
@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final UserMapper userMapper;

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息，不存在返回 null
     */
    public User findByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 根据用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户信息，不存在返回 null
     */
    public User findById(Long userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 保存用户
     *
     * @param user 用户信息
     * @return 影响行数
     */
    public int save(User user) {
        if (user.getId() == null) {
            return userMapper.insert(user);
        } else {
            return userMapper.updateById(user);
        }
    }
}
