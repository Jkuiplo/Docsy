package com.google.docsy.feature.audit.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AuditLogResponse {
    private UUID id;
    private String action;
    private String details;
    private UUID actorId;
    private String actorName; // "System" if actorId is null
    private LocalDateTime timestamp;
}