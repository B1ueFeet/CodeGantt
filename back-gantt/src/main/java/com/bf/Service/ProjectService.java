package com.bf.Service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;
import com.bf.dtos.ProjectDTOs;
import com.bf.Model.AppUser;
import com.bf.Model.Project;
import com.bf.Model.ProjectMember;
import com.bf.Model.ProjectMemberId;
import com.bf.Repository.AppUserRepository;
import com.bf.Repository.ProjectMemberRepository;
import com.bf.Repository.ProjectRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProjectService {

    private final ProjectRepository projects;
    private final AppUserRepository users;
    private final ProjectMemberRepository members;
    private final JsonWebToken jwt;

    public ProjectService(
            ProjectRepository projects,
            AppUserRepository users,
            ProjectMemberRepository members,
            JsonWebToken jwt
    ) {
        this.projects = projects;
        this.users = users;
        this.members = members;
        this.jwt = jwt;
    }

    public List<ProjectDTOs.ProjectResponse> list() {
        return projects.listAll().stream().map(this::toResponse).toList();
    }

    public ProjectDTOs.ProjectResponse get(UUID id) {
        var p = projects.findById(id);
        if (p == null) {
            throw new IllegalArgumentException("Proyecto no existe");
        }
        return toResponse(p);
    }

    @Transactional
    public ProjectDTOs.ProjectResponse create(ProjectDTOs.ProjectCreateRequest req) {
        if (req.name() == null || req.name().isBlank()) {
            throw new IllegalArgumentException("El nombre del proyecto es obligatorio");
        }

        var owner = resolveCurrentUser();
        if (owner == null) {
            throw new IllegalArgumentException("Usuario autenticado no válido");
        }

        var p = new Project();
        p.id = UUID.randomUUID();
        p.name = req.name().trim();
        p.description = req.description();
        p.owner = owner;
        p.userHourLimit = req.userHourLimit() != null ? req.userHourLimit() : BigDecimal.valueOf(40);
        p.createdAt = OffsetDateTime.now();
        p.updatedAt = OffsetDateTime.now();

        projects.persist(p);
        return toResponse(p);
    }

    @Transactional
    public ProjectDTOs.ProjectResponse update(UUID id, ProjectDTOs.ProjectUpdateRequest req) {
        var p = projects.findById(id);
        if (p == null) {
            throw new IllegalArgumentException("Proyecto no existe");
        }

        if (req.name() != null) {
            if (req.name().isBlank()) {
                throw new IllegalArgumentException("El nombre del proyecto no puede estar vacío");
            }
            p.name = req.name().trim();
        }

        if (req.description() != null) {
            p.description = req.description();
        }

        if (req.userHourLimit() != null) {
            if (req.userHourLimit().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("userHourLimit no puede ser negativo");
            }
            p.userHourLimit = req.userHourLimit();
        }

        p.updatedAt = OffsetDateTime.now();

        return toResponse(p);
    }

    @Transactional
    public void delete(UUID id) {
        boolean deleted = projects.deleteById(id);
        if (!deleted) {
            throw new IllegalArgumentException("Proyecto no existe");
        }
    }

    public List<ProjectMember> listMembers(UUID projectId) {
        return members.listByProjectId(projectId);
    }

    @Transactional
    public void addMember(UUID projectId, ProjectDTOs.AddMemberRequest req) {
        var p = projects.findById(projectId);
        if (p == null) {
            throw new IllegalArgumentException("Proyecto no existe");
        }

        var u = users.findById(req.userId());
        if (u == null) {
            throw new IllegalArgumentException("Usuario no existe");
        }

        var id = new ProjectMemberId(projectId, req.userId());
        if (members.findById(id) != null) {
            throw new IllegalArgumentException("Ese usuario ya está asignado al proyecto");
        }

        var m = new ProjectMember();
        m.id = id;
        m.project = p;
        m.user = u;
        m.workloadPercent = Math.max(0, Math.min(100, req.workloadPercent()));
        m.overtimeHours = BigDecimal.valueOf(req.overtimeHours());
        m.createdAt = OffsetDateTime.now();
        m.updatedAt = OffsetDateTime.now();

        members.persist(m);
    }

    @Transactional
    public void removeMember(UUID projectId, UUID userId) {
        boolean deleted = members.deleteById(new ProjectMemberId(projectId, userId));
        if (!deleted) {
            throw new IllegalArgumentException("Asignación no existe");
        }
    }

    private AppUser resolveCurrentUser() {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            return null;
        }

        try {
            UUID userId = UUID.fromString(subject);
            return users.findById(userId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private ProjectDTOs.ProjectResponse toResponse(Project p) {
        return new ProjectDTOs.ProjectResponse(
                p.id,
                p.name,
                p.description,
                p.owner.id,
                p.userHourLimit
        );
    }
}