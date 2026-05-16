package com.google.docsy.feature.workspaceInvitation;

import com.google.docsy.common.security.UserPrincipal;
import com.google.docsy.feature.workspaceInvitation.dto.request.AcceptInvitationRequest;
import com.google.docsy.feature.workspaceInvitation.dto.request.InviteMemberRequest;
import com.google.docsy.feature.workspaceInvitation.dto.response.WorkspaceInvitationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WorkspaceInvitationController {

    private final WorkspaceInvitationService invitationService;

    // Workspace-specific endpoints (Managing Invites)
    @PostMapping("/workspaces/{workspaceId}/invitations")
    public ResponseEntity<WorkspaceInvitationResponse> inviteMember(
            @PathVariable UUID workspaceId,
            @RequestBody InviteMemberRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(invitationService.inviteMember(principal.getUser(), workspaceId, request));
    }

    @GetMapping("/workspaces/{workspaceId}/invitations")
    public ResponseEntity<List<WorkspaceInvitationResponse>> getInvitations(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(invitationService.getInvitations(principal.getUser().getId(), workspaceId));
    }

    // Global endpoint (Accepting Invites) - Does not require workspaceId in path
    @PostMapping("/invitations/accept")
    public ResponseEntity<Void> acceptInvitation(
            @RequestBody AcceptInvitationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        invitationService.acceptInvitation(principal.getUser(), request.getToken());
        return ResponseEntity.ok().build();
    }
}