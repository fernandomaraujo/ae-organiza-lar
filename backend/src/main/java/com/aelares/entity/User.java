package com.aelares.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email", unique = true)
})
public class User extends PanacheEntityBase {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    public UUID id;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    public String email;

    @Column(name = "name", nullable = false, length = 255)
    public String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "last_login")
    public LocalDateTime lastLogin;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferences", columnDefinition = "jsonb")
    public Map<String, Object> preferences;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public static User findByEmail(String email) {
        return find("email", email).firstResult();
    }
}

