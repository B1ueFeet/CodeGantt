package com.bf.Model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "project_member")
public class ProjectMember extends PanacheEntityBase {

    @EmbeddedId
    public ProjectMemberId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("projectId")
    @JoinColumn(name = "project_id", nullable = false)
    public Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    public AppUser user;

    @Column(name = "workload_percent", nullable = false)
    public int workloadPercent; // 0..100

    @Column(name = "overtime_hours", nullable = false, precision = 10, scale = 2)
    public BigDecimal overtimeHours;

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        var now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (overtimeHours == null)
            overtimeHours = BigDecimal.ZERO;
        if (workloadPercent < 0)
            workloadPercent = 0;
        if (workloadPercent > 100)
            workloadPercent = 100;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
        if (workloadPercent < 0)
            workloadPercent = 0;
        if (workloadPercent > 100)
            workloadPercent = 100;
        if (overtimeHours == null)
            overtimeHours = BigDecimal.ZERO;
    }
}
