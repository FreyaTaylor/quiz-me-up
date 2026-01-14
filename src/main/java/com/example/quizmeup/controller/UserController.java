package com.example.quizmeup.controller;

import com.example.quizmeup.common.Result;
import com.example.quizmeup.dto.LoginRequest;
import com.example.quizmeup.dto.LoginResponse;
import com.example.quizmeup.entity.User;
import com.example.quizmeup.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录接口
     *
     * @param request 登录请求（username, password）
     * @return 登录成功返回用户信息，失败返回 401
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return Result.error("用户名和密码不能为空");
        }

        User user = userService.login(request.getUsername(), request.getPassword());
        if (user == null) {
            return Result.unauthorized("用户名或密码错误");
        }

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());

        return Result.success(response);
    }
}
