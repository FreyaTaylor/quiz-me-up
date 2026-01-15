package com.example.quizmeup.dto;

import lombok.Data;

/**
 * 登录响应 DTO
 */
@Data
public class LoginResponse {
    private Long userId;
    private String username;
    private String role; // 用户角色，ADMIN为管理员，null为普通用户
}
