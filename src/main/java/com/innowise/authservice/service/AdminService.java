package com.innowise.authservice.service;

import com.innowise.authservice.model.dto.response.PromoteUserResponse;
import java.util.UUID;

public interface AdminService {

  PromoteUserResponse promoteToAdmin(UUID userId);
}
