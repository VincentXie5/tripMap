package com.travel.plan.common.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 认证模块错误码
 */
@Getter
@AllArgsConstructor
public enum AuthCode implements ErrorCode {
    AUTH_FAILED("AUTH-001", "邮箱或密码错误", HttpStatus.UNAUTHORIZED),
    ACCOUNT_INACTIVE("AUTH-002", "账号未激活，请先激活账号", HttpStatus.FORBIDDEN),
    TOKEN_INVALID("AUTH-003", "登录状态已失效，请重新登录", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
