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
    PromoteUserResponse response = PromoteUserResponse.builder()
        .userId(5L)
        .username("user")
        .email("user@example.com")
        .role(Role.ADMIN)
        .build();
    when(adminService.promoteToAdmin(5L)).thenReturn(response);

    mockMvc.perform(post("/api/v1/admin/users/5/promote"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId", is(5)))
        .andExpect(jsonPath("$.username", is("user")))
        .andExpect(jsonPath("$.email", is("user@example.com")))
        .andExpect(jsonPath("$.role", is("ADMIN")));

    verify(adminService).promoteToAdmin(5L);
  }

  @Test
  void promoteToAdmin_whenUserMissing_returnsNotFound() throws Exception {
    doThrow(new AuthUserNotFoundException("id", "5"))
        .when(adminService).promoteToAdmin(5L);

    mockMvc.perform(post("/api/v1/admin/users/5/promote"))
        .andExpect(status().isNotFound());
  }
}
