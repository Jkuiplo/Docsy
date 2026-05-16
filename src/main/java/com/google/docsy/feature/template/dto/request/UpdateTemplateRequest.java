package com.google.docsy.feature.template.dto.request;

import lombok.Data;

@Data
public class UpdateTemplateRequest {
    private String title;
    private String htmlContent;
}