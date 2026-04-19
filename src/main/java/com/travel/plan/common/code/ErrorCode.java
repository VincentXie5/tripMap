package com.travel.plan.common.code;

import org.springframework.http.HttpStatus;

/**
 * 错误码接口
 * 所有业务错误码枚举都应实现此接口
 */
public interface ErrorCode {
    /**
     * 获取错误码
     */
    String getCode();
    
    /**
     * 获取错误消息
     */
    String getMessage();
    
    /**
     * 获取对应的 HTTP 状态码
     */
    HttpStatus getHttpStatus();
}
