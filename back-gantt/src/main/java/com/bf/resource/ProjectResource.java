package com.bf.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import com.bf.dtos.ProjectDTOs;
import com.bf.Model.ProjectMember;
import com.bf.Service.ProjectService;

import java.util.List;
import java.util.UUID;

@Path("/api/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"USER","ADMIN"})
public class ProjectResource {

    private final ProjectService service;

    public ProjectResource(ProjectService service) {
        this.service = service;
    }

    @GET
    public List<ProjectDTOs.ProjectResponse> list() {
        return service.list();
    }

    @GET
    @Path("/{id}")
    public ProjectDTOs.ProjectResponse get(@PathParam("id") UUID id) {
        return service.get(id);
    }

    @POST
    public ProjectDTOs.ProjectResponse create(ProjectDTOs.ProjectCreateRequest req) {
        return service.create(req);
    }

    @PUT
    @Path("/{id}")
    public ProjectDTOs.ProjectResponse update(@PathParam("id") UUID id, ProjectDTOs.ProjectUpdateRequest req) {
        return service.update(id, req);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") UUID id) {
        service.delete(id);
    }

    // --- Members ---
    @GET
    @Path("/{id}/members")
    public List<ProjectMember> members(@PathParam("id") UUID projectId) {
        return service.listMembers(projectId);
    }

    @POST
    @Path("/{id}/members")
    public void addMember(@PathParam("id") UUID projectId, ProjectDTOs.AddMemberRequest req) {
        service.addMember(projectId, req);
    }

    @DELETE
    @Path("/{id}/members/{userId}")
    public void removeMember(@PathParam("id") UUID projectId, @PathParam("userId") UUID userId) {
        service.removeMember(projectId, userId);
    }
}
