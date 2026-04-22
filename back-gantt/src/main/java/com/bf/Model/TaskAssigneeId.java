package com.bf.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class TaskAssigneeId implements Serializable {

    @Column(name = "task_id", columnDefinition = "uuid")
    public UUID taskId;

    @Column(name = "user_id", columnDefinition = "uuid")
    public UUID userId;

    public TaskAssigneeId() {}

    public TaskAssigneeId(UUID taskId, UUID userId) {
        this.taskId = taskId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskAssigneeId that)) return false;
        return Objects.equals(taskId, that.taskId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, userId);
    }
}
