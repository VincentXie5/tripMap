package com.travel.plan.common;

import com.travel.plan.common.code.BaseCode;
import com.travel.plan.common.code.ErrorCode;
import lombok.Data;

/**
 * 统一响应封装类
 * 所有Controller接口均返回此格式，便于前端统一处理
 *
 * @param <T> 泛型，承载实际业务数据
 */
@Data
public class ApiResult<T> {
    private String code;        // 状态码：200成功，400参数错误，401未认证，404未找到，500服务器错误
    private String message;  // 消息：描述本次响应的文字信息
    private T data;          // 数据：泛型，承载实际业务数据
    private long timestamp;  // 时间戳：响应时间毫秒值

    private ApiResult(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }

    private ApiResult(ErrorCode errorCode, T data) {
        this(errorCode.getCode(), errorCode.getMessage(), data);
    }

    private ApiResult(ErrorCode errorCode, String message, T data) {
        this(errorCode.getCode(), message, data);
    }

    private ApiResult(String code, String message) {
        this(code, message, null);
    }

    private ApiResult(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应，默认消息"操作成功"
     */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(BaseCode.SUCCESS, data);
    }

    /**
     * 成功响应，自定义消息
     */
    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(BaseCode.SUCCESS, message, data);
    }

    /**
     * 错误响应，自定义状态码和消息
     */
    public static <T> ApiResult<T> error(String code, String message) {
        return new ApiResult<>(code, message);
    }

    /**
     * 错误响应，errorCode
     */
    public static <T> ApiResult<T> error(ErrorCode errorCode) {
        return new ApiResult<>(errorCode);
    }
}
