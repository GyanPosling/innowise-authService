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
import org.hibernate.annotations.ColumnDefault;

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
  @ColumnDefault("true")
  private boolean accountNotExpired = true;

  @Column(name = "accounts_not_locked", nullable = false)
  @ColumnDefault("true")
  private boolean accountsNotLocked = true;

  @Column(name = "credentials_not_expired", nullable = false)
  @ColumnDefault("true")
  private boolean credentialsNotExpired = true;

  @Column(name = "enabled", nullable = false)
  @ColumnDefault("true")
  private boolean enabled = true;

  @PrePersist
  private void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();

    if (role == null) {
      role = Role.USER;
    }
  }

  @PreUpdate
  private void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public boolean isAccountNonExpired() {
    return accountNotExpired;
  }

  public boolean isAccountNonLocked() {
    return accountsNotLocked;
  }

  public boolean isCredentialsNonExpired() {
    return credentialsNotExpired;
  }

  public boolean isEnabled() {
    return enabled;
  }
}
