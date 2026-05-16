package com.google.docsy.feature.workspace;

import com.google.docsy.common.security.UserPrincipal;
import com.google.docsy.feature.workspace.dto.request.CreateWorkspaceRequest;
import com.google.docsy.feature.workspace.dto.request.JoinWorkspaceRequest;
import com.google.docsy.feature.workspace.dto.response.WorkspaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            @RequestBody CreateWorkspaceRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(workspaceService.createWorkspace(principal.getUser(), request));
    }

    @PostMapping("/join")
    public ResponseEntity<WorkspaceResponse> joinWorkspace(
            @RequestBody JoinWorkspaceRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(workspaceService.joinWorkspace(principal.getUser(), request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<WorkspaceResponse>> getMyWorkspaces(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(workspaceService.getMyWorkspaces(principal.getUser()));
    }
}