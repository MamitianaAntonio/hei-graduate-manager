package com.hei.app;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hei.app.controller.PromotionController;
import com.hei.app.dto.promotion.PromotionRequest;
import com.hei.app.dto.promotion.PromotionResponse;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.model.Role;
import com.hei.app.security.AppPrincipal;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.security.CustomUserDetailsService;
import com.hei.app.security.JwtService;
import com.hei.app.security.SecurityConfig;
import com.hei.app.service.PromotionService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PromotionController.class)
@Import(SecurityConfig.class)
public class PromotionControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private PromotionService promotionService;

  @MockBean private CurrentUserResolver currentUserResolver;

  @MockBean private JwtService jwtService;

  @MockBean private CustomUserDetailsService customUserDetailsService;

  private static final UUID ADMIN_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID STUDENT_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID TEACHER_ACCOUNT_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void anonymousRequest_isRejected() throws Exception {
    mockMvc.perform(get("/api/promotions")).andExpect(status().isForbidden());
  }

  @Test
  void admin_canCreatePromotion() throws Exception {
    stubAdmin();
    UUID newId = UUID.randomUUID();
    PromotionResponse response = new PromotionResponse(newId, 2026);
    when(promotionService.create(any(PromotionRequest.class), any(CurrentUser.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/api/promotions")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"year\":2026}"))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.year").value(2026));

    verify(promotionService).create(any(PromotionRequest.class), any(CurrentUser.class));
  }

  @Test
  void admin_canGetPromotionById() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();
    PromotionResponse response = new PromotionResponse(id, 2026);
    when(promotionService.findById(eq(id), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/promotions/" + id).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.year").value(2026));
  }

  @Test
  void admin_canGetPromotionByYear() throws Exception {
    stubAdmin();
    PromotionResponse response = new PromotionResponse(UUID.randomUUID(), 2026);
    when(promotionService.findByYear(eq(2026), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/promotions/year/2026").header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.year").value(2026));
  }

  @Test
  void admin_canListPromotions() throws Exception {
    stubAdmin();
    PromotionResponse p1 = new PromotionResponse(UUID.randomUUID(), 2025);
    PromotionResponse p2 = new PromotionResponse(UUID.randomUUID(), 2026);
    when(promotionService.findAll(any(CurrentUser.class))).thenReturn(List.of(p1, p2));

    mockMvc
        .perform(get("/api/promotions").header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));

    verify(promotionService).findAll(any(CurrentUser.class));
  }

  @Test
  void admin_canUpdatePromotion() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();
    PromotionResponse response = new PromotionResponse(id, 2027);
    when(promotionService.update(eq(id), any(PromotionRequest.class), any(CurrentUser.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            put("/api/promotions/" + id)
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"year\":2027}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.year").value(2027));
  }

  @Test
  void admin_canDeletePromotion() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/promotions/" + id).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isNoContent());

    verify(promotionService).delete(eq(id), any(CurrentUser.class));
  }

  @Test
  void student_cannotReadPromotion() throws Exception {
    stubStudent();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can read promotions"))
        .when(promotionService)
        .findById(eq(id), any(CurrentUser.class));

    mockMvc
        .perform(get("/api/promotions/" + id).header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void student_cannotListPromotions() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Only admin can read promotions"))
        .when(promotionService)
        .findAll(any(CurrentUser.class));

    mockMvc
        .perform(get("/api/promotions").header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void student_cannotCreatePromotion() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Only admin can create promotions"))
        .when(promotionService)
        .create(any(PromotionRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/promotions")
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"year\":2026}"))
        .andExpect(status().isForbidden());

    verify(promotionService).create(any(PromotionRequest.class), any(CurrentUser.class));
  }

  @Test
  void student_cannotUpdatePromotion() throws Exception {
    stubStudent();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can update promotions"))
        .when(promotionService)
        .update(eq(id), any(PromotionRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            put("/api/promotions/" + id)
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"year\":2027}"))
        .andExpect(status().isForbidden());

    verify(promotionService).update(eq(id), any(PromotionRequest.class), any(CurrentUser.class));
  }

  @Test
  void student_cannotDeletePromotion() throws Exception {
    stubStudent();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can delete promotions"))
        .when(promotionService)
        .delete(eq(id), any(CurrentUser.class));

    mockMvc
        .perform(delete("/api/promotions/" + id).header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());

    verify(promotionService).delete(eq(id), any(CurrentUser.class));
  }

  @Test
  void teacher_cannotReadPromotion() throws Exception {
    stubTeacher();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can read promotions"))
        .when(promotionService)
        .findById(eq(id), any(CurrentUser.class));

    mockMvc
        .perform(get("/api/promotions/" + id).header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void teacher_cannotListPromotions() throws Exception {
    stubTeacher();
    doThrow(new UnauthorizedActionException("Only admin can read promotions"))
        .when(promotionService)
        .findAll(any(CurrentUser.class));

    mockMvc
        .perform(get("/api/promotions").header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void teacher_cannotCreatePromotion() throws Exception {
    stubTeacher();
    doThrow(new UnauthorizedActionException("Only admin can create promotions"))
        .when(promotionService)
        .create(any(PromotionRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/promotions")
                .header("Authorization", "Bearer teacher-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"year\":2026}"))
        .andExpect(status().isForbidden());

    verify(promotionService).create(any(PromotionRequest.class), any(CurrentUser.class));
  }

  @Test
  void teacher_cannotUpdatePromotion() throws Exception {
    stubTeacher();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can update promotions"))
        .when(promotionService)
        .update(eq(id), any(PromotionRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            put("/api/promotions/" + id)
                .header("Authorization", "Bearer teacher-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"year\":2027}"))
        .andExpect(status().isForbidden());

    verify(promotionService).update(eq(id), any(PromotionRequest.class), any(CurrentUser.class));
  }

  @Test
  void teacher_cannotDeletePromotion() throws Exception {
    stubTeacher();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can delete promotions"))
        .when(promotionService)
        .delete(eq(id), any(CurrentUser.class));

    mockMvc
        .perform(delete("/api/promotions/" + id).header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isForbidden());

    verify(promotionService).delete(eq(id), any(CurrentUser.class));
  }

  @Test
  void insufficientRole_isRejected() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Only admin can create promotions"))
        .when(promotionService)
        .create(any(PromotionRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/promotions")
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"year\":2026}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.error").value("Forbidden"));
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

  private void stubStudent() {
    AppPrincipal principal = new AppPrincipal(STUDENT_ACCOUNT_ID, "student@hei.com", Role.STUDENT);
    AppUserDetails userDetails = new AppUserDetails(principal, "hash", true);
    when(jwtService.validateToken("student-token")).thenReturn(true);
    when(jwtService.extractUsername("student-token")).thenReturn("student@hei.com");
    when(customUserDetailsService.loadUserByUsername("student@hei.com")).thenReturn(userDetails);
    when(currentUserResolver.resolve(principal))
        .thenReturn(new CurrentUser(STUDENT_ACCOUNT_ID, Role.STUDENT, UUID.randomUUID(), null));
  }

  private void stubTeacher() {
    AppPrincipal principal = new AppPrincipal(TEACHER_ACCOUNT_ID, "teacher@hei.com", Role.TEACHER);
    AppUserDetails userDetails = new AppUserDetails(principal, "hash", true);
    when(jwtService.validateToken("teacher-token")).thenReturn(true);
    when(jwtService.extractUsername("teacher-token")).thenReturn("teacher@hei.com");
    when(customUserDetailsService.loadUserByUsername("teacher@hei.com")).thenReturn(userDetails);
    when(currentUserResolver.resolve(principal))
        .thenReturn(new CurrentUser(TEACHER_ACCOUNT_ID, Role.TEACHER, null, UUID.randomUUID()));
  }
}
