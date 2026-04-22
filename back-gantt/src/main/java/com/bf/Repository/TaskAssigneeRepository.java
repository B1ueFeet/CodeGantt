package com.bf.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import com.bf.Model.TaskAssignee;

import java.math.BigDecimal;
import java.util.UUID;

@ApplicationScoped
public class TaskAssigneeRepository implements PanacheRepositoryBase<TaskAssignee, UUID> {

    public BigDecimal sumAssignedHoursByUserInProject(UUID projectId, UUID userId) {
        var res = (Object) find("SELECT COALESCE(SUM(t.assignedHours),0) FROM TaskAssignee t WHERE t.task.project.id = ?1 AND t.user.id = ?2", projectId, userId).firstResult();
        if (res == null) return BigDecimal.ZERO;
        if (res instanceof BigDecimal) return (BigDecimal) res;
        try { return new BigDecimal(res.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}
    