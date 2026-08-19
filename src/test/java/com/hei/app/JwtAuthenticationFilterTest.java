package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.app.model.Role;
import com.hei.app.security.AppPrincipal;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CustomUserDetailsService;
import com.hei.app.security.JwtAuthenticationFilter;
import com.hei.app.security.JwtService;
import jakarta.servlet.FilterChain;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {
  @Mock private JwtService jwtService;

  @Mock private CustomUserDetailsService customUserDetailsService;

  @InjectMocks private JwtAuthenticationFilter jwtAuthenticationFilter;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private FilterChain filterChain;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    filterChain = mock(FilterChain.class);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldAuthenticateRequestWithValidBearerToken() throws Exception {
    AppUserDetails userDetails = userDetails();
    when(jwtService.validateToken("valid.token")).thenReturn(true);
    when(jwtService.extractUsername("valid.token")).thenReturn("student@hei.com");
    when(customUserDetailsService.loadUserByUsername("student@hei.com")).thenReturn(userDetails);
    request.addHeader("Authorization", "Bearer valid.token");

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertEquals(userDetails, authentication.getPrincipal());
  }

  @Test
  void shouldNotAuthenticateWithoutAuthorizationHeader() throws Exception {
    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldNotAuthenticateWithInvalidBearerToken() throws Exception {
    when(jwtService.validateToken("invalid")).thenReturn(false);
    request.addHeader("Authorization", "Bearer invalid");

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(customUserDetailsService, never()).loadUserByUsername("invalid");
  }

  @Test
  void shouldNotAuthenticateWithMalformedAuthorizationHeader() throws Exception {
    request.addHeader("Authorization", "Basic abcdef");

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(jwtService, never()).validateToken("abcdef");
  }

  @Test
  void shouldLoadUserFromSubject() throws Exception {
    AppUserDetails userDetails = userDetails();
    when(jwtService.validateToken("valid.token")).thenReturn(true);
    when(jwtService.extractUsername("valid.token")).thenReturn("student@hei.com");
    when(customUserDetailsService.loadUserByUsername("student@hei.com")).thenReturn(userDetails);
    request.addHeader("Authorization", "Bearer valid.token");

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    verify(customUserDetailsService).loadUserByUsername("student@hei.com");
  }

  @Test
  void shouldSetAuthenticationInSecurityContext() throws Exception {
    AppUserDetails userDetails = userDetails();
    when(jwtService.validateToken("valid.token")).thenReturn(true);
    when(jwtService.extractUsername("valid.token")).thenReturn("student@hei.com");
    when(customUserDetailsService.loadUserByUsername("student@hei.com")).thenReturn(userDetails);
    request.addHeader("Authorization", "Bearer valid.token");

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertTrue(authentication instanceof UsernamePasswordAuthenticationToken);
    assertEquals(userDetails, authentication.getPrincipal());
    assertTrue(
        authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_STUDENT")));
  }

  private AppUserDetails userDetails() {
    return new AppUserDetails(
        new AppPrincipal(UUID.randomUUID(), "student@hei.com", Role.STUDENT), "hash", true);
  }
}
