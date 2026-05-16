package com.google.docsy.feature.workspaceInvitation.dto.response;

import com.google.docsy.enums.InvitationStatus;
import com.google.docsy.enums.WorkspaceRole;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class WorkspaceInvitationResponse {
    private UUID id;
    private String email;
    private WorkspaceRole role;
    private InvitationStatus status;
    private LocalDateTime expiresAt;
}