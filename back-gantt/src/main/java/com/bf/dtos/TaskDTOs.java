package com.bf.dtos;
import com.bf.Model.TaskStatus;

import java.time.LocalDate;
import java.util.UUID;

public class TaskDTOs {

    public record TaskCreateRequest(
            UUID projectId,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate
    ) {}

    public record TaskUpdateRequest(
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            TaskStatus status,
            Integer progress
    ) {}

    public record TaskResponse(
            UUID id,
            UUID projectId,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            TaskStatus status,
            int progress
    ) {}
}
