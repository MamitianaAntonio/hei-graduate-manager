package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderTest {
  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Test
  void encode_shouldReturnDifferentHashFromPlainPassword() {
    String hash = passwordEncoder.encode("secret");

    assertNotEquals("secret", hash);
  }

  @Test
  void matches_shouldReturnTrueForCorrectPassword() {
    String hash = passwordEncoder.encode("secret");

    assertTrue(passwordEncoder.matches("secret", hash));
  }

  @Test
  void matches_shouldReturnFalseForIncorrectPassword() {
    String hash = passwordEncoder.encode("secret");

    assertFalse(passwordEncoder.matches("wrong", hash));
  }
}
