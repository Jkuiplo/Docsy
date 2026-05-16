package com.google.docsy.feature.workspaceInvitation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceInvitationRepository extends JpaRepository<WorkspaceInvitation, UUID> {
    Optional<WorkspaceInvitation> findByToken(String token);
    List<WorkspaceInvitation> findByWorkspaceId(UUID workspaceId);
}