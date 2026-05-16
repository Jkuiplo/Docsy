package com.google.docsy.feature.workspaceMember;

import com.google.docsy.common.exception.BadRequestException;
import com.google.docsy.common.exception.NotFoundException;
import com.google.docsy.enums.WorkspaceRole;
import com.google.docsy.feature.permission.PermissionChecker;
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

        if (targetMember.getRole() == WorkspaceRole.OWNER) {
            throw new BadRequestException("Cannot change the role of the workspace OWNER");
        }

        targetMember.setRole(request.getRole());
        memberRepository.save(targetMember);
    }

    @Transactional
    public void removeMember(UUID requesterId, UUID workspaceId, UUID targetMemberId) {
        permissionChecker.checkCanManageMembers(requesterId, workspaceId);

        WorkspaceMember targetMember = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        if (targetMember.getRole() == WorkspaceRole.OWNER) {
            throw new BadRequestException("Cannot remove the workspace OWNER");
        }

        // Soft delete per SRS database design constraints
        targetMember.setRemovedAt(LocalDateTime.now());
        memberRepository.save(targetMember);

        // TODO: In the future, trigger DocumentService to unassign this user from active ON_REVIEW documents
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