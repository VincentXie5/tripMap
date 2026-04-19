package com.travel.plan.common.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 基础/通用错误码
 */
@Getter
@AllArgsConstructor
public enum BaseCode implements ErrorCode {
    SUCCESS("0", "操作成功", HttpStatus.OK),
    PARAM_ERROR("PARAM-001", "参数错误", HttpStatus.BAD_REQUEST),
    SYSTEM_ERROR("SYS-001", "系统内部错误", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
