package com.hei.app.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class AdminAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    if (authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
      response.sendRedirect("/promotions");
      return;
    }

    SecurityContextHolder.clearContext();
    if (request.getSession(false) != null) {
      request.getSession(false).invalidate();
    }
    response.sendRedirect("/login?admin=true");
  }
}
