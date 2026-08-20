package com.hei.app;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.hei.app.controller.view.PromotionViewController;
import com.hei.app.dto.promotion.PromotionResponse;
import com.hei.app.dto.student.StudentResponse;
import com.hei.app.model.Role;
import com.hei.app.security.AppPrincipal;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.security.CustomUserDetailsService;
import com.hei.app.security.JwtService;
import com.hei.app.security.SecurityConfig;
import com.hei.app.service.GraduateExportService;
import com.hei.app.service.PromotionService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PromotionViewController.class)
@Import(SecurityConfig.class)
class PromotionViewControllerTest {
  private static final UUID ADMIN_ACCOUNT_ID = UUID.randomUUID();

  @Autowired private MockMvc mockMvc;

  @MockBean private PromotionService promotionService;

  @MockBean private GraduateExportService graduateExportService;

  @MockBean private CurrentUserResolver currentUserResolver;

  @MockBean private JwtService jwtService;

  @MockBean private CustomUserDetailsService customUserDetailsService;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void anonymousRequest_isRejected() throws Exception {
    mockMvc.perform(get("/promotions")).andExpect(status().isForbidden());
  }

  @Test
  void admin_canRenderPromotionListAndExportLinks() throws Exception {
    stubAdmin();
    UUID firstPromotionId = UUID.randomUUID();
    UUID secondPromotionId = UUID.randomUUID();
    when(promotionService.findAll(any(CurrentUser.class)))
        .thenReturn(
            List.of(
                new PromotionResponse(firstPromotionId, 2025),
                new PromotionResponse(secondPromotionId, 2026)));

    mockMvc
        .perform(get("/promotions").header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(view().name("promotions"))
        .andExpect(
            model()
                .attribute(
                    "promotions",
                    List.of(
                        new PromotionResponse(firstPromotionId, 2025),
                        new PromotionResponse(secondPromotionId, 2026))))
        .andExpect(content().string(containsString("2025")))
        .andExpect(content().string(containsString("2026")))
        .andExpect(
            content()
                .string(
                    containsString("/api/promotions/" + firstPromotionId + "/graduates/export")))
        .andExpect(
            content()
                .string(
                    containsString("/api/promotions/" + secondPromotionId + "/graduates/export")));
  }

  @Test
  void admin_canRenderGraduatesForPromotion() throws Exception {
    stubAdmin();
    UUID promotionId = UUID.randomUUID();
    when(promotionService.findById(any(UUID.class), any(CurrentUser.class)))
        .thenReturn(new PromotionResponse(promotionId, 2026));
    when(graduateExportService.findGraduates(
            promotionId, new CurrentUser(ADMIN_ACCOUNT_ID, Role.ADMIN, null, null)))
        .thenReturn(
            List.of(
                new StudentResponse(
                    UUID.randomUUID(),
                    "STD001",
                    "Alice",
                    "Martin",
                    promotionId,
                    UUID.randomUUID())));

    mockMvc
        .perform(get("/promotions/" + promotionId).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(view().name("promotion-details"))
        .andExpect(content().string(containsString("STD001")))
        .andExpect(content().string(containsString("Martin")))
        .andExpect(content().string(containsString("Alice")));
  }

  private void stubAdmin() {
    AppPrincipal principal = new AppPrincipal(ADMIN_ACCOUNT_ID, "admin@hei.com", Role.ADMIN);
    AppUserDetails userDetails = new AppUserDetails(principal, "hash", true);
    when(jwtService.validateToken("admin-token")).thenReturn(true);
    when(jwtService.extractUsername("admin-token")).thenReturn("admin@hei.com");
    when(customUserDetailsService.loadUserByUsername("admin@hei.com")).thenReturn(userDetails);
    when(currentUserResolver.resolve(principal))
        .thenReturn(new CurrentUser(ADMIN_ACCOUNT_ID, Role.ADMIN, null, null));
  }
}
