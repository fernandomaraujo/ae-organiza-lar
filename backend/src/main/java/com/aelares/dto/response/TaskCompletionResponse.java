package com.aelares.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class TaskCompletionResponse {
    public UUID id;
    public UUID taskId;
    public LocalDate completionDate;
    public LocalDateTime completedAt;
    public String notes;

    public TaskCompletionResponse() {}

    public TaskCompletionResponse(UUID id, UUID taskId, LocalDate completionDate, LocalDateTime completedAt, String notes) {
        this.id = id;
        this.taskId = taskId;
        this.completionDate = completionDate;
        this.completedAt = completedAt;
        this.notes = notes;
    }
}

