package com.google.docsy.feature.documentVersion.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DocumentVersionResponse {
    private UUID id;
    private Integer versionNumber;
    private String titleSnapshot;
    private String contentSnapshot;
    private UUID createdById;
    private String createdByName;
    private LocalDateTime createdAt;
}