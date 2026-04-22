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
    NICKNAME_ALREADY_EXISTS("USER-003", "该昵称已被使用", HttpStatus.CONFLICT),
    INVALID_OLD_PASSWORD("USER-004", "原密码错误", HttpStatus.UNAUTHORIZED),
    EMAIL_NOT_CHANGED("USER-005", "邮箱未变更", HttpStatus.BAD_REQUEST),
    INVALID_AVATAR_TYPE("USER-006", "无效的头像类型", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
