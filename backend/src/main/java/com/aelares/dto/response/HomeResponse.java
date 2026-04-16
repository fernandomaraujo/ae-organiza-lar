package com.aelares.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public class HomeResponse {
    public UUID id;
    public UUID userId;
    public String name;
    public String description;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public HomeResponse() {}

    public HomeResponse(UUID id, UUID userId, String name, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}

