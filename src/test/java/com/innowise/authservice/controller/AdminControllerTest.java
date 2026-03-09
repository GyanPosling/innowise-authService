package com.innowise.authservice.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.innowise.authservice.exception.AuthUserNotFoundException;
import com.innowise.authservice.exception.GlobalExceptionHandler;
import com.innowise.authservice.model.dto.response.PromoteUserResponse;
import com.innowise.authservice.model.entity.type.Role;
import com.innowise.authservice.service.AdminService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

  private MockMvc mockMvc;

  @Mock
  private AdminService adminService;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new AdminController(adminService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void promoteToAdmin_returnsResponse() throws Exception {
    UUID userId = UUID.randomUUID();
    PromoteUserResponse response = PromoteUserResponse.builder()
        .userId(userId)
        .username("user")
        .email("user@example.com")
        .role(Role.ADMIN)
        .build();
    when(adminService.promoteToAdmin(userId)).thenReturn(response);

    mockMvc.perform(post("/api/v1/admin/users/" + userId + "/promote"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId", is(userId.toString())))
        .andExpect(jsonPath("$.username", is("user")))
        .andExpect(jsonPath("$.email", is("user@example.com")))
        .andExpect(jsonPath("$.role", is("ADMIN")));

    verify(adminService).promoteToAdmin(userId);
  }

  @Test
  void promoteToAdmin_whenUserMissing_returnsNotFound() throws Exception {
    UUID userId = UUID.randomUUID();
    doThrow(new AuthUserNotFoundException("id: " + userId))
        .when(adminService).promoteToAdmin(userId);

    mockMvc.perform(post("/api/v1/admin/users/" + userId + "/promote"))
        .andExpect(status().isNotFound());
  }
}
