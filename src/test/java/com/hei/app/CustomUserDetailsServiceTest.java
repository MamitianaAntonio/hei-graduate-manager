package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.hei.app.model.Role;
import com.hei.app.model.UserAccount;
import com.hei.app.repository.UserAccountRepository;
import com.hei.app.security.AppPrincipal;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CustomUserDetailsService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {
  @Mock private UserAccountRepository userAccountRepository;

  @InjectMocks private CustomUserDetailsService customUserDetailsService;

  @Test
  void loadUserByUsername_shouldReturnPrincipal_whenUserExists() {
    UUID accountId = UUID.randomUUID();
    UserAccount account = userAccountWith(accountId, "student@hei.com", Role.STUDENT, true);
    when(userAccountRepository.findByEmail("student@hei.com")).thenReturn(Optional.of(account));

    AppUserDetails details =
        (AppUserDetails) customUserDetailsService.loadUserByUsername("student@hei.com");

    AppPrincipal principal = details.principal();
    assertEquals(accountId, principal.accountId());
    assertEquals("student@hei.com", principal.email());
    assertEquals(Role.STUDENT, principal.role());
  }

  @Test
  void loadUserByUsername_shouldThrow_whenUserDoesNotExist() {
    when(userAccountRepository.findByEmail("missing@hei.com")).thenReturn(Optional.empty());

    assertThrows(
        UsernameNotFoundException.class,
        () -> customUserDetailsService.loadUserByUsername("missing@hei.com"));
  }

  @Test
  void loadUserByUsername_shouldDisableUser_whenAccountIsInactive() {
    UserAccount account =
        userAccountWith(UUID.randomUUID(), "disabled@hei.com", Role.TEACHER, false);
    when(userAccountRepository.findByEmail("disabled@hei.com")).thenReturn(Optional.of(account));

    UserDetails details = customUserDetailsService.loadUserByUsername("disabled@hei.com");

    assertFalse(details.isEnabled());
  }

  @Test
  void loadUserByUsername_shouldHaveStudentRole() {
    UserAccount account = userAccountWith(UUID.randomUUID(), "student@hei.com", Role.STUDENT, true);
    when(userAccountRepository.findByEmail("student@hei.com")).thenReturn(Optional.of(account));

    UserDetails details = customUserDetailsService.loadUserByUsername("student@hei.com");

    assertTrue(details.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT")));
  }

  @Test
  void loadUserByUsername_shouldHaveTeacherRole() {
    UserAccount account = userAccountWith(UUID.randomUUID(), "teacher@hei.com", Role.TEACHER, true);
    when(userAccountRepository.findByEmail("teacher@hei.com")).thenReturn(Optional.of(account));

    UserDetails details = customUserDetailsService.loadUserByUsername("teacher@hei.com");

    assertTrue(details.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER")));
  }

  @Test
  void loadUserByUsername_shouldHaveAdminRole() {
    UserAccount account = userAccountWith(UUID.randomUUID(), "admin@hei.com", Role.ADMIN, true);
    when(userAccountRepository.findByEmail("admin@hei.com")).thenReturn(Optional.of(account));

    UserDetails details = customUserDetailsService.loadUserByUsername("admin@hei.com");

    assertTrue(details.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
  }

  private UserAccount userAccountWith(UUID id, String email, Role role, boolean active) {
    UserAccount account = new UserAccount();
    account.setId(id);
    account.setEmail(email);
    account.setPasswordHash("hash");
    account.setRole(role);
    account.setActive(active);
    return account;
  }
}
