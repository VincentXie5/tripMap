package com.travel.plan.service;

import com.travel.plan.entity.User;

public interface UserService {
    /**
     * 用户注册
     * @param email 邮箱
     * @param nickname 昵称
     * @param password 密码（未加密）
     * @param verifyCode 验证码
     * @return 注册后的用户
     */
    User register(String email, String nickname, String password, String verifyCode);
    
    /**
     * 用户登录
     * @param email 邮箱
     * @param password 密码
     * @return JWT Token
     */
    String login(String email, String password);
    
    /**
     * 根据邮箱获取用户
     * @param email 邮箱
     * @return 用户
     */
    User getUserByEmail(String email);
    
    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return 用户
     */
    User getUserById(Long id);
    
    /**
     * 获取当前登录用户
     * @param userId 从Token中解析的用户ID
     * @return 用户
     */
    User getCurrentUser(Long userId);
    
    /**
     * 获取用户资料（含头像URL）
     * @param userId 用户ID
     * @return 用户资料响应
     */
    com.travel.plan.controller.dto.ProfileResponse getProfile(Long userId);
    
    /**
     * 更新头像类型
     * @param userId 用户ID
     * @param avatarType 头像类型
     * @return 更新后的用户
     */
    User updateAvatar(Long userId, String avatarType);
    
    /**
     * 更新昵称
     * @param userId 用户ID
     * @param newNickname 新昵称
     * @return 更新后的用户
     */
    User updateNickname(Long userId, String newNickname);
    
    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 发送邮箱修改验证码
     * @param userId 用户ID
     * @param newEmail 新邮箱
     */
    void sendEmailChangeCode(Long userId, String newEmail);
    
    /**
     * 修改邮箱
     * @param userId 用户ID
     * @param newEmail 新邮箱
     * @param verifyCode 验证码
     * @return 更新后的用户
     */
    User changeEmail(Long userId, String newEmail, String verifyCode);
    
    /**
     * 生成头像URL
     * @param user 用户
     * @return 头像URL
     */
    String generateAvatarUrl(User user);
    
    /**
     * 将User转换为ProfileResponse
     * @param user 用户
     * @return ProfileResponse
     */
    com.travel.plan.controller.dto.ProfileResponse toProfileResponse(User user);
}
