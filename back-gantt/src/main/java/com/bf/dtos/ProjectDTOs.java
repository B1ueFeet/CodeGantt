package com.bf.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public class ProjectDTOs {

    public record ProjectCreateRequest(
            String name,
            String description,
            BigDecimal userHourLimit
    ) {}

    public record ProjectUpdateRequest(
            String name,
            String description,
            BigDecimal userHourLimit
    ) {}

    public record ProjectResponse(
            UUID id,
            String name,
            String description,
            UUID ownerId,
            BigDecimal userHourLimit
    ) {}

    public record AddMemberRequest(
            UUID userId,
            int workloadPercent,
            double overtimeHours
    ) {}
}