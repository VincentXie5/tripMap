package com.travel.plan.common;

import lombok.Data;

/**
 * 统一响应封装类
 * 所有Controller接口均返回此格式，便于前端统一处理
 *
 * @param <T> 泛型，承载实际业务数据
 */
@Data
public class ApiResult<T> {
    private int code;        // 状态码：200成功，400参数错误，401未认证，404未找到，500服务器错误
    private String message;  // 消息：描述本次响应的文字信息
    private T data;          // 数据：泛型，承载实际业务数据
    private long timestamp;  // 时间戳：响应时间毫秒值

    private ApiResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应，默认消息"操作成功"
     */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(200, "操作成功", data);
    }

    /**
     * 成功响应，自定义消息
     */
    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(200, message, data);
    }

    /**
     * 错误响应，自定义状态码和消息
     */
    public static <T> ApiResult<T> error(int code, String message) {
        return new ApiResult<>(code, message, null);
    }

    /**
     * 错误响应，默认500状态码
     */
    public static <T> ApiResult<T> error(String message) {
        return new ApiResult<>(500, message, null);
    }

    /**
     * 快捷方法，400状态码（参数校验失败）
     */
    public static <T> ApiResult<T> badRequest(String message) {
        return error(400, message);
    }

    /**
     * 快捷方法，404状态码（资源不存在）
     */
    public static <T> ApiResult<T> notFound(String message) {
        return error(404, message);
    }

    /**
     * 快捷方法，401状态码（未认证）
     */
    public static <T> ApiResult<T> unauthorized(String message) {
        return error(401, message);
    }
}
