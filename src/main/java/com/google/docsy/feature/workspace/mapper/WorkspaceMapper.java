package com.google.docsy.feature.workspace.mapper;

import com.google.docsy.feature.workspace.Workspace;
import com.google.docsy.feature.workspace.dto.response.WorkspaceResponse;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceMapper {
    public WorkspaceResponse toResponse(Workspace workspace) {
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .ownerId(workspace.getOwner().getId())
                .ownerName(workspace.getOwner().getFullName())
                .joinCode(workspace.getJoinCode())
                .joinMode(workspace.getJoinMode())
                .createdAt(workspace.getCreatedAt())
                .build();
    }
}