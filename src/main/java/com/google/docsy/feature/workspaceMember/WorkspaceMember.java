package com.google.docsy.feature.workspaceMember;

import com.google.docsy.common.entity.BaseEntity;
import com.google.docsy.enums.WorkspaceRole;
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
@Table(
    name = "workspace_members", 
    uniqueConstraints = {@UniqueConstraint(columnNames = {"workspace_id", "user_id"})}
)
public class WorkspaceMember extends BaseEntity {

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

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "removed_at")
    private LocalDateTime removedAt; 

    @PrePersist
    public void onPrePersist() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }
}