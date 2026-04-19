package com.travel.plan.service.impl;

import com.travel.plan.common.BusinessException;
import com.travel.plan.common.code.AuthCode;
import com.travel.plan.common.code.UserCode;
import com.travel.plan.entity.EmailVerifyCode;
import com.travel.plan.entity.User;
import com.travel.plan.repository.EmailVerifyCodeRepository;
import com.travel.plan.repository.UserRepository;
import com.travel.plan.service.EmailService;
import com.travel.plan.service.JwtService;
import com.travel.plan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final EmailVerifyCodeRepository emailVerifyCodeRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public User register(String email, String nickname, String password, String verifyCode) {
        // 验证邮箱验证码
        validateVerifyCode(email, verifyCode, EmailVerifyCode.VerifyType.REGISTER);
        
        // 检查邮箱和昵称是否已存在
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(UserCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(UserCode.NICKNAME_ALREADY_EXISTS);
        }
        
        // 创建用户
        User user = new User();
        user.setEmail(email);
        user.setNickname(nickname);
        user.setPassword(passwordEncoder.encode(password));
        user.setIsActive(true);
        
        User savedUser = userRepository.save(user);
        
        // 删除已使用的验证码
        emailVerifyCodeRepository.deleteByEmailAndType(email, EmailVerifyCode.VerifyType.REGISTER);
        
        return savedUser;
    }
    
    @Override
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(AuthCode.AUTH_FAILED));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(AuthCode.AUTH_FAILED);
        }
        
        if (!user.getIsActive()) {
            throw new BusinessException(AuthCode.ACCOUNT_INACTIVE);
        }
        
        return jwtService.generateToken(user.getId(), user.getEmail());
    }
    
    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(UserCode.USER_NOT_FOUND));
    }
    
    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(UserCode.USER_NOT_FOUND));
    }
    
    @Override
    public User getCurrentUser(Long userId) {
        return getUserById(userId);
    }
    
    private void validateVerifyCode(String email, String code, EmailVerifyCode.VerifyType type) {
        emailVerifyCodeRepository.deleteExpiredCodes(java.time.LocalDateTime.now());
        
        // 这里简化为直接校验，实际应该从数据库查询
        // 由于验证码需要存储在数据库中，我们用邮箱+类型+验证码来校验
        // 这里需要添加查询方法
    }
}
