package com.example.quizmeup.common;

import lombok.Data;

import static com.example.quizmeup.common.Constants.*;

/**
 * 统一响应结果封装
 */
@Data
public class FeResponse<T> {
    private Integer code;
    private String message;
    private T data;

    private FeResponse() {
    }

    private FeResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应
     */
    public static <T> FeResponse<T> success(T data) {
        return new FeResponse<>(HTTP_STATUS_SUCCESS, "success", data);
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> FeResponse<T> success() {
        return new FeResponse<>(HTTP_STATUS_SUCCESS, "success", null);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> FeResponse<T> success(String message, T data) {
        return new FeResponse<>(HTTP_STATUS_SUCCESS, message, data);
    }

    /**
     * 错误响应
     */
    public static <T> FeResponse<T> error(String message) {
        return new FeResponse<>(HTTP_STATUS_BAD_REQUEST, message, null);
    }

    /**
     * 错误响应（自定义状态码）
     */
    public static <T> FeResponse<T> error(Integer code, String message) {
        return new FeResponse<>(code, message, null);
    }

    /**
     * 未授权响应
     */
    public static <T> FeResponse<T> unauthorized(String message) {
        return new FeResponse<>(HTTP_STATUS_UNAUTHORIZED, message, null);
    }
}
