package com.aelares.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "task_completions", indexes = {
    @Index(name = "idx_task_completions_task_id", columnList = "task_id"),
    @Index(name = "idx_task_completions_date", columnList = "completion_date")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uq_task_completion_date", columnNames = {"task_id", "completion_date"})
})
public class TaskCompletion extends PanacheEntityBase {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    public UUID id;

    @Column(name = "task_id", nullable = false, columnDefinition = "UUID")
    public UUID taskId;

    @Column(name = "completion_date", nullable = false)
    public LocalDate completionDate;

    @Column(name = "completed_at", nullable = false)
    public LocalDateTime completedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    public String notes;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public static TaskCompletion findByTaskAndDate(UUID taskId, LocalDate date) {
        return find("taskId = ?1 and completionDate = ?2", taskId, date).firstResult();
    }

    public static List<TaskCompletion> findByTaskIdAndDateRange(UUID taskId, LocalDate from, LocalDate to) {
        return list("taskId = ?1 and completionDate >= ?2 and completionDate <= ?3", taskId, from, to);
    }

    public static List<TaskCompletion> findByTaskIds(List<UUID> taskIds, LocalDate from, LocalDate to) {
        if (taskIds == null || taskIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return list("taskId in ?1 and completionDate >= ?2 and completionDate <= ?3", taskIds, from, to);
    }
}

