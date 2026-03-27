package com.bf.dtos;

import com.bf.Model.TaskStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public class TaskDTOs {

    public record TaskCreateRequest(
            UUID projectId,
            String name,
            String description,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            TaskStatus status,
            Integer progress
    ) {}

    public record TaskUpdateRequest(
            String name,
            String description,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            TaskStatus status,
            Integer progress
    ) {}

    public record TaskResponse(
            UUID id,
            UUID projectId,
            String name,
            String description,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            TaskStatus status,
            int progress
    ) {}
}