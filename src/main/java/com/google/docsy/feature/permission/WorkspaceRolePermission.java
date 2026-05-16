package com.google.docsy.feature.permission;

import com.google.docsy.common.entity.BaseEntity;
import com.google.docsy.enums.WorkspaceRole;
import com.google.docsy.feature.workspace.Workspace;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
    name = "workspace_role_permissions", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"workspace_id", "role", "permission"}) 
    }
)
public class WorkspaceRolePermission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkspaceRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private Permission permission;

    @Column(nullable = false)
    private boolean enabled = false;
}