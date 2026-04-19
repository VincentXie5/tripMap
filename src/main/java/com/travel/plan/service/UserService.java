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
}
