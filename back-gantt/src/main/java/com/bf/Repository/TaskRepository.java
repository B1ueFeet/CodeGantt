package com.bf.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import com.bf.Model.Task;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TaskRepository implements PanacheRepositoryBase<Task, UUID> {

    public List<Task> listByProjectId(UUID projectId) {
        return list("project.id", projectId);
    }
}
