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
        if (t == null) throw new IllegalArgumentException("Tarea no existe");
        return toResponse(t);
    }

    @Transactional
    public TaskDTOs.TaskResponse create(TaskDTOs.TaskCreateRequest req) {
        var p = projects.findById(req.projectId());
        if (p == null) throw new IllegalArgumentException("projectId no existe");

        var t = new Task();
        t.id = UUID.randomUUID();
        t.project = p;
        t.name = req.name();
        t.description = req.description();
        t.startDate = req.startDate();
        t.endDate = req.endDate();
        t.status = TaskStatus.TODO;
        t.progress = 0;
        t.createdAt = OffsetDateTime.now();
        t.updatedAt = OffsetDateTime.now();

        tasks.persist(t);
        return toResponse(t);
    }

    @Transactional
    public TaskDTOs.TaskResponse update(UUID id, TaskDTOs.TaskUpdateRequest req) {
        var t = tasks.findById(id);
        if (t == null) throw new IllegalArgumentException("Tarea no existe");

        if (req.name() != null) t.name = req.name();
        if (req.description() != null) t.description = req.description();
        if (req.startDate() != null) t.startDate = req.startDate();
        if (req.endDate() != null) t.endDate = req.endDate();
        if (req.status() != null) t.status = req.status();
        if (req.progress() != null) t.progress = Math.max(0, Math.min(100, req.progress()));

        t.updatedAt = OffsetDateTime.now();
        return toResponse(t);
    }

    @Transactional
    public void delete(UUID id) {
        boolean deleted = tasks.deleteById(id);
        if (!deleted) throw new IllegalArgumentException("Tarea no existe");
    }

    private TaskDTOs.TaskResponse toResponse(Task t) {
        return new TaskDTOs.TaskResponse(
                t.id,
                t.project.id,
                t.name,
                t.description,
                t.startDate,
                t.endDate,
                t.status,
                t.progress
        );
    }
}
