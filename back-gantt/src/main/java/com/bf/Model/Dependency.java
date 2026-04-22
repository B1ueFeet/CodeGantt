package com.bf.Model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "dependency")
public class Dependency extends PanacheEntityBase {

    @Id
    @Column(columnDefinition = "uuid")
    public UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    public Project project;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "predecessor_id", nullable = false)
    public Task predecessor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "successor_id", nullable = false)
    public Task successor;

    @Column(name = "type", nullable = false, length = 10)
    public String type; // 'FS'

    @Column(name = "lag_minutes", nullable = false)
    public int lagMinutes;

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (type == null) type = "FS";
        createdAt = OffsetDateTime.now();
    }
}
