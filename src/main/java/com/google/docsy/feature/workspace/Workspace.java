package com.google.docsy.feature.workspace;

import com.google.docsy.common.entity.BaseEntity;
import com.google.docsy.enums.JoinMode;
import com.google.docsy.feature.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "workspaces", indexes = {@Index(name = "idx_workspace_join_code", columnList = "join_code")})
public class Workspace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "join_code", nullable = false, unique = true, length = 20)
    private String joinCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "join_mode", nullable = false, length = 30)
    private JoinMode joinMode = JoinMode.INVITE_ONLY;

    @Column(name = "join_password_hash")
    private String joinPasswordHash; // Only used if JoinMode == PASSWORD_AND_INVITE
}