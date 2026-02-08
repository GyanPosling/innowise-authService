package com.innowise.authservice.config;

import com.innowise.authservice.model.entity.AuthUser;
import com.innowise.authservice.model.entity.type.Role;
import com.innowise.authservice.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements ApplicationRunner {

  private final AuthUserRepository authUserRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${admin.username:}")
  private String adminUsername;

  @Value("${admin.email:}")
  private String adminEmail;

  @Value("${admin.password:}")
  private String adminPassword;

  @Override
  public void run(ApplicationArguments args) {
    if (isBlank(adminUsername) || isBlank(adminEmail) || isBlank(adminPassword)) {
      log.info("Admin seed skipped: missing admin credentials");
      return;
    }
    if (authUserRepository.existsByUsername(adminUsername)
        || authUserRepository.existsByEmail(adminEmail)) {
      return;
    }

    AuthUser admin = new AuthUser();
    admin.setUsername(adminUsername);
    admin.setEmail(adminEmail);
    admin.setPassword(passwordEncoder.encode(adminPassword));
    admin.setRole(Role.ADMIN);
    authUserRepository.save(admin);

    log.info("Admin user created: {}", adminUsername);
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
