package com.travel.plan.controller;

import com.travel.plan.common.ApiResult;
import com.travel.plan.controller.dto.AuthRequest;
import com.travel.plan.controller.dto.SendCodeRequest;
import com.travel.plan.controller.dto.UserResponse;
import com.travel.plan.entity.User;
import com.travel.plan.service.EmailService;
import com.travel.plan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final EmailService emailService;
    
    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public ApiResult<Void> sendCode(@RequestBody SendCodeRequest request) {
        emailService.sendVerifyCodeEmail(request.getEmail(), null);
        return ApiResult.success("验证码已发送", null);
    }
    
    /**
     * 注册
     */
    @PostMapping("/register")
    public ApiResult<UserResponse> register(@RequestBody AuthRequest request) {
        User user = userService.register(
                request.getEmail(),
                request.getNickname(),
                request.getPassword(),
                request.getVerifyCode()
        );
        return ApiResult.success("注册成功", toUserResponse(user));
    }
    
    /**
     * 登录
     */
    @PostMapping("/login")
    public ApiResult<String> login(@RequestBody AuthRequest request) {
        String token = userService.login(request.getEmail(), request.getPassword());
        return ApiResult.success("登录成功", token);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ApiResult<UserResponse> getCurrentUser(org.springframework.security.core.Authentication authentication) {
        com.travel.plan.config.UserPrincipal principal = (com.travel.plan.config.UserPrincipal) authentication.getPrincipal();
        User user = userService.getUserById(principal.getUserId());
        return ApiResult.success(toUserResponse(user));
    }
    
    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAvatarType().name(),
                user.getIsActive()
        );
    }
}
