package com.google.entity;

import com.google.enums.WorkspaceRole;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "workspace_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"workspace_id", "user_id"})
}, indexes = {
    @Index(name = "idx_workspace_members_workspace_id", columnList = "workspace_id"),
    @Index(name = "idx_workspace_members_user_id", columnList = "user_id"),
    @Index(name = "idx_workspace_members_role", columnList = "role")
})
public class WorkspaceMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkspaceRole role;

    @Column(name = "display_name", length = 150)
    private String displayName;

    @Column(name = "position_title", length = 150)
    private String positionTitle;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(name = "removed_at")
    private LocalDateTime removedAt;
}