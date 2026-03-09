package com.innowise.authservice.controller;

import com.innowise.authservice.controller.api.AdminControllerApi;
import com.innowise.authservice.model.dto.response.PromoteUserResponse;
import com.innowise.authservice.service.AdminService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController implements AdminControllerApi {

  private final AdminService adminService;

  @PostMapping("/users/{id}/promote")
  @Override
  public ResponseEntity<PromoteUserResponse> promoteToAdmin(@PathVariable UUID id) {
    PromoteUserResponse response = adminService.promoteToAdmin(id);
    return ResponseEntity.ok(response);
  }
}
