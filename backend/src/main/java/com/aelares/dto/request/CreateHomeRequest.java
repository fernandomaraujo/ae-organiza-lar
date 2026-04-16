package com.aelares.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateHomeRequest {
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
    public String name;

    @Size(max = 500, message = "Descrição não pode exceder 500 caracteres")
    public String description;

    public CreateHomeRequest() {}

    public CreateHomeRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

