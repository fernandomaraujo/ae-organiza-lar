package com.aelares.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_schedules", indexes = {
    @Index(name = "idx_task_schedules_task_id", columnList = "task_id"),
    @Index(name = "idx_task_schedules_day", columnList = "day_of_week")
})
public class TaskSchedule extends PanacheEntityBase {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    public UUID id;

    @Column(name = "task_id", nullable = false, columnDefinition = "UUID")
    public UUID taskId;

    @Column(name = "day_of_week", nullable = false)
    public Integer dayOfWeek; // 1-7 (Segunda a Domingo)

    @Column(name = "priority", nullable = false, length = 20)
    public String priority; // LOW, MEDIUM, HIGH

    @Column(name = "notes", columnDefinition = "TEXT")
    public String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public static TaskSchedule findByTaskId(UUID taskId) {
        return find("taskId", taskId).firstResult();
    }
}

