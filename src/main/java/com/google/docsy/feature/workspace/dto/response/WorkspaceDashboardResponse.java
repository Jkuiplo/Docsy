package com.google.docsy.feature.workspace.dto.response;

import com.google.docsy.enums.WorkspaceRole;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class WorkspaceDashboardResponse {
    private UUID workspaceId;
    private String workspaceName;
    private WorkspaceRole myRole;
    private DocumentStats documentStats;
    private long pendingInvitations;
    private long waitingForMyReview;

    @Data
    @Builder
    public static class DocumentStats {
        private long draft;
        private long onReview;
        private long approved;
        private long archived;
        private long rejected; 
    }
}