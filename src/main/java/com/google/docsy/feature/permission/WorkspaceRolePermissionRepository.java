package com.google.docsy.feature.permission;

import com.google.docsy.enums.WorkspaceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRolePermissionRepository extends JpaRepository<WorkspaceRolePermission, UUID> {
    Optional<WorkspaceRolePermission> findByWorkspaceIdAndRoleAndPermission(UUID workspaceId, WorkspaceRole role, Permission permission);
    List<WorkspaceRolePermission> findByWorkspaceId(UUID workspaceId);
    List<WorkspaceRolePermission> findByWorkspaceIdAndRole(UUID workspaceId, WorkspaceRole role);
}