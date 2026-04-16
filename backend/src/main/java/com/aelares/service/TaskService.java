package com.aelares.service;

import com.aelares.entity.Task;
import com.aelares.entity.TaskSchedule;
import com.aelares.dto.request.CreateTaskRequest;
import com.aelares.dto.response.TaskResponse;
import com.aelares.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class TaskService {

    /**
     * Cria uma nova tarefa com agendamento
     */
    @Transactional
    public TaskResponse createTask(UUID homeId, CreateTaskRequest request) {
        Task task = new Task();
        task.id = UUID.randomUUID();
        task.homeId = homeId;
        task.title = request.title;
        task.description = request.description;
        task.createdAt = LocalDateTime.now();
        task.updatedAt = LocalDateTime.now();

        task.persist();

        // Criar agendamento
        TaskSchedule schedule = new TaskSchedule();
        schedule.id = UUID.randomUUID();
        schedule.taskId = task.id;
        schedule.dayOfWeek = request.dayOfWeek;
        schedule.priority = request.priority != null ? request.priority : "MEDIUM";
        schedule.notes = request.notes;
        schedule.createdAt = LocalDateTime.now();

        schedule.persist();

        return toResponse(task, schedule);
    }

    public TaskResponse getTaskById(UUID homeId, UUID taskId) {
        Task task = findTaskByHome(taskId, homeId);

        TaskSchedule schedule = TaskSchedule.findByTaskId(taskId);
        return toResponse(task, schedule);
    }

    /**
     * Lista todas as tarefas de uma residência
     */
    public List<TaskResponse> getTasksByHomeId(UUID homeId) {
        List<Task> tasks = Task.findByHomeId(homeId);
        return tasks.stream()
            .map(task -> {
                TaskSchedule schedule = TaskSchedule.findByTaskId(task.id);
                return toResponse(task, schedule);
            })
            .collect(Collectors.toList());
    }

    /**
     * Lista tarefas agendadas para um dia específico da semana
     */
    public List<TaskResponse> getTasksByDayOfWeek(UUID homeId, Integer dayOfWeek) {
        List<Task> allTasks = Task.findByHomeId(homeId);

        return allTasks.stream()
            .map(task -> {
                TaskSchedule schedule = TaskSchedule.findByTaskId(task.id);
                return schedule;
            })
            .filter(schedule -> schedule != null && schedule.dayOfWeek.equals(dayOfWeek))
            .map(schedule -> {
                Task task = Task.findById(schedule.taskId);
                return toResponse(task, schedule);
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse updateTask(UUID homeId, UUID taskId, CreateTaskRequest request) {
        Task task = findTaskByHome(taskId, homeId);

        task.title = request.title;
        task.description = request.description;
        task.updatedAt = LocalDateTime.now();
        task.persist();

        TaskSchedule schedule = TaskSchedule.findByTaskId(taskId);
        if (schedule != null) {
            schedule.dayOfWeek = request.dayOfWeek;
            schedule.priority = request.priority;
            schedule.notes = request.notes;
            schedule.persist();
        }

        return toResponse(task, schedule);
    }

    @Transactional
    public void deleteTask(UUID homeId, UUID taskId) {
        Task task = findTaskByHome(taskId, homeId);
        task.delete();
    }

    private Task findTaskByHome(UUID taskId, UUID homeId) {
        Task task = Task.findByIdAndHomeId(taskId, homeId);
        if (task == null) {
            throw new ResourceNotFoundException("Tarefa não encontrada");
        }
        return task;
    }

    /**
     * Converte entidades para TaskResponse
     */
    private TaskResponse toResponse(Task task, TaskSchedule schedule) {
        TaskResponse.TaskScheduleResponse scheduleResponse = null;
        if (schedule != null) {
            scheduleResponse = new TaskResponse.TaskScheduleResponse(
                schedule.id,
                schedule.dayOfWeek,
                schedule.priority,
                schedule.notes,
                schedule.createdAt
            );
        }

        return new TaskResponse(
            task.id,
            task.homeId,
            task.title,
            task.description,
            task.createdAt,
            task.updatedAt,
            scheduleResponse
        );
    }
}

