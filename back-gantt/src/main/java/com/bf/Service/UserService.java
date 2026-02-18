package com.bf.Service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import com.bf.dtos.UserDTOs;
import com.bf.Model.AppUser;
import com.bf.Model.UserRole;
import com.bf.Repository.AppUserRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class UserService {

    private final AppUserRepository users;

    public UserService(AppUserRepository users) {
        this.users = users;
    }

    public List<UserDTOs.UserResponse> list() {
        return users.listAll().stream().map(this::toResponse).toList();
    }

    public UserDTOs.UserResponse get(UUID id) {
        var u = users.findById(id);
        if (u == null)
            throw new IllegalArgumentException("Usuario no existe");
        return toResponse(u);
    }

    @Transactional
    public UserDTOs.UserResponse create(UserDTOs.UserCreateRequest req) {
        users.findByUsername(req.username()).ifPresent(x -> {
            throw new IllegalArgumentException("username ya existe");
        });
        users.findByEmail(req.email()).ifPresent(x -> {
            throw new IllegalArgumentException("email ya existe");
        });

        var u = new AppUser();
        u.id = UUID.randomUUID();
        u.role = req.role() != null ? req.role() : UserRole.USER;
        u.firstName = req.firstName();
        u.lastName = req.lastName();
        u.username = req.username();
        u.email = req.email();
        u.passwordHash = BCrypt.hashpw(req.password(), BCrypt.gensalt(10));
        u.createdAt = OffsetDateTime.now();
        u.updatedAt = OffsetDateTime.now();

        users.persist(u);
        return toResponse(u);
    }

    @Transactional
    public UserDTOs.UserResponse update(UUID id, UserDTOs.UserUpdateRequest req) {
        var u = users.findById(id);
        if (u == null)
            throw new IllegalArgumentException("Usuario no existe");

        if (req.role() != null)
            u.role = req.role();
        if (req.firstName() != null)
            u.firstName = req.firstName();
        if (req.lastName() != null)
            u.lastName = req.lastName();

        if (req.email() != null && !req.email().equals(u.email)) {
            users.findByEmail(req.email()).ifPresent(x -> {
                throw new IllegalArgumentException("email ya existe");
            });
            u.email = req.email();
        }

        u.updatedAt = OffsetDateTime.now();
        return toResponse(u);
    }

    @Transactional
    public void delete(UUID id) {
        boolean deleted = users.deleteById(id);
        if (!deleted)
            throw new IllegalArgumentException("Usuario no existe");
    }

    private UserDTOs.UserResponse toResponse(AppUser u) {
        return new UserDTOs.UserResponse(u.id, u.role, u.firstName, u.lastName, u.username, u.email);
    }
}
