package com.google.docsy.feature.permission.dto.response;

import com.google.docsy.enums.WorkspaceRole;
import com.google.docsy.feature.permission.Permission;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class MyPermissionsResponse {
    private WorkspaceRole role;
    private List<Permission> permissions;
}