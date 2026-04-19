package com.travel.plan.common;

import com.travel.plan.common.code.ErrorCode;
import lombok.Getter;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常
 * 用于抛出业务逻辑错误，被 GlobalExceptionHandler 统一处理
 */
@Getter
public class BusinessException extends RuntimeException {
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    public BusinessException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.httpStatus = errorCode.getHttpStatus();
    }

    public BusinessException(ErrorCode errorCode, Object... args) {
        this.code = errorCode.getCode();
        this.message = MessageFormatter.arrayFormat(errorCode.getMessage(), args).getMessage();
        this.httpStatus = errorCode.getHttpStatus();
    }
}
