package com.google.docsy.feature.workspace;

import com.google.docsy.common.exception.BadRequestException;
import com.google.docsy.common.exception.NotFoundException;
import com.google.docsy.common.util.JoinCodeGenerator;
import com.google.docsy.enums.JoinMode;
import com.google.docsy.enums.WorkspaceRole;
import com.google.docsy.feature.user.User;
import com.google.docsy.feature.workspace.dto.request.CreateWorkspaceRequest;
import com.google.docsy.feature.workspace.dto.request.JoinWorkspaceRequest;
import com.google.docsy.feature.workspace.dto.response.WorkspaceResponse;
import com.google.docsy.feature.workspace.mapper.WorkspaceMapper;
import com.google.docsy.feature.workspaceMember.WorkspaceMember;
import com.google.docsy.feature.workspaceMember.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final WorkspaceMapper workspaceMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public WorkspaceResponse createWorkspace(User owner, CreateWorkspaceRequest request) {
        // 1. Create Workspace
        Workspace workspace = new Workspace();
        workspace.setName(request.getName());
        workspace.setOwner(owner);
        workspace.setJoinMode(request.getJoinMode() != null ? request.getJoinMode() : JoinMode.INVITE_ONLY);

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

        return workspaceMapper.toResponse(workspace);
    }

    @Transactional
    public WorkspaceResponse joinWorkspace(User user, JoinWorkspaceRequest request) {
        Workspace workspace = workspaceRepository.findByJoinCode(request.getJoinCode())
                .orElseThrow(() -> new NotFoundException("Invalid join code"));

        // Check if already a member
        if (memberRepository.findByWorkspaceIdAndUserId(workspace.getId(), user.getId()).isPresent()) {
            throw new BadRequestException("You are already a member of this workspace");
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

        return workspaceMapper.toResponse(workspace);
    }

    public List<WorkspaceResponse> getMyWorkspaces(User user) {
        // Find all memberships for this user, then map the linked workspaces
        return memberRepository.findAll().stream() // Ideally you'd use a custom query: findByUserIdAndRemovedAtIsNull
                .filter(m -> m.getUser().getId().equals(user.getId()) && m.getRemovedAt() == null)
                .map(m -> workspaceMapper.toResponse(m.getWorkspace()))
                .collect(Collectors.toList());
    }
}