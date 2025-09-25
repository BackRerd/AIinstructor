package lnjz.backrer.aiinstructor.controller;

import lnjz.backrer.aiinstructor.DTO.LoginRequest;
import lnjz.backrer.aiinstructor.DTO.LoginResponse;
import lnjz.backrer.aiinstructor.DTO.RegisterRequest;
import lnjz.backrer.aiinstructor.entity.Result;
import lnjz.backrer.aiinstructor.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<?> login(@Validated @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/register")
    public Result<?> register(@Validated @RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

    // 可以添加其他接口，如检查用户名/手机号是否存在的接口
    @GetMapping("/check-username")
    public Result<Boolean> checkUsernameExists(@RequestParam String username) {
        boolean exists = authService.getUserService().checkUsernameExists(username);
        return Result.success(exists ? "用户名已存在" : "用户名可用", exists);
    }

    @GetMapping("/check-phone")
    public Result<Boolean> checkPhoneExists(@RequestParam String phoneNumber) {
        boolean exists = authService.getUserService().checkPhoneNumberExists(phoneNumber);

        return Result.success(exists ? "手机号已注册" : "手机号可用", exists);
    }

    /**
     * 验证并刷新Token，用于自动登录
     * 前端在启动时可以调用此接口检查本地存储的token是否有效
     */
    @PostMapping("/verify-token")
    public Result<LoginResponse> verifyToken(@RequestHeader("Authorization") String token) {
        return authService.verifyAndRefreshToken(token);
    }
}