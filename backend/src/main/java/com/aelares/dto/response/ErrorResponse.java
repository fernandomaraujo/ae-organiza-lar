package com.aelares.dto.response;

public class ErrorResponse {
    public String message;
    public String details;
    public int status;
    public long timestamp;

    public ErrorResponse() {}

    public ErrorResponse(String message, String details, int status, long timestamp) {
        this.message = message;
        this.details = details;
        this.status = status;
        this.timestamp = timestamp;
    }

    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }
}

