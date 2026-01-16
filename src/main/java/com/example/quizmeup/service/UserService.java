package com.example.quizmeup.service;

import com.example.quizmeup.entity.User;
import com.example.quizmeup.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户服务类
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 用户登录（明文密码比对）
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户信息，登录失败返回 null
     */
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);

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
        return userRepository.findById(userId);
    }
}
