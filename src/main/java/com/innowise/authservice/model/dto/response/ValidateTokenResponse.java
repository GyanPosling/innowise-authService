package com.innowise.authservice.model.dto.response;

import com.innowise.authservice.model.entity.type.Role;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenResponse {

    private boolean valid;
    private UUID userId;
    private String username;
    private String email;
    private Role role;
}
