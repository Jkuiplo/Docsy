package com.google.docsy.feature.workspaceMember.dto.request;

import com.google.docsy.enums.WorkspaceRole;
import lombok.Data;

@Data
public class ChangeMemberRoleRequest {
    private WorkspaceRole role;
}