package com.google.docsy.feature.workspaceMember;

import com.google.docsy.common.security.UserPrincipal;
import com.google.docsy.feature.workspaceMember.dto.request.ChangeMemberRoleRequest;
import com.google.docsy.feature.workspaceMember.dto.response.WorkspaceMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/members")
@RequiredArgsConstructor
public class WorkspaceMemberController {

    private final WorkspaceMemberService memberService;

    @GetMapping
    public ResponseEntity<List<WorkspaceMemberResponse>> getMembers(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(memberService.getMembers(principal.getUser().getId(), workspaceId));
    }

    @PatchMapping("/{memberId}/role")
    public ResponseEntity<Void> changeRole(
            @PathVariable UUID workspaceId,
            @PathVariable UUID memberId,
            @RequestBody ChangeMemberRoleRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        memberService.changeRole(principal.getUser().getId(), workspaceId, memberId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID workspaceId,
            @PathVariable UUID memberId,
            @AuthenticationPrincipal UserPrincipal principal) {
        memberService.removeMember(principal.getUser().getId(), workspaceId, memberId);
        return ResponseEntity.ok().build();
    }
}