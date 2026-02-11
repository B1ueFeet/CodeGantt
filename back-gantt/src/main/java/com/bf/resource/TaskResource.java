package com.bf.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import com.bf.dtos.TaskDTOs;
import com.bf.Service.TaskService;

import java.util.List;
import java.util.UUID;

@Path("/api/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskResource {

    private final TaskService service;

    public TaskResource(TaskService service) {
        this.service = service;
    }

    @GET
    @Path("/by-project/{projectId}")
    public List<TaskDTOs.TaskResponse> listByProject(@PathParam("projectId") UUID projectId) {
        return service.listByProject(projectId);
    }

    @GET
    @Path("/{id}")
    public TaskDTOs.TaskResponse get(@PathParam("id") UUID id) {
        return service.get(id);
    }

    @POST
    public TaskDTOs.TaskResponse create(TaskDTOs.TaskCreateRequest req) {
        return service.create(req);
    }

    @PUT
    @Path("/{id}")
    public TaskDTOs.TaskResponse update(@PathParam("id") UUID id, TaskDTOs.TaskUpdateRequest req) {
        return service.update(id, req);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") UUID id) {
        service.delete(id);
    }
}
