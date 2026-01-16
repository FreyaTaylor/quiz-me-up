package com.example.quizmeup.interceptor;

import com.example.quizmeup.common.FeResponse;
import com.example.quizmeup.entity.User;
import com.example.quizmeup.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 * 拦截 /api/** 路径（排除 /api/auth/**），验证 user_id 是否有效
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // 排除 /api/auth/** 路径（仅登录接口公开）
        if (path.startsWith("/api/auth/")) {
            return true;
        }

        // 所有其他 /api/** 接口都需要登录验证
        // 获取 user_id（优先从请求参数获取，其次从 Header 获取）
        String userIdStr = request.getParameter("userId");
        if (userIdStr == null || userIdStr.isEmpty()) {
            userIdStr = request.getHeader("userId");
        }

        if (userIdStr == null || userIdStr.isEmpty()) {
            writeErrorResponse(response, FeResponse.error("Missing user_id parameter"));
            return false;
        }

        try {
            Long userId = Long.parseLong(userIdStr);
            User user = userService.getUserById(userId);

            if (user == null) {
                writeErrorResponse(response, FeResponse.error("Invalid user"));
                return false;
            }

            // 验证通过，继续执行
            return true;
        } catch (NumberFormatException e) {
            writeErrorResponse(response, FeResponse.error("Invalid user_id format"));
            return false;
        }
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, FeResponse<?> result) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
