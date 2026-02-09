package com.innowise.authservice.controller;

import com.innowise.authservice.model.dto.response.PromoteUserResponse;
import com.innowise.authservice.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Admin user management API")
public class AdminController {

  private final AdminService adminService;

  @Operation(summary = "Promote user to ADMIN")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User promoted"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  @PostMapping("/users/{id}/promote")
  public ResponseEntity<PromoteUserResponse> promoteToAdmin(@PathVariable Long id) {
    PromoteUserResponse response = adminService.promoteToAdmin(id);
    return ResponseEntity.ok(response);
  }
}
