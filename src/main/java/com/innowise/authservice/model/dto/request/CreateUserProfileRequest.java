package com.innowise.authservice.model.dto.request;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserProfileRequest {

  private UUID id;
  private String name;
  private String surname;
  private LocalDate birthDate;
  private String email;
}
