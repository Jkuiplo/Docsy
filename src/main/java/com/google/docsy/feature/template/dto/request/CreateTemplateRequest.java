package com.google.docsy.feature.template.dto.request;

import lombok.Data;

@Data
public class CreateTemplateRequest {
    private String title;
    private String htmlContent;
}