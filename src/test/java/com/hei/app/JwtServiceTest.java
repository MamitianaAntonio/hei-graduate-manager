package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hei.app.model.Role;
import com.hei.app.security.AppPrincipal;
import com.hei.app.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

public class JwtServiceTest {
  private static final String SECRET = "test-secret-key-that-is-long-enough-for-hs256-signing";
  private static final long EXPIRATION_MS = 3_600_000L;

  private final JwtService jwtService = new JwtService(SECRET, EXPIRATION_MS);

  @Test
  void generateToken_shouldCreateValidToken() {
    String token = jwtService.generateToken(principal());

    assertNotNull(token);
    assertTrue(jwtService.validateToken(token));
  }

  @Test
  void generateToken_shouldContainExpectedSubject() {
    AppPrincipal principal = principal();

    String token = jwtService.generateToken(principal);

    assertEquals(principal.email(), jwtService.extractUsername(token));
  }

  @Test
  void generateToken_shouldContainAccountId() {
    AppPrincipal principal = principal();

    String token = jwtService.generateToken(principal);

    assertEquals(principal.accountId(), jwtService.extractAccountId(token));
  }

  @Test
  void generateToken_shouldContainRole() {
    AppPrincipal principal = principal();

    String token = jwtService.generateToken(principal);

    assertEquals(Role.STUDENT, jwtService.extractRole(token));
  }

  @Test
  void validateToken_shouldReturnTrueForValidToken() {
    String token = jwtService.generateToken(principal());

    assertTrue(jwtService.validateToken(token));
  }

  @Test
  void validateToken_shouldRejectInvalidToken() {
    assertFalse(jwtService.validateToken("invalid.token.value"));
  }

  @Test
  void validateToken_shouldRejectExpiredToken() {
    SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    String expiredToken =
        Jwts.builder()
            .subject("student@hei.com")
            .issuedAt(Date.from(Instant.now().minusSeconds(7_200)))
            .expiration(Date.from(Instant.now().minusSeconds(3_600)))
            .signWith(key)
            .compact();

    assertFalse(jwtService.validateToken(expiredToken));
  }

  @Test
  void extractUsername_shouldReturnEmail() {
    String token = jwtService.generateToken(principal());

    assertEquals("student@hei.com", jwtService.extractUsername(token));
  }

  private AppPrincipal principal() {
    return new AppPrincipal(UUID.randomUUID(), "student@hei.com", Role.STUDENT);
  }
}
