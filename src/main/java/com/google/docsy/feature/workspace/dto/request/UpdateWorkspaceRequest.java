package com.google.docsy.feature.workspace.dto.request;

import com.google.docsy.enums.JoinMode;
import lombok.Data;

@Data
public class UpdateWorkspaceRequest {
    private String name;
    private JoinMode joinMode;
    private String joinPassword; 
}