package com.travel.plan.controller.dto;

import lombok.Data;

@Data
public class EmailChangeRequest {
    private String email;
    private String verifyCode;
}
