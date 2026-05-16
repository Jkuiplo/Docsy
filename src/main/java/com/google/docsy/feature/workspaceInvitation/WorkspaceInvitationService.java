package com.google.docsy.feature.workspaceInvitation;

import com.google.docsy.common.exception.BadRequestException;
import com.google.docsy.common.exception.NotFoundException;
import com.google.docsy.enums.InvitationStatus;
import com.google.docsy.feature.notification.NotificationService;
import com.google.docsy.feature.permission.PermissionChecker;
import com.google.docsy.feature.user.User;
import com.google.docsy.feature.workspace.Workspace;
import com.google.docsy.feature.workspace.WorkspaceRepository;
import com.google.docsy.feature.workspaceInvitation.dto.request.InviteMemberRequest;
import com.google.docsy.feature.workspaceInvitation.dto.response.WorkspaceInvitationResponse;
import com.google.docsy.feature.workspaceMember.WorkspaceMember;
import com.google.docsy.feature.workspaceMember.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceInvitationService {

    private final WorkspaceInvitationRepository invitationRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final PermissionChecker permissionChecker;
    private final NotificationService notificationService;

    @Transactional
    public WorkspaceInvitationResponse inviteMember(User requester, UUID workspaceId, InviteMemberRequest request) {
        permissionChecker.checkCanManageMembers(requester.getId(), workspaceId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("Workspace not found"));

        WorkspaceInvitation invitation = new WorkspaceInvitation();
        invitation.setWorkspace(workspace);
        invitation.setEmail(request.getEmail());
        invitation.setRole(request.getRole());
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setInvitedBy(requester);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7)); // Invites expire in 7 days
        invitation.setStatus(InvitationStatus.PENDING);

        invitationRepository.save(invitation);

        System.out.println("--- VIBE CODING DEBUG: EMAIL INVITATION ---");
        System.out.println("To: " + request.getEmail());
        System.out.println("Token: " + invitation.getToken());
        System.out.println("-------------------------------------------");

        notificationService.sendInvitationEmail(
            request.getEmail(), 
            workspace.getName(), 
            invitation.getToken()
        );

        return mapToResponse(invitation);
    }

    public List<WorkspaceInvitationResponse> getInvitations(UUID requesterId, UUID workspaceId) {
        permissionChecker.checkCanManageMembers(requesterId, workspaceId);

        return invitationRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void acceptInvitation(User acceptingUser, String token) {
        WorkspaceInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Invalid invitation token"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Invitation is no longer valid");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new BadRequestException("Invitation has expired");
        }

        // Prevent double-joining
        boolean alreadyMember = memberRepository.findByWorkspaceIdAndUserId(
                invitation.getWorkspace().getId(), acceptingUser.getId()).isPresent();
        
        if (alreadyMember) {
            throw new BadRequestException("You are already a member of this workspace");
        }

        // 1. Create the new member
        WorkspaceMember newMember = new WorkspaceMember();
        newMember.setWorkspace(invitation.getWorkspace());
        newMember.setUser(acceptingUser);
        newMember.setRole(invitation.getRole());
        newMember.setJoinedAt(LocalDateTime.now());
        memberRepository.save(newMember);

        // 2. Mark invite as accepted
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedBy(acceptingUser);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
    }

    private WorkspaceInvitationResponse mapToResponse(WorkspaceInvitation invitation) {
        return WorkspaceInvitationResponse.builder()
                .id(invitation.getId())
                .email(invitation.getEmail())
                .role(invitation.getRole())
                .status(invitation.getStatus())
                .expiresAt(invitation.getExpiresAt())
                .build();
    }
}