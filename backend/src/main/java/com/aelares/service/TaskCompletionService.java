package com.aelares.service;

import com.aelares.dto.request.TaskCompletionRequest;
import com.aelares.dto.response.TaskCompletionResponse;
import com.aelares.dto.response.WeeklyPlanResponse;
import com.aelares.dto.response.WeeklyPlanResponse.DayPlanResponse;
import com.aelares.dto.response.WeeklyPlanResponse.TaskDayResponse;
import com.aelares.entity.Home;
import com.aelares.entity.Task;
import com.aelares.entity.TaskCompletion;
import com.aelares.entity.TaskSchedule;
import com.aelares.exception.ResourceNotFoundException;
import com.aelares.exception.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class TaskCompletionService {

    /**
     * Marca uma tarefa como concluída numa data específica.
     * Se já estiver concluída naquela data, atualiza as notas.
     */
    @Transactional
    public TaskCompletionResponse completeTask(UUID homeId, UUID taskId, TaskCompletionRequest request) {
        validateTaskBelongsToHome(taskId, homeId);

        TaskCompletion existing = TaskCompletion.findByTaskAndDate(taskId, request.date);
        if (existing != null) {
            existing.notes = request.notes;
            existing.persist();
            return toResponse(existing);
        }

        TaskCompletion completion = new TaskCompletion();
        completion.id = UUID.randomUUID();
        completion.taskId = taskId;
        completion.completionDate = request.date;
        completion.completedAt = LocalDateTime.now();
        completion.notes = request.notes;
        completion.persist();

        return toResponse(completion);
    }

    /**
     * Desmarca uma tarefa como concluída numa data específica.
     */
    @Transactional
    public void uncompleteTask(UUID homeId, UUID taskId, LocalDate date) {
        validateTaskBelongsToHome(taskId, homeId);

        TaskCompletion completion = TaskCompletion.findByTaskAndDate(taskId, date);
        if (completion == null) {
            throw new ResourceNotFoundException("Conclusão não encontrada para esta data");
        }
        completion.delete();
    }

    /**
     * Lista conclusões de uma tarefa num intervalo de datas.
     */
    public List<TaskCompletionResponse> getCompletions(UUID homeId, UUID taskId, LocalDate from, LocalDate to) {
        validateTaskBelongsToHome(taskId, homeId);
        return TaskCompletion.findByTaskIdAndDateRange(taskId, from, to)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Retorna o planejamento semanal completo de um lar.
     * weekStart é uma data qualquer; a semana vai de segunda (ou weekStart) a domingo.
     */
    public WeeklyPlanResponse getWeeklyPlan(UUID homeId, UUID userId, LocalDate referenceDate) {
        Home home = Home.findByIdAndUserId(homeId, userId);
        if (home == null) {
            throw new ResourceNotFoundException("Residência não encontrada");
        }

        // Ajusta para segunda-feira da semana de referência
        LocalDate weekStart = referenceDate.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        List<Task> allTasks = Task.findByHomeId(homeId);
        if (allTasks.isEmpty()) {
            return new WeeklyPlanResponse(homeId, home.name, weekStart, weekEnd, buildEmptyDays(weekStart));
        }

        // Busca agendamentos e conclusões de uma vez
        List<UUID> taskIds = allTasks.stream().map(t -> t.id).collect(Collectors.toList());
        Map<UUID, TaskSchedule> scheduleByTask = allTasks.stream()
            .collect(Collectors.toMap(
                t -> t.id,
                t -> {
                    TaskSchedule s = TaskSchedule.findByTaskId(t.id);
                    return s != null ? s : new TaskSchedule();
                }
            ));

        List<TaskCompletion> completions = TaskCompletion.findByTaskIds(taskIds, weekStart, weekEnd);
        // Agrupa por taskId + data
        Map<String, TaskCompletion> completionMap = completions.stream()
            .collect(Collectors.toMap(
                c -> c.taskId + "_" + c.completionDate,
                c -> c
            ));

        // Monta os 7 dias
        List<DayPlanResponse> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            int dow = i + 1; // 1 = Segunda ... 7 = Domingo
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));

            List<TaskDayResponse> dayTasks = new ArrayList<>();
            for (Task task : allTasks) {
                TaskSchedule schedule = scheduleByTask.get(task.id);
                if (schedule.dayOfWeek == null || !schedule.dayOfWeek.equals(dow)) continue;

                String key = task.id + "_" + date;
                TaskCompletion completion = completionMap.get(key);
                boolean completed = completion != null;

                dayTasks.add(new TaskDayResponse(
                    task.id,
                    task.title,
                    task.description,
                    schedule.priority,
                    schedule.notes,
                    completed,
                    completed ? toResponse(completion) : null
                ));
            }

            days.add(new DayPlanResponse(dow, capitalize(dayName), date, dayTasks));
        }

        return new WeeklyPlanResponse(homeId, home.name, weekStart, weekEnd, days);
    }

    private List<DayPlanResponse> buildEmptyDays(LocalDate weekStart) {
        List<DayPlanResponse> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            int dow = i + 1;
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
            days.add(new DayPlanResponse(dow, capitalize(dayName), date, Collections.emptyList()));
        }
        return days;
    }

    private void validateTaskBelongsToHome(UUID taskId, UUID homeId) {
        Task task = Task.findByIdAndHomeId(taskId, homeId);
        if (task == null) {
            throw new ResourceNotFoundException("Tarefa não encontrada");
        }
    }

    private TaskCompletionResponse toResponse(TaskCompletion c) {
        return new TaskCompletionResponse(c.id, c.taskId, c.completionDate, c.completedAt, c.notes);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}

