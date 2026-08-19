package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.app.dto.auth.LoginResponse;
import com.hei.app.exceptions.BusinessException;
import com.hei.app.model.Role;
import com.hei.app.model.UserAccount;
import com.hei.app.repository.UserAccountRepository;
import com.hei.app.security.AppPrincipal;
import com.hei.app.security.JwtService;
import com.hei.app.service.AuthService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
  @Mock private UserAccountRepository userAccountRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private JwtService jwtService;

  @InjectMocks private AuthService authService;

  @Test
  void login_shouldReturnToken_whenCredentialsAreValid() {
    UserAccount account = accountWith("student@hei.com", "hash", true, Role.STUDENT);
    when(userAccountRepository.findByEmail("student@hei.com")).thenReturn(Optional.of(account));
    when(passwordEncoder.matches("password", "hash")).thenReturn(true);
    when(jwtService.generateToken(any(AppPrincipal.class))).thenReturn("jwt-token");

    LoginResponse response = authService.login("student@hei.com", "password");

    assertEquals("jwt-token", response.token());
  }

  @Test
  void login_shouldRejectUnknownEmail() {
    when(userAccountRepository.findByEmail("unknown@hei.com")).thenReturn(Optional.empty());

    assertThrows(BusinessException.class, () -> authService.login("unknown@hei.com", "password"));

    verify(passwordEncoder, never()).matches(any(), any());
    verify(jwtService, never()).generateToken(any());
  }

  @Test
  void login_shouldRejectInvalidPassword() {
    UserAccount account = accountWith("student@hei.com", "hash", true, Role.STUDENT);
    when(userAccountRepository.findByEmail("student@hei.com")).thenReturn(Optional.of(account));
    when(passwordEncoder.matches("wrong-password", "hash")).thenReturn(false);

    assertThrows(
        BusinessException.class, () -> authService.login("student@hei.com", "wrong-password"));

    verify(jwtService, never()).generateToken(any());
  }

  @Test
  void login_shouldRejectInactiveAccount() {
    UserAccount account = accountWith("student@hei.com", "hash", false, Role.STUDENT);
    when(userAccountRepository.findByEmail("student@hei.com")).thenReturn(Optional.of(account));

    assertThrows(BusinessException.class, () -> authService.login("student@hei.com", "password"));

    verify(passwordEncoder, never()).matches(any(), any());
    verify(jwtService, never()).generateToken(any());
  }

  @Test
  void login_shouldGenerateTokenWithCorrectPrincipal() {
    UUID accountId = UUID.randomUUID();
    UserAccount account = accountWith(accountId, "teacher@hei.com", "hash", true, Role.TEACHER);
    when(userAccountRepository.findByEmail("teacher@hei.com")).thenReturn(Optional.of(account));
    when(passwordEncoder.matches("password", "hash")).thenReturn(true);
    when(jwtService.generateToken(any(AppPrincipal.class))).thenReturn("jwt-token");

    authService.login("teacher@hei.com", "password");

    ArgumentCaptor<AppPrincipal> principalCaptor = ArgumentCaptor.forClass(AppPrincipal.class);
    verify(jwtService).generateToken(principalCaptor.capture());
    AppPrincipal principal = principalCaptor.getValue();
    assertEquals(accountId, principal.accountId());
    assertEquals("teacher@hei.com", principal.email());
    assertEquals(Role.TEACHER, principal.role());
  }

  private UserAccount accountWith(String email, String passwordHash, boolean active, Role role) {
    return accountWith(UUID.randomUUID(), email, passwordHash, active, role);
  }

  private UserAccount accountWith(
      UUID id, String email, String passwordHash, boolean active, Role role) {
    UserAccount account = new UserAccount();
    account.setId(id);
    account.setEmail(email);
    account.setPasswordHash(passwordHash);
    account.setRole(role);
    account.setActive(active);
    return account;
  }
}
