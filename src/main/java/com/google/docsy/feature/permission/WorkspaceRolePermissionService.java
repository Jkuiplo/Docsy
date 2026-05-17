package com.google.docsy.feature.permission;

import com.google.docsy.common.exception.NotFoundException;
import com.google.docsy.enums.WorkspaceRole;
import com.google.docsy.feature.audit.AuditLogService;
import com.google.docsy.feature.permission.dto.request.UpdateRolePermissionRequest;
import com.google.docsy.feature.permission.dto.response.MyPermissionsResponse;
import com.google.docsy.feature.permission.dto.response.RolePermissionResponse;
import com.google.docsy.feature.user.User;
import com.google.docsy.feature.workspaceMember.WorkspaceMember;

import io.jsonwebtoken.lang.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceRolePermissionService {

    private final WorkspaceRolePermissionRepository permissionRepository;
    private final PermissionChecker permissionChecker;
    private final AuditLogService auditLogService;

    public List<RolePermissionResponse> getWorkspacePermissions(UUID requesterId, UUID workspaceId) {
        permissionChecker.checkCanManageMembers(requesterId, workspaceId);

        return permissionRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updatePermission(User requester, UUID workspaceId, UpdateRolePermissionRequest request) {
        permissionChecker.checkCanManageMembers(requester.getId(), workspaceId);

        if (request.getRole() == WorkspaceRole.OWNER) {
            throw new IllegalArgumentException("OWNER permissions cannot be modified");
        }

        WorkspaceRolePermission rolePerm = permissionRepository
                .findByWorkspaceIdAndRoleAndPermission(workspaceId, request.getRole(), request.getPermission())
                .orElseThrow(() -> new NotFoundException("Permission setting not found"));

        rolePerm.setEnabled(request.isEnabled());
        permissionRepository.save(rolePerm);

        String status = request.isEnabled() ? "Enabled" : "Disabled";
        auditLogService.logAction(rolePerm.getWorkspace(), requester, "PERMISSION_UPDATED", 
                status + " " + request.getPermission() + " for role " + request.getRole());
    }

    private RolePermissionResponse mapToResponse(WorkspaceRolePermission perm) {
        return RolePermissionResponse.builder()
                .id(perm.getId())
                .role(perm.getRole())
                .permission(perm.getPermission())
                .enabled(perm.isEnabled())
                .build();
    }

    public MyPermissionsResponse getMyPermissions(UUID userId, UUID workspaceId) {
        WorkspaceMember member = permissionChecker.verifyWorkspaceAccess(userId, workspaceId);
        WorkspaceRole myRole = member.getRole();

        List<Permission> activePermissions;

        if (myRole == WorkspaceRole.OWNER) {
            activePermissions = Arrays.asList(Permission.values());
        } else {
            activePermissions = permissionRepository.findByWorkspaceIdAndRole(workspaceId, myRole)
                    .stream()
                    .filter(WorkspaceRolePermission::isEnabled)
                    .map(WorkspaceRolePermission::getPermission)
                    .collect(Collectors.toList());
        }

        return MyPermissionsResponse.builder()
                .role(myRole)
                .permissions(activePermissions)
                .build();
    }
}