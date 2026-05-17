package com.google.docsy.feature.permission.dto.request;

import com.google.docsy.enums.WorkspaceRole;
import com.google.docsy.feature.permission.Permission;
import lombok.Data;

@Data
public class UpdateRolePermissionRequest {
    private WorkspaceRole role;
    private Permission permission;
    private boolean enabled;
}