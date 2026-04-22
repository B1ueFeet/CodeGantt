package com.bf.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import com.bf.Repository.TaskRepository;
import com.bf.dtos.BatchDTOs;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.UUID;
import java.math.BigDecimal;

@Path("/api/tasks/batch-update")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"USER","ADMIN"})
public class TaskBatchResource {

    private final TaskRepository tasks;

    public TaskBatchResource(TaskRepository tasks) {
        this.tasks = tasks;
    }

    @POST
    @Transactional
    public Map<String,Object> batchUpdate(BatchDTOs.BatchUpdateReq req) {
        var applied = new ArrayList<UUID>();
        var errors = new ArrayList<Map<String,String>>();

        for (var it : req.updates()) {
            try {
                var t = tasks.findById(it.taskId());
                if (t == null) {
                    errors.add(Map.of("taskId", it.taskId().toString(), "reason", "Tarea no existe"));
                    continue;
                }
                if (it.startAt() != null) t.startAt = it.startAt();
                if (it.endAt() != null) t.endAt = it.endAt();
                if (it.estimatedHours() != null) t.estimatedHours = it.estimatedHours();
                t.updatedAt = OffsetDateTime.now();
                applied.add(t.id);
            } catch (Exception e) {
                errors.add(Map.of("taskId", it.taskId().toString(), "reason", e.getMessage()));
            }
        }

        return Map.of("applied", applied, "errors", errors);
    }
}
