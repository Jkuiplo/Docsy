package com.google.docsy.feature.documentVersion;

import com.google.docsy.feature.user.User;
import com.google.docsy.feature.workspace.Workspace;
import com.google.docsy.feature.document.Document;
import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "document_versions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"document_id", "version_number"})
}, indexes = {
    @Index(name = "idx_document_versions_document_id", columnList = "document_id"),
    @Index(name = "idx_document_versions_workspace_id", columnList = "workspace_id")
})
public class DocumentVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "title_snapshot", nullable = false, length = 255)
    private String titleSnapshot;

    @Column(name = "content_snapshot", nullable = false, columnDefinition = "TEXT")
    private String contentSnapshot;

    @Column(name = "rendered_html_snapshot", columnDefinition = "TEXT")
    private String renderedHtmlSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edited_by", nullable = false)
    private User editedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}