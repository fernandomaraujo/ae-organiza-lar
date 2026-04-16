package com.aelares.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserResponse {
    public UUID id;
    public String email;
    public String name;
    public LocalDateTime createdAt;
    public LocalDateTime lastLogin;
    public Object preferences;

    public UserResponse() {}

    public UserResponse(UUID id, String email, String name, LocalDateTime createdAt, LocalDateTime lastLogin, Object preferences) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.preferences = preferences;
    }
}

