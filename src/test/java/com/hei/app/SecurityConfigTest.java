package com.hei.app;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hei.app.model.Role;
import com.hei.app.security.AppPrincipal;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CustomUserDetailsService;
import com.hei.app.security.JwtService;
import com.hei.app.security.SecurityConfig;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SecurityTestController.class)
@Import(SecurityConfig.class)
public class SecurityConfigTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private JwtService jwtService;

  @MockBean private CustomUserDetailsService customUserDetailsService;

  @MockBean private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void authEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
    mockMvc
        .perform(get("/api/auth/ping"))
        .andExpect(status().isOk())
        .andExpect(content().string("pong"));
  }

  @Test
  void protectedEndpoint_shouldRedirectToLoginWithoutJwt() throws Exception {
    mockMvc
        .perform(get("/api/protected"))
        .andExpect(status().isFound())
        .andExpect(redirectedUrl("http://localhost/login"));
  }

  @Test
  void protectedEndpoint_shouldBeAuthenticatedWithValidJwt() throws Exception {
    stubValidToken("valid.token", "student@hei.com");

    mockMvc
        .perform(get("/api/protected").header("Authorization", "Bearer valid.token"))
        .andExpect(status().isOk())
        .andExpect(content().string("student@hei.com"));
  }

  @Test
  void sessionPolicy_shouldBeStateless() throws Exception {
    stubValidToken("valid.token", "student@hei.com");

    mockMvc
        .perform(get("/api/protected").header("Authorization", "Bearer valid.token"))
        .andExpect(status().isOk())
        .andExpect(cookie().doesNotExist("JSESSIONID"));
  }

  @Test
  void csrf_shouldBeDisabled() throws Exception {
    stubValidToken("valid.token", "student@hei.com");

    mockMvc
        .perform(post("/api/protected").header("Authorization", "Bearer valid.token"))
        .andExpect(status().isOk())
        .andExpect(content().string("student@hei.com"));
  }

  @Test
  void httpBasic_shouldNotAuthenticate() throws Exception {
    mockMvc
        .perform(get("/api/protected").header("Authorization", "Basic dXNlcjpwYXNz"))
        .andExpect(status().isFound())
        .andExpect(redirectedUrl("http://localhost/login"));
  }

  @Test
  void formLogin_shouldRedirectToLogin() throws Exception {
    mockMvc
        .perform(get("/api/protected"))
        .andExpect(status().isFound())
        .andExpect(redirectedUrl("http://localhost/login"));
  }

  private void stubValidToken(String token, String email) {
    AppUserDetails userDetails =
        new AppUserDetails(new AppPrincipal(UUID.randomUUID(), email, Role.STUDENT), "hash", true);
    when(jwtService.validateToken(token)).thenReturn(true);
    when(jwtService.extractUsername(token)).thenReturn(email);
    when(customUserDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
  }
}
