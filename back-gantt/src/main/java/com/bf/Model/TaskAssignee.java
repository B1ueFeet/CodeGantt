package com.bf.Model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "task_assignee")
public class TaskAssignee extends PanacheEntityBase {

    @EmbeddedId
    public TaskAssigneeId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("taskId")
    @JoinColumn(name = "task_id", nullable = false)
    public Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    public AppUser user;

    @Column(name = "assigned_hours", nullable = false, precision = 10, scale = 2)
    public BigDecimal assignedHours;

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        var now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (assignedHours == null) assignedHours = BigDecimal.ZERO;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
        if (assignedHours == null) assignedHours = BigDecimal.ZERO;
    }
}
