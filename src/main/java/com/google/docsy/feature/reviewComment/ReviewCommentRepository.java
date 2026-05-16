package com.google.docsy.feature.reviewComment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, UUID> {
    List<ReviewComment> findByDocumentIdOrderByCreatedAtAsc(UUID documentId);
}