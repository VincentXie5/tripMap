package com.travel.plan.common.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 每日行程模块错误码
 */
@Getter
@AllArgsConstructor
public enum DailyPlanCode implements ErrorCode {
    NOT_FOUND("DAILY-PLAN-001", "未找到每日行程, id:{}", HttpStatus.NOT_FOUND),
    DATE_MUST_IN_RANGE("DAILY-PLAN-002", "行程日期必须在旅行计划时间范围内({} - {})", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
