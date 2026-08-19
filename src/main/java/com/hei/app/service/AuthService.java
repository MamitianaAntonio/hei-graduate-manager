package com.hei.app.service;

import com.hei.app.dto.auth.LoginResponse;
import com.hei.app.exceptions.BusinessException;
import com.hei.app.model.UserAccount;
import com.hei.app.repository.UserAccountRepository;
import com.hei.app.security.AppPrincipal;
import com.hei.app.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
  private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";

  private final UserAccountRepository userAccountRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public LoginResponse login(String email, String password) {
    UserAccount account =
        userAccountRepository
            .findByEmail(email)
            .orElseThrow(() -> new BusinessException(INVALID_CREDENTIALS_MESSAGE));

    if (!account.isActive()) {
      throw new BusinessException(INVALID_CREDENTIALS_MESSAGE);
    }

    if (!passwordEncoder.matches(password, account.getPasswordHash())) {
      throw new BusinessException(INVALID_CREDENTIALS_MESSAGE);
    }

    AppPrincipal principal =
        new AppPrincipal(account.getId(), account.getEmail(), account.getRole());
    String token = jwtService.generateToken(principal);
    return new LoginResponse(token);
  }
}
