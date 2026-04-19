package com.travel.plan.controller.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String nickname;
    private String password;
    private String verifyCode;
}
