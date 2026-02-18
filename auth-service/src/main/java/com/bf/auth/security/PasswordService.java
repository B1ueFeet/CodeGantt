package com.bf.auth.security;

import jakarta.enterprise.context.ApplicationScoped;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class PasswordService {

  public String hash(String raw) {
    return BCrypt.hashpw(raw, BCrypt.gensalt(12));
  }

  public boolean matches(String raw, String hash) {
    return raw != null && hash != null && BCrypt.checkpw(raw, hash);
  }
}
