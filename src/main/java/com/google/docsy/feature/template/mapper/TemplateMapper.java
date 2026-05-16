package com.google.docsy.feature.template.mapper;

import com.google.docsy.feature.template.Template;
import com.google.docsy.feature.template.dto.response.TemplateResponse;
import org.springframework.stereotype.Component;

@Component
public class TemplateMapper {
    
    public TemplateResponse toResponse(Template template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .title(template.getTitle())
                .htmlContent(template.getHtmlContent())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}