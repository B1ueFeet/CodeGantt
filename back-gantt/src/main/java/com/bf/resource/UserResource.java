package com.bf.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import com.bf.dtos.UserDTOs;
import com.bf.Service.UserService;

import java.util.List;
import java.util.UUID;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private final UserService service;

    public UserResource(UserService service) {
        this.service = service;
    }

    @GET
    public List<UserDTOs.UserResponse> list() {
        return service.list();
    }

    @GET
    @Path("/{id}")
    public UserDTOs.UserResponse get(@PathParam("id") UUID id) {
        return service.get(id);
    }

    @POST
    @PermitAll
    public UserDTOs.UserResponse create(UserDTOs.UserCreateRequest req) {
        return service.create(req);
    }

    @PUT
    @Path("/{id}")
    public UserDTOs.UserResponse update(@PathParam("id") UUID id, UserDTOs.UserUpdateRequest req) {
        return service.update(id, req);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")

    public void delete(@PathParam("id") UUID id) {
        service.delete(id);
    }
}
