package com.google.docsy.feature.workspace;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.docsy.common.exception.BadRequestException;
import com.google.docsy.common.exception.NotFoundException;
import com.google.docsy.common.util.JoinCodeGenerator;
import com.google.docsy.enums.DocumentStatus;
import com.google.docsy.enums.InvitationStatus;
import com.google.docsy.enums.JoinMode;
import com.google.docsy.enums.WorkspaceRole;
import com.google.docsy.feature.audit.AuditLogService;
import com.google.docsy.feature.document.DocumentRepository;
import com.google.docsy.feature.permission.WorkspaceRolePermissionRepository;
import com.google.docsy.feature.user.User;
import com.google.docsy.feature.workspace.dto.request.CreateWorkspaceRequest;
import com.google.docsy.feature.workspace.dto.request.JoinWorkspaceRequest;
import com.google.docsy.feature.workspace.dto.response.WorkspaceDashboardResponse;
import com.google.docsy.feature.workspace.dto.response.WorkspaceResponse;
import com.google.docsy.feature.workspace.mapper.WorkspaceMapper;
import com.google.docsy.feature.workspaceInvitation.WorkspaceInvitationRepository;
import com.google.docsy.feature.workspaceMember.WorkspaceMember;
import com.google.docsy.feature.workspaceMember.WorkspaceMemberRepository;
import com.google.docsy.feature.permission.Permission;
import com.google.docsy.feature.permission.WorkspaceRolePermission;
import com.google.docsy.feature.permission.PermissionChecker;
import com.google.docsy.common.security.CurrentUserProvider;
import com.google.docsy.feature.workspace.dto.request.UpdateWorkspaceRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final WorkspaceMapper workspaceMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final WorkspaceRolePermissionRepository rolePermissionRepository;
    private final PermissionChecker permissionChecker;
    private final CurrentUserProvider currentUserProvider;
    private final DocumentRepository documentRepository;
    private final WorkspaceInvitationRepository invitationRepository;
    
    @Transactional
    public WorkspaceResponse createWorkspace(User owner, CreateWorkspaceRequest request) {
        Workspace workspace = new Workspace();
        workspace.setName(request.getName());
        workspace.setOwner(owner);
        workspace.setJoinMode(request.getJoinMode() != null ? request.getJoinMode() : JoinMode.INVITE_ONLY);
        
        auditLogService.logAction(workspace, owner, "WORKSPACE_CREATED", "Workspace initialized");

        if (workspace.getJoinMode() == JoinMode.PASSWORD_AND_INVITE && request.getJoinPassword() != null) {
            workspace.setJoinPasswordHash(passwordEncoder.encode(request.getJoinPassword()));
        }

        String newJoinCode;
        do {
            newJoinCode = JoinCodeGenerator.generate();
        } while (workspaceRepository.existsByJoinCode(newJoinCode));
        workspace.setJoinCode(newJoinCode);

        workspace = workspaceRepository.save(workspace);

        WorkspaceMember ownerMember = new WorkspaceMember();
        ownerMember.setWorkspace(workspace);
        ownerMember.setUser(owner);
        ownerMember.setRole(WorkspaceRole.OWNER);
        ownerMember.setJoinedAt(LocalDateTime.now());
        memberRepository.save(ownerMember);

        initializeDefaultPermissions(workspace);

        return workspaceMapper.toResponse(workspace);
    }

    public WorkspaceResponse getWorkspaceById(UUID workspaceId) {
        User currentUser = currentUserProvider.getCurrentUser();
        
        permissionChecker.verifyWorkspaceAccess(currentUser.getId(), workspaceId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("Workspace not found"));

        return workspaceMapper.toResponse(workspace);
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(UUID workspaceId, UpdateWorkspaceRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();

        permissionChecker.checkCanManageWorkspace(currentUser.getId(), workspaceId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("Workspace not found"));

        StringBuilder auditDetails = new StringBuilder("Updated settings: ");

        if (request.getName() != null && !request.getName().isBlank()) {
            workspace.setName(request.getName());
            auditDetails.append("Name. ");
        }

        if (request.getJoinMode() != null) {
            workspace.setJoinMode(request.getJoinMode());
            auditDetails.append("JoinMode set to ").append(request.getJoinMode()).append(". ");
        }

        if (request.getJoinPassword() != null && !request.getJoinPassword().isBlank() 
            && workspace.getJoinMode() == JoinMode.PASSWORD_AND_INVITE) {
            
            workspace.setJoinPasswordHash(passwordEncoder.encode(request.getJoinPassword()));
            auditDetails.append("Join Password changed. ");
        }

        workspace = workspaceRepository.save(workspace);

        auditLogService.logAction(workspace, currentUser, "WORKSPACE_UPDATED", auditDetails.toString());

        return workspaceMapper.toResponse(workspace);
    }

    @Transactional
    public WorkspaceResponse joinWorkspace(User user, JoinWorkspaceRequest request) {
        Workspace workspace = workspaceRepository.findByJoinCode(request.getJoinCode())
                .orElseThrow(() -> new NotFoundException("Invalid join code"));

        java.util.Optional<WorkspaceMember> existingMemberOpt = 
                memberRepository.findByWorkspaceIdAndUserId(workspace.getId(), user.getId());

        if (existingMemberOpt.isPresent()) {
            WorkspaceMember existing = existingMemberOpt.get();
            if (existing.getRemovedAt() == null) {
                throw new BadRequestException("You are already an active member of this workspace");
            } else {
                existing.setRemovedAt(null);
                existing.setRole(WorkspaceRole.USER); 
                existing.setJoinedAt(LocalDateTime.now());
                memberRepository.save(existing);
                
                auditLogService.logAction(workspace, user, "MEMBER_REJOINED", user.getEmail() + " rejoined the workspace");
                return workspaceMapper.toResponse(workspace);
            }
        }
        
        if (workspace.getJoinMode() == JoinMode.INVITE_ONLY) {
            throw new BadRequestException("This workspace is invite-only. You must receive an email invitation.");
        }

        if (workspace.getJoinMode() == JoinMode.PASSWORD_AND_INVITE) {
            if (request.getJoinPassword() == null || !passwordEncoder.matches(request.getJoinPassword(), workspace.getJoinPasswordHash())) {
                throw new BadRequestException("Invalid workspace password");
            }
        }

        WorkspaceMember newMember = new WorkspaceMember();
        newMember.setWorkspace(workspace);
        newMember.setUser(user);
        newMember.setRole(WorkspaceRole.USER);
        newMember.setJoinedAt(LocalDateTime.now());
        memberRepository.save(newMember);

        auditLogService.logAction(workspace, user, "MEMBER_JOINED", user.getEmail() + " joined the workspace");

        return workspaceMapper.toResponse(workspace);
    }

    public List<WorkspaceResponse> getMyWorkspaces(User user) {
        return memberRepository.findAll().stream() 
                .filter(m -> m.getUser().getId().equals(user.getId()) && m.getRemovedAt() == null)
                .map(m -> workspaceMapper.toResponse(m.getWorkspace()))
                .collect(Collectors.toList());
    }
    
    private void initializeDefaultPermissions(Workspace workspace) {
        for (WorkspaceRole role : WorkspaceRole.values()) {
            if (role == WorkspaceRole.OWNER) continue; 

            for (Permission perm : Permission.values()) {
                WorkspaceRolePermission rolePerm = new WorkspaceRolePermission();
                rolePerm.setWorkspace(workspace);
                rolePerm.setRole(role);
                rolePerm.setPermission(perm);

                if (role == WorkspaceRole.ADMIN) {
                    rolePerm.setEnabled(true); 
                } else if (role == WorkspaceRole.REVIEWER) {
                    boolean isReviewAction = (perm == Permission.VIEW_ALL_DOCUMENTS || perm == Permission.REVIEW_ASSIGNED_DOCUMENTS);
                    rolePerm.setEnabled(isReviewAction);
                } else if (role == WorkspaceRole.USER) {
                    boolean isUserAction = (perm == Permission.CREATE_BLANK_DOCUMENTS || perm == Permission.CREATE_FROM_TEMPLATE);
                    rolePerm.setEnabled(isUserAction);
                }
                
                rolePermissionRepository.save(rolePerm);
            }
        }
    }
    public WorkspaceDashboardResponse getWorkspaceDashboard(UUID workspaceId) {
        User currentUser = currentUserProvider.getCurrentUser();

        WorkspaceMember member = permissionChecker.verifyWorkspaceAccess(currentUser.getId(), workspaceId);
        Workspace workspace = member.getWorkspace();

        long drafts = documentRepository.countByWorkspaceIdAndStatus(workspaceId, DocumentStatus.DRAFT);
        long rejected = documentRepository.countByWorkspaceIdAndStatus(workspaceId, DocumentStatus.REJECTED);
        long onReview = documentRepository.countByWorkspaceIdAndStatus(workspaceId, DocumentStatus.ON_REVIEW);
        long approved = documentRepository.countByWorkspaceIdAndStatus(workspaceId, DocumentStatus.APPROVED);
        long archived = documentRepository.countByWorkspaceIdAndStatus(workspaceId, DocumentStatus.ARCHIVED);

        long pendingInvites = invitationRepository.countByWorkspaceIdAndStatus(workspaceId, InvitationStatus.PENDING);

        long waitingForMe = documentRepository.countByWorkspaceIdAndAssignedReviewerIdAndStatus(
                workspaceId, currentUser.getId(), DocumentStatus.ON_REVIEW);

        return WorkspaceDashboardResponse.builder()
                .workspaceId(workspace.getId())
                .workspaceName(workspace.getName())
                .myRole(member.getRole())
                .documentStats(WorkspaceDashboardResponse.DocumentStats.builder()
                        .draft(drafts)
                        .rejected(rejected)
                        .onReview(onReview)
                        .approved(approved)
                        .archived(archived)
                        .build())
                .pendingInvitations(pendingInvites)
                .waitingForMyReview(waitingForMe)
                .build();
    }
}