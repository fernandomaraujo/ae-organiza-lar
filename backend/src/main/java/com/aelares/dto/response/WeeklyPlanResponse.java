package com.aelares.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Visão semanal: tarefas de um lar agrupadas por dia da semana,
 * com informação de conclusão para cada data específica do período.
 */
public class WeeklyPlanResponse {

    public UUID homeId;
    public String homeName;
    public LocalDate weekStart;
    public LocalDate weekEnd;
    public List<DayPlanResponse> days;

    public WeeklyPlanResponse() {}

    public WeeklyPlanResponse(UUID homeId, String homeName, LocalDate weekStart, LocalDate weekEnd, List<DayPlanResponse> days) {
        this.homeId = homeId;
        this.homeName = homeName;
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.days = days;
    }

    public static class DayPlanResponse {
        /** 1 = Segunda, 2 = Terça, ..., 7 = Domingo */
        public int dayOfWeek;
        public String dayName;
        public LocalDate date;
        public List<TaskDayResponse> tasks;

        public DayPlanResponse() {}

        public DayPlanResponse(int dayOfWeek, String dayName, LocalDate date, List<TaskDayResponse> tasks) {
            this.dayOfWeek = dayOfWeek;
            this.dayName = dayName;
            this.date = date;
            this.tasks = tasks;
        }
    }

    public static class TaskDayResponse {
        public UUID taskId;
        public String title;
        public String description;
        public String priority;
        public String notes;
        public boolean completed;
        public TaskCompletionResponse completion;

        public TaskDayResponse() {}

        public TaskDayResponse(UUID taskId, String title, String description, String priority,
                               String notes, boolean completed, TaskCompletionResponse completion) {
            this.taskId = taskId;
            this.title = title;
            this.description = description;
            this.priority = priority;
            this.notes = notes;
            this.completed = completed;
            this.completion = completion;
        }
    }
}

