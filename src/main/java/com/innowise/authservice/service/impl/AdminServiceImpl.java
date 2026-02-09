package com.innowise.authservice.service.impl;

import com.innowise.authservice.exception.AuthUserNotFoundException;
import com.innowise.authservice.mapper.AuthUserMapper;
import com.innowise.authservice.model.dto.response.PromoteUserResponse;
import com.innowise.authservice.model.entity.AuthUser;
import com.innowise.authservice.model.entity.type.Role;
import com.innowise.authservice.repository.AuthUserRepository;
import com.innowise.authservice.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

  private final AuthUserRepository authUserRepository;
  private final AuthUserMapper authUserMapper;

  @Override
  public PromoteUserResponse promoteToAdmin(Long userId) {
    AuthUser user = authUserRepository.findById(userId)
        .orElseThrow(() -> new AuthUserNotFoundException("id", String.valueOf(userId)));
    if (user.getRole() == Role.ADMIN) {
      return authUserMapper.toPromoteUserResponse(user);
    }
    user.setRole(Role.ADMIN);
    AuthUser savedUser = authUserRepository.save(user);
    return authUserMapper.toPromoteUserResponse(savedUser);
  }
}
