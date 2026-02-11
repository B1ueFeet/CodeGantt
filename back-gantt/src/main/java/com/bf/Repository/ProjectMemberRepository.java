package com.bf.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import com.bf.Model.ProjectMember;
import com.bf.Model.ProjectMemberId;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProjectMemberRepository implements PanacheRepositoryBase<ProjectMember, ProjectMemberId> {

    public List<ProjectMember> listByProjectId(UUID projectId) {
        return list("project.id", projectId);
    }

    public List<ProjectMember> listByUserId(UUID userId) {
        return list("user.id", userId);
    }
}
