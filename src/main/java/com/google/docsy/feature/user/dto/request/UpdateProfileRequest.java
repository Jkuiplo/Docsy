package com.google.docsy.feature.user.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String positionTitle;
}