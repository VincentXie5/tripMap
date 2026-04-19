package com.travel.plan.common.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 用户模块错误码
 */
@Getter
@AllArgsConstructor
public enum UserCode implements ErrorCode {
    USER_NOT_FOUND("USER-001", "用户不存在", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS("USER-002", "该邮箱已被注册", HttpStatus.CONFLICT),
    NICKNAME_ALREADY_EXISTS("USER-003", "该昵称已被使用", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
