package com.hei.app;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.hei.app.conf.FacadeIT;
import com.hei.app.model.Promotion;
import com.hei.app.model.Role;
import com.hei.app.model.UserAccount;
import com.hei.app.repository.PromotionRepository;
import com.hei.app.repository.UserAccountRepository;
import com.hei.app.security.AppPrincipal;
import com.hei.app.security.JwtService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class PromotionIT extends FacadeIT {
  @Autowired private MockMvc mockMvc;

  @Autowired private PromotionRepository promotionRepository;

  @Autowired private UserAccountRepository userAccountRepository;

  @Autowired private JwtService jwtService;

  @Autowired private Flyway flyway;

  @BeforeEach
  void cleanPromotions() {
    promotionRepository.deleteAll();
  }

  @Test
  void flyway_shouldApplyDatabaseMigrations() {
    assertTrue(flyway.info().applied().length >= 3);
    assertEquals(0, promotionRepository.count());
  }

  @Test
  void admin_shouldCreateAndReadPromotionThroughControllerAndDatabase() throws Exception {
    String token = adminToken();

    mockMvc
        .perform(
            post("/api/promotions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"year\":2099}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.year").value(2099));

    Promotion promotion = promotionRepository.findByYear(2099).orElseThrow();

    mockMvc
        .perform(get("/api/promotions").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(promotion.getId().toString()))
        .andExpect(jsonPath("$[0].year").value(2099));

    mockMvc
        .perform(get("/promotions").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(view().name("promotions"))
        .andExpect(content().string(containsString("2099")))
        .andExpect(
            content()
                .string(
                    containsString("/api/promotions/" + promotion.getId() + "/graduates/export")));
  }

  @Test
  void admin_shouldRejectDuplicatePromotionThroughControllerAndDatabase() throws Exception {
    String token = adminToken();
    Promotion existing = new Promotion();
    existing.setYear(2098);
    promotionRepository.save(existing);

    mockMvc
        .perform(
            post("/api/promotions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"year\":2098}"))
        .andExpect(status().isConflict());
  }

  private String adminToken() {
    UserAccount account =
        userAccountRepository
            .findByEmail("integration-admin@hei.com")
            .orElseGet(
                () -> {
                  UserAccount newAccount = new UserAccount();
                  newAccount.setEmail("integration-admin@hei.com");
                  newAccount.setPasswordHash("integration-test-password");
                  newAccount.setRole(Role.ADMIN);
                  newAccount.setActive(true);
                  return userAccountRepository.save(newAccount);
                });
    return jwtService.generateToken(
        new AppPrincipal(account.getId(), account.getEmail(), account.getRole()));
  }
}
