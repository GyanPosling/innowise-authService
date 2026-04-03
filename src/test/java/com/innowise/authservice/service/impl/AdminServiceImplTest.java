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
import java.util.UUID;
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
    UUID userId = UUID.randomUUID();
    when(authUserRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(AuthUserNotFoundException.class, () -> adminService.promoteToAdmin(userId));
  }

  @Test
  void promoteToAdmin_whenAlreadyAdmin_doesNotSave() {
    UUID userId = UUID.randomUUID();
    AuthUser user = new AuthUser();
    user.setId(userId);
    user.setRole(Role.ADMIN);
    PromoteUserResponse expected = PromoteUserResponse.builder()
        .userId(userId)
        .role(Role.ADMIN)
        .build();
    when(authUserRepository.findById(userId)).thenReturn(Optional.of(user));
    when(authUserMapper.toPromoteUserResponse(user)).thenReturn(expected);

    PromoteUserResponse response = adminService.promoteToAdmin(userId);

        assertSame(expected, response);
        verify(authUserRepository, never()).save(user);
    }

  @Test
  void promoteToAdmin_whenUserRoleNotAdmin_updatesRoleAndSaves() {
    UUID userId = UUID.randomUUID();
    AuthUser user = new AuthUser();
    user.setId(userId);
    user.setRole(Role.USER);
    AuthUser savedUser = new AuthUser();
    savedUser.setId(userId);
    savedUser.setRole(Role.ADMIN);
    PromoteUserResponse expected = PromoteUserResponse.builder()
        .userId(userId)
        .role(Role.ADMIN)
        .build();
    when(authUserRepository.findById(userId)).thenReturn(Optional.of(user));
    when(authUserRepository.save(user)).thenReturn(savedUser);
    when(authUserMapper.toPromoteUserResponse(savedUser)).thenReturn(expected);

    PromoteUserResponse response = adminService.promoteToAdmin(userId);

        assertEquals(Role.ADMIN, user.getRole());
        verify(authUserRepository).save(user);
        assertSame(expected, response);
    }
}
