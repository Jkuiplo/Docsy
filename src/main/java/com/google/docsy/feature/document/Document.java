package com.google.docsy.feature.document;

import com.google.docsy.common.entity.BaseEntity;
import com.google.docsy.enums.DocumentStatus;
import com.google.docsy.feature.template.Template;
import com.google.docsy.feature.user.User;
import com.google.docsy.feature.workspace.Workspace;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "documents")
public class Document extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private Template template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_reviewer_id")
    private User assignedReviewer;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "template_snapshot_html", columnDefinition = "TEXT")
    private String templateSnapshotHtml;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "archive_scheduled_at")
    private LocalDateTime archiveScheduledAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;
}