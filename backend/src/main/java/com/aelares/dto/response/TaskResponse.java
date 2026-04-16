package com.aelares.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public class TaskResponse {
    public UUID id;
    public UUID homeId;
    public String title;
    public String description;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public TaskScheduleResponse schedule;

    public TaskResponse() {}

    public TaskResponse(UUID id, UUID homeId, String title, String description, LocalDateTime createdAt, LocalDateTime updatedAt, TaskScheduleResponse schedule) {
        this.id = id;
        this.homeId = homeId;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.schedule = schedule;
    }

    public static class TaskScheduleResponse {
        public UUID id;
        public Integer dayOfWeek;
        public String priority;
        public String notes;
        public LocalDateTime createdAt;

        public TaskScheduleResponse() {}

        public TaskScheduleResponse(UUID id, Integer dayOfWeek, String priority, String notes, LocalDateTime createdAt) {
            this.id = id;
            this.dayOfWeek = dayOfWeek;
            this.priority = priority;
            this.notes = notes;
            this.createdAt = createdAt;
        }
    }
}

