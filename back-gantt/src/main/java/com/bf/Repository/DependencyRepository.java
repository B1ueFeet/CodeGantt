package com.bf.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import com.bf.Model.Dependency;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DependencyRepository implements PanacheRepositoryBase<Dependency, UUID> {

    public List<Dependency> listByProjectId(UUID projectId) {
        return list("project.id", projectId);
    }

    public List<Dependency> listSuccessors(UUID taskId) {
        return list("predecessor.id", taskId);
    }

    public List<Dependency> listPredecessors(UUID taskId) {
        return list("successor.id", taskId);
    }
}
