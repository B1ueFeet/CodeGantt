package com.bf.auth.resource;

import com.bf.auth.dto.LoginRequest;
import com.bf.auth.dto.LoginResponse;
import com.bf.auth.entity.AppUser;
import com.bf.auth.security.PasswordService;
import com.bf.auth.security.TokenService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

  @Inject PasswordService passwordService;
  @Inject TokenService tokenService;

  @POST
  @Path("/login")
  public Response login(@Valid LoginRequest req) {
    AppUser user = AppUser.findByUsernameOrEmail(req.login);
    if (user == null || !passwordService.matches(req.password, user.passwordHash)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    return Response.ok(new LoginResponse(tokenService.generate(user))).build();
  }

  // Endpoint opcional para crear usuario rápido en dev:
  @POST
  @Path("/seed-admin")
  @Transactional
  public Response seedAdmin() {
    AppUser existing = AppUser.find("username", "admin").firstResult();
    if (existing != null) return Response.ok().build();

    AppUser u = new AppUser();
    u.username = "admin";
    u.email = "admin@local";
    u.role = "ADMIN";
    u.passwordHash = passwordService.hash("admin123");
    u.persist();
    return Response.status(Response.Status.CREATED).build();
  }
}
