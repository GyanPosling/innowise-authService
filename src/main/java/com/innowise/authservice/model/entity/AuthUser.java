package com.innowise.authservice.model.entity;

import com.innowise.authservice.model.entity.type.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auth_users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "username", unique = true, nullable = false, length = 255)
  private String username;

  @Column(name = "password", nullable = false, length = 255)
  private String password;

  @Column(name = "email", unique = true, nullable = false, length = 255)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private Role role;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "account_not_expired", nullable = false)
  private Boolean accountNotExpired;

  @Column(name = "accounts_not_locked", nullable = false)
  private Boolean accountsNotLocked;

  @Column(name = "credentials_not_expired", nullable = false)
  private Boolean credentialsNotExpired;

  @Column(name = "enabled", nullable = false)
  private Boolean enabled;

  @PrePersist
  private void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();

    if (accountNotExpired == null) {
      accountNotExpired = true;
    }
    if (accountsNotLocked == null) {
      accountsNotLocked = true;
    }
    if (credentialsNotExpired == null) {
      credentialsNotExpired = true;
    }
    if (enabled == null) {
      enabled = true;
    }
    if (role == null) {
      role = Role.USER;
    }
  }

  @PreUpdate
  private void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public boolean isAccountNonExpired() {
    return Boolean.TRUE.equals(accountNotExpired);
  }

  public boolean isAccountNonLocked() {
    return Boolean.TRUE.equals(accountsNotLocked);
  }

  public boolean isCredentialsNonExpired() {
    return Boolean.TRUE.equals(credentialsNotExpired);
  }

  public boolean isEnabled() {
    return Boolean.TRUE.equals(enabled);
  }
}
