package com.google.docsy.feature.workspace;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.docsy.common.exception.BadRequestException;
import com.google.docsy.common.exception.NotFoundException;
import com.google.docsy.common.util.JoinCodeGenerator;
import com.google.docsy.enums.JoinMode;
import com.google.docsy.enums.WorkspaceRole;
import com.google.docsy.feature.audit.AuditLogService;
import com.google.docsy.feature.permission.WorkspaceRolePermissionRepository;
import com.google.docsy.feature.user.User;
import com.google.docsy.feature.workspace.dto.request.CreateWorkspaceRequest;
import com.google.docsy.feature.workspace.dto.request.JoinWorkspaceRequest;
import com.google.docsy.feature.workspace.dto.response.WorkspaceResponse;
import com.google.docsy.feature.workspace.mapper.WorkspaceMapper;
import com.google.docsy.feature.workspaceMember.WorkspaceMember;
import com.google.docsy.feature.workspaceMember.WorkspaceMemberRepository;
import com.google.docsy.feature.permission.Permission;
import com.google.docsy.feature.permission.WorkspaceRolePermission;

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
    
    @Transactional
    public WorkspaceResponse createWorkspace(User owner, CreateWorkspaceRequest request) {
        // 1. Create Workspace
        Workspace workspace = new Workspace();
        workspace.setName(request.getName());
        workspace.setOwner(owner);
        workspace.setJoinMode(request.getJoinMode() != null ? request.getJoinMode() : JoinMode.INVITE_ONLY);
        
        auditLogService.logAction(workspace, owner, "WORKSPACE_CREATED", "Workspace initialized");

        if (workspace.getJoinMode() == JoinMode.PASSWORD_AND_INVITE && request.getJoinPassword() != null) {
            workspace.setJoinPasswordHash(passwordEncoder.encode(request.getJoinPassword()));
        }

        // Generate unique join code
        String newJoinCode;
        do {
            newJoinCode = JoinCodeGenerator.generate();
        } while (workspaceRepository.existsByJoinCode(newJoinCode));
        workspace.setJoinCode(newJoinCode);

        workspace = workspaceRepository.save(workspace);

        // 2. Automatically make the creator the OWNER member
        WorkspaceMember ownerMember = new WorkspaceMember();
        ownerMember.setWorkspace(workspace);
        ownerMember.setUser(owner);
        ownerMember.setRole(WorkspaceRole.OWNER);
        ownerMember.setJoinedAt(LocalDateTime.now());
        memberRepository.save(ownerMember);

        initializeDefaultPermissions(workspace);

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
                // Reactivate the user!
                existing.setRemovedAt(null);
                existing.setRole(WorkspaceRole.USER); // Reset to base role
                existing.setJoinedAt(LocalDateTime.now());
                memberRepository.save(existing);
                
                auditLogService.logAction(workspace, user, "MEMBER_REJOINED", user.getEmail() + " rejoined the workspace");
                return workspaceMapper.toResponse(workspace);
            }
        }
        
        // Validate mode and password
        if (workspace.getJoinMode() == JoinMode.INVITE_ONLY) {
            throw new BadRequestException("This workspace is invite-only. You must receive an email invitation.");
        }

        if (workspace.getJoinMode() == JoinMode.PASSWORD_AND_INVITE) {
            if (request.getJoinPassword() == null || !passwordEncoder.matches(request.getJoinPassword(), workspace.getJoinPasswordHash())) {
                throw new BadRequestException("Invalid workspace password");
            }
        }

        // Join as a basic USER
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
        // Find all memberships for this user, then map the linked workspaces
        return memberRepository.findAll().stream() // Ideally you'd use a custom query: findByUserIdAndRemovedAtIsNull
                .filter(m -> m.getUser().getId().equals(user.getId()) && m.getRemovedAt() == null)
                .map(m -> workspaceMapper.toResponse(m.getWorkspace()))
                .collect(Collectors.toList());
    }
    
    private void initializeDefaultPermissions(Workspace workspace) {
        for (WorkspaceRole role : WorkspaceRole.values()) {
            if (role == WorkspaceRole.OWNER) continue; // Owner bypasses DB checks

            for (Permission perm : Permission.values()) {
                WorkspaceRolePermission rolePerm = new WorkspaceRolePermission();
                rolePerm.setWorkspace(workspace);
                rolePerm.setRole(role);
                rolePerm.setPermission(perm);

                // Set sensible defaults based on role
                if (role == WorkspaceRole.ADMIN) {
                    rolePerm.setEnabled(true); // Admins can do everything
                } else if (role == WorkspaceRole.REVIEWER) {
                    // Reviewers only get review-related permissions by default
                    boolean isReviewAction = (perm == Permission.VIEW_ALL_DOCUMENTS || perm == Permission.REVIEW_ASSIGNED_DOCUMENTS);
                    rolePerm.setEnabled(isReviewAction);
                } else if (role == WorkspaceRole.USER) {
                    // Users can only create drafts by default
                    boolean isUserAction = (perm == Permission.CREATE_BLANK_DOCUMENTS || perm == Permission.CREATE_FROM_TEMPLATE);
                    rolePerm.setEnabled(isUserAction);
                }
                
                rolePermissionRepository.save(rolePerm);
            }
        }
    }
}