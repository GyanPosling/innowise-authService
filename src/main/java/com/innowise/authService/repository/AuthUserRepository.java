package com.innowise.authservice.repository;

import com.innowise.authservice.model.entity.AuthUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
  Optional<AuthUser> findByUsername(String username);

  Optional<AuthUser> findByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);
}
