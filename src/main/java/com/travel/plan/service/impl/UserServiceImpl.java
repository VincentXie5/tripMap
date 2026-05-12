package com.travel.plan.service.impl;

import com.travel.plan.common.BusinessException;
import com.travel.plan.common.code.AuthCode;
import com.travel.plan.common.code.UserCode;
import com.travel.plan.controller.dto.ProfileResponse;
import com.travel.plan.entity.EmailVerifyCode;
import com.travel.plan.entity.User;
import com.travel.plan.repository.EmailVerifyCodeRepository;
import com.travel.plan.repository.UserRepository;
import com.travel.plan.service.EmailService;
import com.travel.plan.service.FileService;
import com.travel.plan.service.JwtService;
import com.travel.plan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final EmailVerifyCodeRepository emailVerifyCodeRepository;
    private final EmailService emailService;
    private final FileService fileService;
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
    
    @Override
    public ProfileResponse getProfile(Long userId) {
        User user = getUserById(userId);
        return toProfileResponse(user);
    }
    
    @Override
    @Transactional
    public User updateAvatar(Long userId, String avatarType) {
        User.AvatarType type;
        try {
            type = User.AvatarType.valueOf(avatarType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(UserCode.INVALID_AVATAR_TYPE);
        }
        
        User user = getUserById(userId);
        user.setAvatarType(type);
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public User uploadAvatar(Long userId, java.io.InputStream inputStream, String contentType, String originalFilename, long size) {
        // 校验文件类型
        String ext = getFileExtension(originalFilename);
        if (ext == null || !ext.matches("^(jpg|jpeg|png|gif|webp)$")) {
            throw new BusinessException(UserCode.INVALID_AVATAR_FILE_TYPE);
        }
        // 校验文件大小 (2MB)
        if (size > 2 * 1024 * 1024) {
            throw new BusinessException(UserCode.AVATAR_FILE_TOO_LARGE);
        }

        User user = getUserById(userId);

        // 如果之前是 CUSTOM 且有不同扩展名，删除旧文件
        if (user.getAvatarType() == User.AvatarType.CUSTOM && user.getAvatarExt() != null) {
            if (!user.getAvatarExt().equals(ext)) {
                fileService.deleteByPrefix("avatars/" + userId + ".");
            }
        }

        String key = "avatars/" + userId + "." + ext;
        fileService.upload(key, inputStream, contentType, size);

        user.setAvatarType(User.AvatarType.CUSTOM);
        user.setAvatarExt(ext);
        return userRepository.save(user);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    @Override
    @Transactional
    public User updateNickname(Long userId, String newNickname) {
        User user = getUserById(userId);
        
        // 如果昵称相同，不做修改
        if (user.getNickname().equals(newNickname)) {
            return user;
        }
        
        // 检查昵称是否已被使用
        if (userRepository.existsByNickname(newNickname)) {
            throw new BusinessException(UserCode.NICKNAME_ALREADY_EXISTS);
        }
        
        user.setNickname(newNickname);
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        // 验证新密码长度
        if (newPassword.length() < 8) {
            throw new BusinessException(AuthCode.PASSWORD_TOO_WEAK);
        }
        
        User user = getUserById(userId);
        
        // 验证原密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(UserCode.INVALID_OLD_PASSWORD);
        }
        
        // 检查新旧密码是否相同
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BusinessException(UserCode.EMAIL_NOT_CHANGED, "新密码不能与原密码相同");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void sendEmailChangeCode(Long userId, String newEmail) {
        User user = getUserById(userId);
        
        // 检查邮箱是否相同
        if (user.getEmail().equals(newEmail)) {
            throw new BusinessException(UserCode.EMAIL_NOT_CHANGED);
        }
        
        // 检查新邮箱是否已被注册
        if (userRepository.existsByEmail(newEmail)) {
            throw new BusinessException(UserCode.EMAIL_ALREADY_EXISTS);
        }
        
        // 生成并发送验证码
        String code = generateVerifyCode();
        EmailVerifyCode verifyCode = new EmailVerifyCode();
        verifyCode.setEmail(newEmail);
        verifyCode.setCode(code);
        verifyCode.setType(EmailVerifyCode.VerifyType.EMAIL_CHANGE);
        verifyCode.setExpiredAt(java.time.LocalDateTime.now().plusMinutes(10));
        emailVerifyCodeRepository.save(verifyCode);
        
        emailService.sendVerifyCodeEmail(newEmail, code);
    }
    
    @Override
    @Transactional
    public User changeEmail(Long userId, String newEmail, String verifyCode) {
        User user = getUserById(userId);
        
        // 验证新邮箱是否已注册
        if (userRepository.existsByEmail(newEmail)) {
            throw new BusinessException(UserCode.EMAIL_ALREADY_EXISTS);
        }
        
        // 验证验证码
        validateVerifyCode(newEmail, verifyCode, EmailVerifyCode.VerifyType.EMAIL_CHANGE);
        
        // 更新邮箱
        user.setEmail(newEmail);
        User savedUser = userRepository.save(user);
        
        // 删除已使用的验证码
        emailVerifyCodeRepository.deleteByEmailAndType(newEmail, EmailVerifyCode.VerifyType.EMAIL_CHANGE);
        
        return savedUser;
    }
    
    @Override
    public String generateAvatarUrl(User user) {
        if (user.getAvatarType() == User.AvatarType.GRAVATAR) {
            String emailHash = md5Hex(user.getEmail().toLowerCase().trim());
            return "https://www.gravatar.com/avatar/" + emailHash + "?d=identicon&s=200";
        }
        if (user.getAvatarType() == User.AvatarType.CUSTOM && user.getAvatarExt() != null) {
            return "/api/files/avatars/" + user.getId() + "." + user.getAvatarExt();
        }
        // DEFAULT 或 CUSTOM 但 avatarExt 为空时，生成默认 SVG
        String initial = user.getNickname().substring(0, 1).toUpperCase();
        return "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200' viewBox='0 0 200 200'%3E%3Crect fill='%234F46E5' width='200' height='200'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' fill='white' font-size='80' font-family='Arial'%3E" + initial + "%3C/text%3E%3C/svg%3E";
    }
    
    public ProfileResponse toProfileResponse(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAvatarType() != null ? user.getAvatarType().name() : "DEFAULT",
                user.getIsActive(),
                user.getCreatedAt(),
                generateAvatarUrl(user)
        );
    }
    
    private void validateVerifyCode(String email, String code, EmailVerifyCode.VerifyType type) {
        // 删除过期验证码
        emailVerifyCodeRepository.deleteExpiredCodes(java.time.LocalDateTime.now());
        
        Optional<EmailVerifyCode> verifyCodeOpt = emailVerifyCodeRepository.findByEmailAndType(email, type);
        if (verifyCodeOpt.isEmpty()) {
            throw new BusinessException(AuthCode.INVALID_VERIFY_CODE);
        }
        
        EmailVerifyCode verifyCode = verifyCodeOpt.get();
        if (!verifyCode.getCode().equals(code)) {
            throw new BusinessException(AuthCode.INVALID_VERIFY_CODE);
        }
    }
    
    private String generateVerifyCode() {
        int code = (int) ((Math.random() * 900000) + 100000);
        return String.valueOf(code);
    }
    
    private String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                String hexString = Integer.toHexString(0xff & b);
                if (hexString.length() == 1) hex.append("0");
                hex.append(hexString);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
