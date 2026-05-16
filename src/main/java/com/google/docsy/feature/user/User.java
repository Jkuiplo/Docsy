package com.google.docsy.feature.user;

import com.google.docsy.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users", indexes = {@Index(name = "idx_users_email", columnList = "email")})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "position_title", length = 150)
    private String positionTitle;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;
}