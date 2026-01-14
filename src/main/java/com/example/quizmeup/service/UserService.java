package com.example.quizmeup.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.quizmeup.entity.User;
import com.example.quizmeup.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务类
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录（明文密码比对）
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户信息，登录失败返回 null
     */
    public User login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user != null && password.equals(user.getPassword())) {
            return user;
        }
        return null;
    }

    /**
     * 根据用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户信息，不存在返回 null
     */
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
}
