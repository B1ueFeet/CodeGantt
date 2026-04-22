package com.bf.Repository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import com.bf.Model.AppUser;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AppUserRepository implements PanacheRepositoryBase<AppUser, UUID> {

    public Optional<AppUser> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public Optional<AppUser> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<AppUser> findByFullName(String fullName) {
        if (fullName == null) return Optional.empty();
        var parts = fullName.trim().split("\\s+");
        if (parts.length < 2) return Optional.empty();
        String first = parts[0];
        String last = parts[parts.length-1];
        return find("firstName = ?1 and lastName = ?2", first, last).firstResultOptional();
    }
}
