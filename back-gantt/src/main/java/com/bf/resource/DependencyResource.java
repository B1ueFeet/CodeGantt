package com.bf.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import com.bf.Service.DependencyService;
import com.bf.Model.Dependency;

import java.util.UUID;

@Path("/api/dependencies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"USER","ADMIN"})
public class DependencyResource {

    private final DependencyService service;

    public static record CreateReq(UUID projectId, UUID predecessorId, UUID successorId, int lagMinutes) {}

    public DependencyResource(DependencyService service) {
        this.service = service;
    }

    @POST
    public Dependency create(CreateReq req) {
        return service.create(req.projectId, req.predecessorId, req.successorId, req.lagMinutes);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") UUID id) {
        service.delete(id);
    }
}
