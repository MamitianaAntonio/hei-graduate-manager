package com.hei.app.controller;

import com.hei.app.dto.auth.LoginRequest;
import com.hei.app.dto.auth.LoginResponse;
import com.hei.app.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("/login")
  public LoginResponse login(@RequestBody LoginRequest request) {
    return authService.login(request.email(), request.password());
  }
}
