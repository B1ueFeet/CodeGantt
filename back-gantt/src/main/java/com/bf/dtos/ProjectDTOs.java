package com.bf.dtos;

import java.util.UUID;

public class ProjectDTOs {

    public record ProjectCreateRequest(
            String name,
            String description,
            UUID ownerId
    ) {}

    public record ProjectUpdateRequest(
            String name,
            String description
    ) {}

    public record ProjectResponse(
            UUID id,
            String name,
            String description,
            UUID ownerId
    ) {}

    public record AddMemberRequest(
            UUID userId,
            int workloadPercent,
            double overtimeHours
    ) {}
}
