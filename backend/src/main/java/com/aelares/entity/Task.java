package com.aelares.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_tasks_home_id", columnList = "home_id")
})
public class Task extends PanacheEntityBase {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    public UUID id;

    @Column(name = "home_id", nullable = false, columnDefinition = "UUID")
    public UUID homeId;

    @Column(name = "title", nullable = false, length = 255)
    public String title;

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

    public static java.util.List<Task> findByHomeId(UUID homeId) {
        return list("homeId", homeId);
    }

    public static Task findByIdAndHomeId(UUID taskId, UUID homeId) {
        return find("id = ?1 and homeId = ?2", taskId, homeId).firstResult();
    }
}

