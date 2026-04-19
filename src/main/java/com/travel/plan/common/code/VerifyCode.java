package com.travel.plan.common.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 验证码模块错误码
 */
@Getter
@AllArgsConstructor
public enum VerifyCode implements ErrorCode {
    EXPIRED("VERIFY-001", "验证码已过期", HttpStatus.BAD_REQUEST),
    MISMATCH("VERIFY-002", "验证码错误", HttpStatus.BAD_REQUEST),
    SEND_ERROR("VERIFY-003", "发送验证码失败，请稍后重试", HttpStatus.SERVICE_UNAVAILABLE);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
