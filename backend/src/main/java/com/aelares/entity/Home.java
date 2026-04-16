package com.aelares.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "homes", indexes = {
    @Index(name = "idx_homes_user_id", columnList = "user_id")
})
public class Home extends PanacheEntityBase {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    public UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    public UUID userId;

    @Column(name = "name", nullable = false, length = 255)
    public String name;

    @Column(name = "description", columnDefinition = "TEXT")
    public String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static java.util.List<Home> findByUserId(UUID userId) {
        return list("userId", userId);
    }

    public static Home findByIdAndUserId(UUID homeId, UUID userId) {
        return find("id = ?1 and userId = ?2", homeId, userId).firstResult();
    }
}

