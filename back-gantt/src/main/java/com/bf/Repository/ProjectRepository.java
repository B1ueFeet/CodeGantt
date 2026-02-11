package com.bf.Repository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import com.bf.Model.Project;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProjectRepository implements PanacheRepositoryBase<Project, UUID> {

    public List<Project> listByOwnerId(UUID ownerId) {
        return list("owner.id", ownerId);
    }
}
