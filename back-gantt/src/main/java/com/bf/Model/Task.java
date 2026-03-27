package com.bf.Model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "task")
public class Task extends PanacheEntityBase {

    @Id
    @Column(columnDefinition = "uuid")
    public UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    public Project project;

    @Column(nullable = false, length = 200)
    public String name;

    @Column(columnDefinition = "text")
    public String description;

    @Column(name = "start_at", nullable = false)
    public OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    public OffsetDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    public TaskStatus status;

    @Column(nullable = false)
    public int progress; // 0..100

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        var now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (startAt == null) {
            startAt = now;
        }

        if (endAt == null) {
            endAt = startAt.plusHours(2);
        }

        if (status == null) {
            status = TaskStatus.TODO;
        }

        if (progress < 0) {
            progress = 0;
        }

        if (progress > 100) {
            progress = 100;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();

        if (progress < 0) {
            progress = 0;
        }

        if (progress > 100) {
            progress = 100;
        }
    }
}