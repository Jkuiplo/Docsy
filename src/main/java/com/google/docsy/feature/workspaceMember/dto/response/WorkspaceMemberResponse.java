package com.google.docsy.feature.workspaceMember.dto.response;

import com.google.docsy.enums.WorkspaceRole;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class WorkspaceMemberResponse {
    private UUID id;         // Member ID
    private UUID userId;     // Actual User ID
    private String email;
    private String fullName;
    private WorkspaceRole role;
    private LocalDateTime joinedAt;
}