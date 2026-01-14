package com.example.quizmeup.dto;

import lombok.Data;

/**
 * 登录响应 DTO
 */
@Data
public class LoginResponse {
    private Long userId;
    private String username;
}
