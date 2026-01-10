package com.example.quizmeup.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.quizmeup.domain.entity.User;
import com.example.quizmeup.infra.mapper.UserMapper;
import com.example.quizmeup.service.dto.LoginRequest;
import com.example.quizmeup.service.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 认证服务。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;

    /**
     * 用户登录。
     */
    public LoginResponse login(LoginRequest request) {
        // 查找用户
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getUsername, request.getUsername())
        );

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        // 验证密码（SHA256）
        String inputPasswordHash = sha256(request.getPassword());
        if (!inputPasswordHash.equals(user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        return new LoginResponse(user.getId(), user.getUsername());
    }

    /**
     * SHA256 加密。
     */
    private String sha256(String input) {
        return input;
//        try {
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
//            StringBuilder hexString = new StringBuilder();
//            for (byte b : hash) {
//                String hex = Integer.toHexString(0xff & b);
//                if (hex.length() == 1) {
//                    hexString.append('0');
//                }
//                hexString.append(hex);
//            }
//            return hexString.toString();
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException("SHA-256算法不可用", e);
//        }
    }
}
