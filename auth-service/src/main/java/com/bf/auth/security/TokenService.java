package com.bf.auth.security;

import com.bf.auth.entity.AppUser;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class TokenService {

  @ConfigProperty(name = "mp.jwt.verify.issuer")
  String issuer;

  public String generate(AppUser user) {
    return Jwt.issuer(issuer)
      .subject(user.id.toString())
      .upn(user.username)
      .groups(Set.of(user.role))          // roles via groups (RBAC)
      .claim("email", user.email)
      .expiresIn(Duration.ofHours(8))
      .sign();
  }
}
