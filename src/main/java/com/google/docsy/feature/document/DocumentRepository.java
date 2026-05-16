package com.google.docsy.feature.document;

import org.springframework.data.jpa.repository.JpaRepository;

import com.google.docsy.enums.DocumentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByWorkspaceIdOrderByUpdatedAtDesc(UUID workspaceId);
    Optional<Document> findByIdAndWorkspaceId(UUID id, UUID workspaceId);
    List<Document> findByWorkspaceIdAndStatusOrderByUpdatedAtDesc(UUID workspaceId, DocumentStatus status);
    List<Document> findByStatusAndArchiveScheduledAtLessThanEqual(DocumentStatus status, java.time.LocalDateTime date);
}