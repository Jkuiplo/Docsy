package com.google.docsy.feature.permission;

import com.google.docsy.common.security.CurrentUserProvider;
import com.google.docsy.feature.permission.dto.request.UpdateRolePermissionRequest;
import com.google.docsy.feature.permission.dto.response.RolePermissionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/permissions")
@RequiredArgsConstructor
public class WorkspaceRolePermissionController {

    private final WorkspaceRolePermissionService permissionService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<List<RolePermissionResponse>> getPermissions(@PathVariable UUID workspaceId) {
        return ResponseEntity.ok(permissionService.getWorkspacePermissions(currentUserProvider.getCurrentUserId(), workspaceId));
    }

    @PatchMapping
    public ResponseEntity<Void> updatePermission(
            @PathVariable UUID workspaceId,
            @RequestBody UpdateRolePermissionRequest request) {
        permissionService.updatePermission(currentUserProvider.getCurrentUser(), workspaceId, request);
        return ResponseEntity.ok().build();
    }
}