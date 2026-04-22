package com.travel.plan.controller;

import com.travel.plan.common.ApiResult;
import com.travel.plan.controller.dto.*;
import com.travel.plan.entity.User;
import com.travel.plan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    
    private final UserService userService;
    
    /**
     * 获取当前用户资料
     */
    @GetMapping
    public ApiResult<ProfileResponse> getProfile(@AuthenticationPrincipal com.travel.plan.config.UserPrincipal principal) {
        ProfileResponse profile = userService.getProfile(principal.getUserId());
        return ApiResult.success(profile);
    }
    
    /**
     * 更新头像类型
     */
    @PutMapping("/avatar")
    public ApiResult<ProfileResponse> updateAvatar(
            @AuthenticationPrincipal com.travel.plan.config.UserPrincipal principal,
            @RequestBody AvatarUpdateRequest request) {
        User user = userService.updateAvatar(principal.getUserId(), request.getAvatarType());
        return ApiResult.success("头像更新成功", userService.toProfileResponse(user));
    }
    
    /**
     * 更新昵称
     */
    @PutMapping("/nickname")
    public ApiResult<ProfileResponse> updateNickname(
            @AuthenticationPrincipal com.travel.plan.config.UserPrincipal principal,
            @RequestBody NicknameUpdateRequest request) {
        User user = userService.updateNickname(principal.getUserId(), request.getNickname());
        return ApiResult.success("昵称更新成功", userService.toProfileResponse(user));
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/password")
    public ApiResult<Void> changePassword(
            @AuthenticationPrincipal com.travel.plan.config.UserPrincipal principal,
            @RequestBody PasswordChangeRequest request) {
        userService.changePassword(principal.getUserId(), request.getOldPassword(), request.getNewPassword());
        return ApiResult.success("密码修改成功", null);
    }
    
    /**
     * 发送邮箱修改验证码
     */
    @PostMapping("/send-code")
    public ApiResult<Void> sendEmailChangeCode(
            @AuthenticationPrincipal com.travel.plan.config.UserPrincipal principal,
            @RequestBody SendCodeRequest request) {
        userService.sendEmailChangeCode(principal.getUserId(), request.getEmail());
        return ApiResult.success("验证码已发送到新邮箱", null);
    }
    
    /**
     * 修改邮箱
     */
    @PutMapping("/email")
    public ApiResult<ProfileResponse> changeEmail(
            @AuthenticationPrincipal com.travel.plan.config.UserPrincipal principal,
            @RequestBody EmailChangeRequest request) {
        User user = userService.changeEmail(principal.getUserId(), request.getEmail(), request.getVerifyCode());
        return ApiResult.success("邮箱修改成功", userService.toProfileResponse(user));
    }
}
