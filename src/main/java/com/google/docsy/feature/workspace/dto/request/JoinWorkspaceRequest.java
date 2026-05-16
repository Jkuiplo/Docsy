package com.google.docsy.feature.workspace.dto.request;

import lombok.Data;

@Data
public class JoinWorkspaceRequest {
    private String joinCode;
    private String joinPassword; 
}