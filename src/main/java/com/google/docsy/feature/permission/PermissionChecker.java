package com.google.docsy.feature.permission;

import com.google.docsy.common.exception.ForbiddenException;
import com.google.docsy.enums.WorkspaceRole;
import com.google.docsy.feature.workspaceMember.WorkspaceMember;
import com.google.docsy.feature.workspaceMember.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PermissionChecker {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRolePermissionRepository rolePermissionRepository;

    /**
     * CORE SECURITY: Proves the user is a member of the workspace.
     * Use this for basic endpoints (like viewing the dashboard) to prevent IDOR.
     */
    public WorkspaceMember verifyWorkspaceAccess(UUID userId, UUID workspaceId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new ForbiddenException("You do not have access to this workspace"));
    }

    /**
     * FEATURE SECURITY: Checks if the user's role allows a specific action.
     */
    public void checkPermission(UUID userId, UUID workspaceId, Permission requiredPermission) {
        // 1. Check if they are in the workspace at all
        WorkspaceMember member = verifyWorkspaceAccess(userId, workspaceId);
        
        // 2. OWNER always has all permissions 
        if (member.getRole() == WorkspaceRole.OWNER) {
            return; 
        }

        // 3. Look up the specific role configuration for this workspace
        WorkspaceRolePermission rolePerm = rolePermissionRepository
                .findByWorkspaceIdAndRoleAndPermission(workspaceId, member.getRole(), requiredPermission)
                .orElseThrow(() -> new ForbiddenException("Permission setting is missing for this role"));

        // 4. Reject if the owner disabled this permission
        if (!rolePerm.isEnabled()) {
            throw new ForbiddenException("Your role does not have permission to perform this action");
        }
    }

    // --- Clean Convenience Methods ---
    
    public void checkCanManageTemplates(UUID userId, UUID workspaceId) {
        checkPermission(userId, workspaceId, Permission.CREATE_TEMPLATES);
    }

    public void checkCanManageMembers(UUID userId, UUID workspaceId) {
        checkPermission(userId, workspaceId, Permission.MANAGE_MEMBERS);
    }
    
    public void checkCanCreateBlankDocument(UUID userId, UUID workspaceId) {
        checkPermission(userId, workspaceId, Permission.CREATE_BLANK_DOCUMENTS);
    }
    public void checkCanCreateFromTemplate(UUID userId, UUID workspaceId) {
        checkPermission(userId, workspaceId, Permission.CREATE_FROM_TEMPLATE);
    }
    public void checkCanEditAnyDocument(UUID userId, UUID workspaceId) {
        checkPermission(userId, workspaceId, Permission.EDIT_ALL_DOCUMENTS);
    }
    public void checkCanManageWorkspace(UUID userId, UUID workspaceId) {
        checkPermission(userId, workspaceId, Permission.MANAGE_WORKSPACE);
    }
}