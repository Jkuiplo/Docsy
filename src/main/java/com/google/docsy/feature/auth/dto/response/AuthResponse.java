package com.google.docsy.feature.auth.dto.response;

import com.google.docsy.feature.user.dto.UserResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private UserResponse user;
}