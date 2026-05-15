package com.google.docsy.feature.workspace;

import com.google.docsy.common.entity.BaseEntity;
import com.google.docsy.feature.user.User;
import com.google.docsy.enums.JoinMode;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "workspaces", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"owner_id", "name"})
})
public class Workspace extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "join_code", nullable = false, unique = true, length = 50)
    private String joinCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "join_mode", nullable = false, length = 30)
    private JoinMode joinMode;

    @Column(name = "join_password_hash", length = 255)
    private String joinPasswordHash;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "color_scheme", length = 50)
    private String colorScheme;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}