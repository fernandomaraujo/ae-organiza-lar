package com.aelares.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_tokens", indexes = {
    @Index(name = "idx_auth_tokens_email", columnList = "email"),
    @Index(name = "idx_auth_tokens_expires", columnList = "expires_at")
})
public class AuthToken extends PanacheEntityBase {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    public UUID id;

    @Column(name = "email", nullable = false, length = 255)
    public String email;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    public String code; // 6 digits OTP

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    public LocalDateTime expiresAt;

    @Column(name = "used_at")
    public LocalDateTime usedAt;

    @Column(name = "ip_address", length = 45)
    public String ipAddress;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public static AuthToken findByCode(String code) {
        return find("code", code).firstResult();
    }

    public static java.util.List<AuthToken> findByEmail(String email) {
        return list("email", email);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isUsed() {
        return this.usedAt != null;
    }
}

