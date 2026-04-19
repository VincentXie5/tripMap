package com.travel.plan.common.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 旅行计划模块错误码
 */
@Getter
@AllArgsConstructor
public enum TravelPlanCode implements ErrorCode {
    NOT_FOUND("TRAVEL-PLAN-001", "未找到旅行计划, id:{}", HttpStatus.NOT_FOUND),
    END_DATE_LARGER_THAN_START_DATE("TRAVEL-PLAN-002", "结束日期必须大于等于开始日期", HttpStatus.BAD_REQUEST);


    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
