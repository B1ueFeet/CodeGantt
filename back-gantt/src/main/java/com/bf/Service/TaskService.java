package com.bf.Service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import com.bf.dtos.TaskDTOs;
import com.bf.Model.Task;
import com.bf.Model.TaskStatus;
import com.bf.Repository.ProjectRepository;
import com.bf.Repository.TaskRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TaskService {

    private final TaskRepository tasks;
    private final ProjectRepository projects;

    public TaskService(TaskRepository tasks, ProjectRepository projects) {
        this.tasks = tasks;
        this.projects = projects;
    }

    public List<TaskDTOs.TaskResponse> listByProject(UUID projectId) {
        return tasks.listByProjectId(projectId).stream().map(this::toResponse).toList();
    }

    public TaskDTOs.TaskResponse get(UUID id) {
        var t = tasks.findById(id);
        if (t == null) {
            throw new IllegalArgumentException("Tarea no existe");
        }
        return toResponse(t);
    }

    @Transactional
    public TaskDTOs.TaskResponse create(TaskDTOs.TaskCreateRequest req) {
        if (req.projectId() == null) {
            throw new IllegalArgumentException("projectId es obligatorio");
        }

        if (req.name() == null || req.name().isBlank()) {
            throw new IllegalArgumentException("El nombre de la tarea es obligatorio");
        }

        var p = projects.findById(req.projectId());
        if (p == null) {
            throw new IllegalArgumentException("projectId no existe");
        }

        var startAt = req.startAt() != null ? req.startAt() : OffsetDateTime.now();
        var endAt = req.endAt() != null ? req.endAt() : startAt.plusHours(2);

        if (endAt.isBefore(startAt)) {
            throw new IllegalArgumentException("endAt no puede ser menor que startAt");
        }

        var t = new Task();
        t.id = UUID.randomUUID();
        t.project = p;
        t.name = req.name().trim();
        t.description = req.description();
        t.startAt = startAt;
        t.endAt = endAt;
        t.status = req.status() != null ? req.status() : TaskStatus.TODO;
        t.progress = req.progress() != null ? Math.max(0, Math.min(100, req.progress())) : 0;
        t.createdAt = OffsetDateTime.now();
        t.updatedAt = OffsetDateTime.now();

        tasks.persist(t);
        return toResponse(t);
    }

    @Transactional
    public TaskDTOs.TaskResponse update(UUID id, TaskDTOs.TaskUpdateRequest req) {
        var t = tasks.findById(id);
        if (t == null) {
            throw new IllegalArgumentException("Tarea no existe");
        }

        if (req.name() != null) {
            if (req.name().isBlank()) {
                throw new IllegalArgumentException("El nombre de la tarea no puede estar vacío");
            }
            t.name = req.name().trim();
        }

        if (req.description() != null) {
            t.description = req.description();
        }

        var nextStartAt = req.startAt() != null ? req.startAt() : t.startAt;
        var nextEndAt = req.endAt() != null ? req.endAt() : t.endAt;

        if (nextStartAt == null) {
            nextStartAt = OffsetDateTime.now();
        }

        if (nextEndAt == null) {
            nextEndAt = nextStartAt.plusHours(2);
        }

        if (nextEndAt.isBefore(nextStartAt)) {
            throw new IllegalArgumentException("endAt no puede ser menor que startAt");
        }

        t.startAt = nextStartAt;
        t.endAt = nextEndAt;

        if (req.status() != null) {
            t.status = req.status();
        }

        if (req.progress() != null) {
            t.progress = Math.max(0, Math.min(100, req.progress()));
        }

        if (t.status == TaskStatus.DONE && t.progress < 100) {
            t.progress = 100;
        }

        if (t.status == TaskStatus.TODO && t.progress == 100) {
            t.progress = 0;
        }

        t.updatedAt = OffsetDateTime.now();

        return toResponse(t);
    }

    @Transactional
    public void delete(UUID id) {
        boolean deleted = tasks.deleteById(id);
        if (!deleted) {
            throw new IllegalArgumentException("Tarea no existe");
        }
    }

    private TaskDTOs.TaskResponse toResponse(Task t) {
        return new TaskDTOs.TaskResponse(
                t.id,
                t.project.id,
                t.name,
                t.description,
                t.startAt,
                t.endAt,
                t.status,
                t.progress
        );
    }
}