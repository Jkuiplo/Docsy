package com.google.docsy.feature.template.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TemplateResponse {
    private UUID id;
    private String title;
    private String htmlContent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}