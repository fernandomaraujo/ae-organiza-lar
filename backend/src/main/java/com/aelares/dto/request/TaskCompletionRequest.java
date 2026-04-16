package com.aelares.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class TaskCompletionRequest {

    @NotNull(message = "Data é obrigatória")
    public LocalDate date;

    @Size(max = 500, message = "Notas não podem exceder 500 caracteres")
    public String notes;

    public TaskCompletionRequest() {}

    public TaskCompletionRequest(LocalDate date, String notes) {
        this.date = date;
        this.notes = notes;
    }
}

