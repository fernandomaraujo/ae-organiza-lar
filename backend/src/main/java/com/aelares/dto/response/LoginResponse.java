package com.aelares.dto.response;

public class LoginResponse {
    public String message;
    public String verificationId;

    public LoginResponse() {}

    public LoginResponse(String message, String verificationId) {
        this.message = message;
        this.verificationId = verificationId;
    }
}

