package com.innowise.authservice.service;

import com.innowise.authservice.model.dto.response.PromoteUserResponse;

public interface AdminService {

  PromoteUserResponse promoteToAdmin(Long userId);
}
