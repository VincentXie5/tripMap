package com.travel.plan.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "email_verify_code", indexes = {
    @Index(name = "idx_email_type_expired", columnList = "email, type, expired_at")
})
public class EmailVerifyCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false, length = 10)
    private String code;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerifyType type;
    
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum VerifyType {
        REGISTER, FORGET_PASSWORD, EMAIL_CHANGE
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }
}
