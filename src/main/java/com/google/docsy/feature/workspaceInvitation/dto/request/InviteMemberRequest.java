package com.google.docsy.feature.workspaceInvitation.dto.request;

import com.google.docsy.enums.WorkspaceRole;
import lombok.Data;

@Data
public class InviteMemberRequest {
    private String email;
    private WorkspaceRole role;
}