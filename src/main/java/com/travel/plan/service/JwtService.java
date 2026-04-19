package com.travel.plan.service;

public interface JwtService {
    /**
     * 生成JWT Token
     * @param userId 用户ID
     * @param email 用户邮箱
     * @return JWT Token字符串
     */
    String generateToken(Long userId, String email);
    
    /**
     * 验证Token是否有效
     * @param token JWT Token
     * @return true如果有效，false如果无效或已过期
     */
    boolean validateToken(String token);
    
    /**
     * 从Token中获取用户ID
     * @param token JWT Token
     * @return 用户ID
     */
    Long getUserIdFromToken(String token);
    
    /**
     * 从Token中获取邮箱
     * @param token JWT Token
     * @return 用户邮箱
     */
    String getEmailFromToken(String token);
}
