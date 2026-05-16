package com.google.docsy.feature.workspace;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
    Optional<Workspace> findByJoinCode(String joinCode);
    boolean existsByJoinCode(String joinCode);
}