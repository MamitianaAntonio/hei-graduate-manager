package com.hei.app;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityTestController {
  @GetMapping("/api/auth/ping")
  public String ping() {
    return "pong";
  }

  @GetMapping("/api/protected")
  public String protectedEndpoint(@AuthenticationPrincipal UserDetails userDetails) {
    return userDetails.getUsername();
  }

  @PostMapping("/api/protected")
  public String protectedPost(@AuthenticationPrincipal UserDetails userDetails) {
    return userDetails.getUsername();
  }
}
