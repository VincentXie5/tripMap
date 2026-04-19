package com.travel.plan.service.impl;

import com.travel.plan.common.BusinessException;
import com.travel.plan.common.code.VerifyCode;
import com.travel.plan.entity.EmailVerifyCode;
import com.travel.plan.repository.EmailVerifyCodeRepository;
import com.travel.plan.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final EmailVerifyCodeRepository emailVerifyCodeRepository;
    
    private static final int CODE_EXPIRE_MINUTES = 10;
    private static final String FROM_EMAIL = "xieyongsen5@gmail.com";
    
    @Override
    @Transactional
    public void sendVerifyCodeEmail(String to, String code) {
        // 生成验证码
        String verifyCode = generateVerifyCode();
        
        // 保存验证码到数据库
        EmailVerifyCode emailVerifyCode = new EmailVerifyCode();
        emailVerifyCode.setEmail(to);
        emailVerifyCode.setCode(verifyCode);
        emailVerifyCode.setType(EmailVerifyCode.VerifyType.REGISTER);
        emailVerifyCode.setExpiredAt(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES));
        emailVerifyCodeRepository.save(emailVerifyCode);
        
        // 发送邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject("TripMap 注册验证码");
            message.setText("您的注册验证码是：" + verifyCode + "，有效期" + CODE_EXPIRE_MINUTES + "分钟。");
            mailSender.send(message);
            log.info("验证码邮件已发送至: {}", to);
        } catch (Exception e) {
            log.error("发送验证码邮件失败: {}", e.getMessage());
            throw new BusinessException(VerifyCode.SEND_ERROR);
        }
    }
    
    private String generateVerifyCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
