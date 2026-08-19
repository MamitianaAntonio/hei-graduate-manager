package com.hei.app;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hei.app.controller.AuthController;
import com.hei.app.dto.auth.LoginResponse;
import com.hei.app.exceptions.BusinessException;
import com.hei.app.security.CustomUserDetailsService;
import com.hei.app.security.JwtService;
import com.hei.app.security.SecurityConfig;
import com.hei.app.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private AuthService authService;

  @MockBean private JwtService jwtService;

  @MockBean private CustomUserDetailsService customUserDetailsService;

  @MockBean private PasswordEncoder passwordEncoder;

  @Test
  void login_shouldReturn200AndToken_whenCredentialsAreValid() throws Exception {
    when(authService.login("student@hei.com", "password"))
        .thenReturn(new LoginResponse("jwt-token"));

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"student@hei.com\",\"password\":\"password\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("jwt-token"));
  }

  @Test
  void login_shouldRejectInvalidCredentials() throws Exception {
    when(authService.login("student@hei.com", "wrong-password"))
        .thenThrow(new BusinessException("Invalid email or password"));

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"student@hei.com\",\"password\":\"wrong-password\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid email or password"));
  }

  @Test
  void login_shouldValidateRequestBody() throws Exception {
    mockMvc
        .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(""))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_shouldDelegateToAuthService() throws Exception {
    when(authService.login("student@hei.com", "password"))
        .thenReturn(new LoginResponse("jwt-token"));

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"student@hei.com\",\"password\":\"password\"}"))
        .andExpect(status().isOk());

    verify(authService).login("student@hei.com", "password");
  }
}
