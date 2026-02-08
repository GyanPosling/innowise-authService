package com.innowise.authservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.authservice.exception.AuthUserNotFoundException;
import com.innowise.authservice.mapper.AuthUserMapper;
import com.innowise.authservice.model.dto.response.PromoteUserResponse;
import com.innowise.authservice.model.entity.AuthUser;
import com.innowise.authservice.model.entity.type.Role;
import com.innowise.authservice.repository.AuthUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

  @Mock
  private AuthUserRepository authUserRepository;
  @Mock
  private AuthUserMapper authUserMapper;

  @InjectMocks
  private AdminServiceImpl adminService;

  @Test
  void promoteToAdmin_whenUserNotFound_throwsException() {
    when(authUserRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(AuthUserNotFoundException.class, () -> adminService.promoteToAdmin(1L));
  }

  @Test
  void promoteToAdmin_whenAlreadyAdmin_doesNotSave() {
    AuthUser user = new AuthUser();
    user.setId(2L);
    user.setRole(Role.ADMIN);
    PromoteUserResponse expected = PromoteUserResponse.builder()
        .userId(2L)
        .role(Role.ADMIN)
        .build();
    when(authUserRepository.findById(2L)).thenReturn(Optional.of(user));
    when(authUserMapper.toPromoteUserResponse(user)).thenReturn(expected);

    PromoteUserResponse response = adminService.promoteToAdmin(2L);

    assertSame(expected, response);
    verify(authUserRepository, never()).save(user);
  }

  @Test
  void promoteToAdmin_whenUserRoleNotAdmin_updatesRoleAndSaves() {
    AuthUser user = new AuthUser();
    user.setId(3L);
    user.setRole(Role.USER);
    AuthUser savedUser = new AuthUser();
    savedUser.setId(3L);
    savedUser.setRole(Role.ADMIN);
    PromoteUserResponse expected = PromoteUserResponse.builder()
        .userId(3L)
        .role(Role.ADMIN)
        .build();
    when(authUserRepository.findById(3L)).thenReturn(Optional.of(user));
    when(authUserRepository.save(user)).thenReturn(savedUser);
    when(authUserMapper.toPromoteUserResponse(savedUser)).thenReturn(expected);

    PromoteUserResponse response = adminService.promoteToAdmin(3L);

    assertEquals(Role.ADMIN, user.getRole());
    verify(authUserRepository).save(user);
    assertSame(expected, response);
  }
}
