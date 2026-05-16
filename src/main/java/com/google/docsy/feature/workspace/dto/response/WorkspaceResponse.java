package com.google.docsy.feature.workspace.dto.response;

import com.google.docsy.enums.JoinMode;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class WorkspaceResponse {
    private UUID id;
    private String name;
    private UUID ownerId;
    private String ownerName;
    private String joinCode;
    private JoinMode joinMode;
    private LocalDateTime createdAt;
}