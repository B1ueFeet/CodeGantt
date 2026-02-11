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
}
