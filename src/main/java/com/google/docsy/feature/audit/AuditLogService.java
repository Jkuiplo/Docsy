package com.google.docsy.feature.audit;

import com.google.docsy.feature.audit.dto.AuditLogResponse;
import com.google.docsy.feature.permission.PermissionChecker;
import com.google.docsy.feature.user.User;
import com.google.docsy.feature.workspace.Workspace;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final PermissionChecker permissionChecker;

    // Use @Async so logging doesn't slow down the main API response
    @Async
    public void logAction(Workspace workspace, User actor, String action, String details) {
        AuditLog log = new AuditLog();
        log.setWorkspace(workspace);
        log.setActor(actor);
        log.setAction(action);
        log.setDetails(details);
        
        auditLogRepository.save(log);
    }

    public List<AuditLogResponse> getWorkspaceAuditLogs(UUID requesterId, UUID workspaceId) {
        // Gatekeeper: Only users who can manage members (Admins/Owners) should see audit logs
        permissionChecker.checkCanManageMembers(requesterId, workspaceId);

        return auditLogRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .action(log.getAction())
                .details(log.getDetails())
                .actorId(log.getActor() != null ? log.getActor().getId() : null)
                .actorName(log.getActor() != null ? log.getActor().getFullName() : "System")
                .timestamp(log.getCreatedAt())
                .build();
    }
}