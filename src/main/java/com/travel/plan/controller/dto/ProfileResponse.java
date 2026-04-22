package com.travel.plan.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private Long id;
    private String email;
    private String nickname;
    private String avatarType;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String avatarUrl;
}
