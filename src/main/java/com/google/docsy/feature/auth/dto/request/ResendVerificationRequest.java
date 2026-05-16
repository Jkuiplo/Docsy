package com.google.docsy.feature.auth.dto.request;

import lombok.Data;

@Data
public class ResendVerificationRequest {
    private String email;
}