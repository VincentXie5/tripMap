package com.travel.plan.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.security.Principal;

@Data
@AllArgsConstructor
public class UserPrincipal implements Principal {
    private Long userId;
    private String email;
    
    @Override
    public String getName() {
        return email;
    }
}
