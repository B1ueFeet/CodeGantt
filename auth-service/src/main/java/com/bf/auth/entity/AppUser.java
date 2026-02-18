package com.bf.auth.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class AppUser extends PanacheEntityBase {

  @Id
  @Column(columnDefinition = "uuid")
  public UUID id;

  @Column(nullable = false, unique = true, length = 60)
  public String username;

  @Column(nullable = false, unique = true, length = 120)
  public String email;

  @Column(nullable = false, length = 255)
  public String passwordHash;

  @Column(nullable = false, length = 20)
  public String role; // ADMIN / USER

  @Column(nullable = false)
  public OffsetDateTime createdAt;

  @Column(nullable = false)
  public OffsetDateTime updatedAt;

  @PrePersist
  void prePersist() {
    if (id == null) id = UUID.randomUUID();
    createdAt = OffsetDateTime.now();
    updatedAt = createdAt;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = OffsetDateTime.now();
  }

  public static AppUser findByUsernameOrEmail(String login) {
    return find("username = ?1 or email = ?1", login).firstResult();
  }
}
