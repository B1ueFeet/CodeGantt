package com.bf.Service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.bf.Model.Task;
import com.bf.Model.TaskAssignee;
import com.bf.Model.TaskAssigneeId;
import com.bf.Model.TaskStatus;
import com.bf.Repository.AppUserRepository;
import com.bf.Repository.ProjectRepository;
import com.bf.Repository.TaskAssigneeRepository;
import com.bf.Repository.TaskRepository;
import com.bf.dtos.TaskDTOs;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class TaskService {

    private final TaskRepository tasks;
    private final ProjectRepository projects;
    private final DependencyService dependencyService;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final AppUserRepository appUserRepository;

    public TaskService(
            TaskRepository tasks,
            ProjectRepository projects,
            DependencyService dependencyService,
            TaskAssigneeRepository taskAssigneeRepository,
            AppUserRepository appUserRepository
    ) {
        this.tasks = tasks;
        this.projects = projects;
        this.dependencyService = dependencyService;
        this.taskAssigneeRepository = taskAssigneeRepository;
        this.appUserRepository = appUserRepository;
    }

    public List<TaskDTOs.TaskResponse> listByProject(UUID projectId) {
        return tasks.listByProjectId(projectId).stream()
                .map(this::toResponse)
                .toList();
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
        return createInternal(req);
    }

    @Transactional
    public Task createFromImport(UUID projectId, com.bf.Service.MsProjectImportService.ImportedTask it) {
        var p = projects.findById(projectId);
        if (p == null) throw new IllegalArgumentException("projectId no existe");

        var t = new Task();
        t.id = UUID.randomUUID();
        t.project = p;
        t.name = it.name == null ? "Untitled" : it.name.trim();
        t.description = null;
        t.startAt = it.start != null ? it.start : OffsetDateTime.now();
        t.endAt = it.start != null ? it.start.plusMinutes(Math.max(1, it.durationMinutes)) : t.startAt.plusHours(1);
        t.status = TaskStatus.TODO;
        t.progress = 0;
        t.estimatedHours = BigDecimal.valueOf(it.durationMinutes / 60.0);
        t.externalUid = it.uid;
        t.createdAt = OffsetDateTime.now();
        t.updatedAt = OffsetDateTime.now();

        if (p.userHourLimit != null && t.estimatedHours != null && t.estimatedHours.compareTo(p.userHourLimit) > 0) {
            throw new IllegalArgumentException("Estimated hours for task exceed project user_hour_limit");
        }

        tasks.persist(t);
        return t;
    }

    private TaskDTOs.TaskResponse createInternal(TaskDTOs.TaskCreateRequest req) {
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

        var estimatedHours = req.estimatedHours() != null ? req.estimatedHours() : BigDecimal.ZERO;

        if (p.userHourLimit != null && estimatedHours.compareTo(p.userHourLimit) > 0) {
            throw new IllegalArgumentException("estimatedHours excede user_hour_limit del proyecto");
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
        t.estimatedHours = estimatedHours;
        t.externalUid = req.externalUid();
        t.createdAt = OffsetDateTime.now();
        t.updatedAt = OffsetDateTime.now();

        tasks.persist(t);

        if (req.assignees() != null) {
            syncAssignees(t, req.assignees());
        }

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

        if (req.estimatedHours() != null) {
            if (t.project != null && t.project.userHourLimit != null && req.estimatedHours().compareTo(t.project.userHourLimit) > 0) {
                throw new IllegalArgumentException("estimatedHours excede user_hour_limit del proyecto");
            }
            t.estimatedHours = req.estimatedHours();
        }

        if (t.status == TaskStatus.DONE && t.progress < 100) {
            t.progress = 100;
        }

        if (t.status == TaskStatus.TODO && t.progress == 100) {
            t.progress = 0;
        }

        t.updatedAt = OffsetDateTime.now();

        if (req.assignees() != null) {
            syncAssignees(t, req.assignees());
        }

        return toResponse(t);
    }

    @Transactional
    public void delete(UUID id) {
        boolean deleted = tasks.deleteById(id);
        if (!deleted) {
            throw new IllegalArgumentException("Tarea no existe");
        }
    }

    @Transactional
    public void createDependency(UUID predecessorId, UUID successorId, int lagMinutes) {
        var pred = tasks.findById(predecessorId);
        if (pred == null) throw new IllegalArgumentException("Predecessor no existe");
        dependencyService.create(pred.project.id, predecessorId, successorId, lagMinutes);
    }

    @Transactional
    public void assignUserToTask(UUID taskId, UUID userId, BigDecimal hours) {
        var t = tasks.findById(taskId);
        if (t == null) throw new IllegalArgumentException("Tarea no existe");

        var user = appUserRepository.findById(userId);
        if (user == null) throw new IllegalArgumentException("Usuario no existe");

        var p = t.project;
        if (p != null && p.userHourLimit != null) {
            BigDecimal existing = taskAssigneeRepository.sumAssignedHoursByUserInProject(p.id, userId);
            if (existing == null) existing = BigDecimal.ZERO;
            BigDecimal newTotal = existing.add(hours != null ? hours : BigDecimal.ZERO);

            if (newTotal.compareTo(p.userHourLimit) > 0) {
                throw new IllegalArgumentException("Asignación excede user_hour_limit del proyecto");
            }
        }

        TaskAssignee ta = new TaskAssignee();
        ta.id = new TaskAssigneeId(taskId, userId);
        ta.task = t;
        ta.user = user;
        ta.assignedHours = hours != null ? hours : BigDecimal.ZERO;
        ta.persist();
    }

    private void syncAssignees(Task task, List<TaskDTOs.AssigneeDTO> assignees) {
        taskAssigneeRepository.delete("task.id", task.id);

        if (assignees == null || assignees.isEmpty()) {
            return;
        }

        Set<UUID> processedUsers = new HashSet<>();

        for (var assignee : assignees) {
            if (assignee == null || assignee.userId() == null) {
                continue;
            }

            if (processedUsers.contains(assignee.userId())) {
                continue;
            }

            var user = appUserRepository.findById(assignee.userId());
            if (user == null) {
                continue;
            }

            var assignedHours = assignee.assignedHours() != null ? assignee.assignedHours() : BigDecimal.ZERO;

            if (task.project != null && task.project.userHourLimit != null) {
                BigDecimal existing = taskAssigneeRepository.sumAssignedHoursByUserInProject(task.project.id, assignee.userId());
                if (existing == null) existing = BigDecimal.ZERO;

                BigDecimal newTotal = existing.add(assignedHours);
                if (newTotal.compareTo(task.project.userHourLimit) > 0) {
                    throw new IllegalArgumentException("Asignación excede user_hour_limit del proyecto para el usuario " + user.username);
                }
            }

            TaskAssignee entity = new TaskAssignee();
            entity.id = new TaskAssigneeId(task.id, user.id);
            entity.task = task;
            entity.user = user;
            entity.assignedHours = assignedHours;
            entity.persist();

            processedUsers.add(user.id);
        }
    }

    private TaskDTOs.TaskResponse toResponse(Task t) {
        var assignees = taskAssigneeRepository.list("task.id", t.id).stream()
                .map(item -> new TaskDTOs.AssigneeDTO(
                        item.user.id,
                        item.user.firstName,
                        item.user.lastName,
                        item.user.username,
                        item.assignedHours
                ))
                .toList();

        return new TaskDTOs.TaskResponse(
                t.id,
                t.project != null ? t.project.id : null,
                t.name,
                t.description,
                t.startAt,
                t.endAt,
                t.status,
                t.progress,
                t.estimatedHours != null ? t.estimatedHours : BigDecimal.ZERO,
                assignees
        );
    }
}