package com.travel.plan.repository;

import com.travel.plan.entity.EmailVerifyCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerifyCodeRepository extends JpaRepository<EmailVerifyCode, Long> {
    
    Optional<EmailVerifyCode> findByEmailAndCodeAndType(String email, String code, EmailVerifyCode.VerifyType type);
    
    @Modifying
    @Query("DELETE FROM EmailVerifyCode e WHERE e.email = :email AND e.type = :type")
    void deleteByEmailAndType(@Param("email") String email, @Param("type") EmailVerifyCode.VerifyType type);
    
    @Modifying
    @Query("DELETE FROM EmailVerifyCode e WHERE e.expiredAt < :now")
    void deleteExpiredCodes(@Param("now") LocalDateTime now);
}
