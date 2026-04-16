package com.aelares.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    public String email;

    public LoginRequest() {}

    public LoginRequest(String email) {
        this.email = email;
    }
}

