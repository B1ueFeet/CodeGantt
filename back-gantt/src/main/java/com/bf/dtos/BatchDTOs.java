package com.bf.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class BatchDTOs {

    public record UpdateItem(UUID taskId, OffsetDateTime startAt, OffsetDateTime endAt, BigDecimal estimatedHours) {}

    public record BatchUpdateReq(List<UpdateItem> updates) {}
}
