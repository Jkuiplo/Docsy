package com.google.docsy.feature.workspaceMember;

import com.google.docsy.common.exception.BadRequestException;
import com.google.docsy.common.exception.NotFoundException;
import com.google.docsy.common.security.CurrentUserProvider;
import com.google.docsy.enums.WorkspaceRole;
import com.google.docsy.feature.audit.AuditLogService;
import com.google.docsy.feature.document.DocumentService;
import com.google.docsy.feature.permission.PermissionChecker;
import com.google.docsy.feature.user.User;
import com.google.docsy.feature.workspaceMember.dto.request.ChangeMemberRoleRequest;
import com.google.docsy.feature.workspaceMember.dto.response.WorkspaceMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberService {

    private final WorkspaceMemberRepository memberRepository;
    private final PermissionChecker permissionChecker;
    private final CurrentUserProvider currentUserProvider;
    private final AuditLogService auditLogService;
    private final DocumentService documentService;

    public List<WorkspaceMemberResponse> getMembers(UUID userId, UUID workspaceId) {
        permissionChecker.verifyWorkspaceAccess(userId, workspaceId);

        return memberRepository.findByWorkspaceIdAndRemovedAtIsNull(workspaceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void changeRole(UUID requesterId, UUID workspaceId, UUID targetMemberId, ChangeMemberRoleRequest request) {
        permissionChecker.checkCanManageMembers(requesterId, workspaceId);

        WorkspaceMember targetMember = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        if (!targetMember.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("This user is not a member of this workspace");
        }

        if (targetMember.getRole() == WorkspaceRole.OWNER) {
            throw new BadRequestException("Cannot change the role of the workspace OWNER");
        }

        targetMember.setRole(request.getRole());
        
        User requester = currentUserProvider.getCurrentUser();
        auditLogService.logAction(targetMember.getWorkspace(), requester, "ROLE_CHANGED", "Role changed to " + request.getRole());

        memberRepository.save(targetMember);
    }

    @Transactional
    public void removeMember(UUID requesterId, UUID workspaceId, UUID targetMemberId) {
        permissionChecker.checkCanManageMembers(requesterId, workspaceId);

        WorkspaceMember targetMember = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        if (targetMember.getRole() == WorkspaceRole.OWNER) {
            throw new BadRequestException("Cannot change the role of the workspace OWNER");
        }

        if (targetMember.getRole() == WorkspaceRole.OWNER) {
            throw new BadRequestException("Cannot remove the workspace OWNER");
        }

        // Soft delete per SRS database design constraints
        targetMember.setRemovedAt(LocalDateTime.now());
        memberRepository.save(targetMember);

        User requester = currentUserProvider.getCurrentUser();
        auditLogService.logAction(targetMember.getWorkspace(), requester, "MEMBER_REMOVED", "Removed User " + targetMember.getUser().getEmail());
        
        documentService.unassignRemovedReviewer(workspaceId, targetMember.getUser().getId());
    }

    @Transactional
    public void leaveWorkspace(UUID workspaceId) {
        User requester = currentUserProvider.getCurrentUser();
        
        WorkspaceMember member = memberRepository.findByWorkspaceIdAndUserId(workspaceId, requester.getId())
                .orElseThrow(() -> new NotFoundException("You are not a member of this workspace"));

        if (member.getRole() == WorkspaceRole.OWNER) {
            throw new BadRequestException("The OWNER cannot leave the workspace. Transfer ownership first or delete the workspace.");
        }

        // Soft delete the membership
        member.setRemovedAt(LocalDateTime.now());
        memberRepository.save(member);

        auditLogService.logAction(member.getWorkspace(), requester, "MEMBER_LEFT", requester.getEmail() + " left the workspace");

        // Clean up any documents stuck ON_REVIEW assigned to them
        documentService.unassignRemovedReviewer(workspaceId, requester.getId());
    }

    private WorkspaceMemberResponse mapToResponse(WorkspaceMember member) {
        return WorkspaceMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .email(member.getUser().getEmail())
                .fullName(member.getUser().getFullName())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}