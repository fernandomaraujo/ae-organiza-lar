package com.aelares.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class CreateTaskRequest {
    @NotBlank(message = "Título é obrigatório")
    @Size(min = 2, max = 255, message = "Título deve ter entre 2 e 255 caracteres")
    public String title;

    @Size(max = 1000, message = "Descrição não pode exceder 1000 caracteres")
    public String description;

    @NotBlank(message = "Dia da semana é obrigatório")
    @Min(value = 1, message = "Dia da semana deve ser entre 1 e 7")
    @Max(value = 7, message = "Dia da semana deve ser entre 1 e 7")
    public Integer dayOfWeek;

    public String priority = "MEDIUM"; // LOW, MEDIUM, HIGH

    @Size(max = 500, message = "Notas não podem exceder 500 caracteres")
    public String notes;

    public CreateTaskRequest() {}

    public CreateTaskRequest(String title, String description, Integer dayOfWeek, String priority, String notes) {
        this.title = title;
        this.description = description;
        this.dayOfWeek = dayOfWeek;
        this.priority = priority != null ? priority : "MEDIUM";
        this.notes = notes;
    }
}

