package com.google.docsy.feature.permission;

import com.google.docsy.enums.WorkspaceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRolePermissionRepository extends JpaRepository<WorkspaceRolePermission, UUID> {
    Optional<WorkspaceRolePermission> findByWorkspaceIdAndRoleAndPermission(UUID workspaceId, WorkspaceRole role, Permission permission);
    Optional<WorkspaceRolePermission> findByWorkspaceId(UUID workspaceId);
}