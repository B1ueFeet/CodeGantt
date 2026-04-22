package com.bf.dtos;

import com.bf.Model.TaskStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class TaskDTOs {

    public record AssigneeDTO(
            UUID userId,
            String firstName,
            String lastName,
            String username,
            BigDecimal assignedHours
    ) {}

    public record TaskCreateRequest(
            UUID projectId,
            String name,
            String description,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            TaskStatus status,
            Integer progress,
            BigDecimal estimatedHours,
            List<AssigneeDTO> assignees,
            String externalUid
    ) {}

    public record TaskUpdateRequest(
            String name,
            String description,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            TaskStatus status,
            Integer progress,
            BigDecimal estimatedHours,
            List<AssigneeDTO> assignees
    ) {}

    public record TaskResponse(
            UUID id,
            UUID projectId,
            String name,
            String description,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            TaskStatus status,
            int progress,
            BigDecimal estimatedHours,
            List<AssigneeDTO> assignees
    ) {}
}