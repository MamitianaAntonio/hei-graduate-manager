package com.hei.app;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hei.app.controller.TranscriptController;
import com.hei.app.dto.transcript.TranscriptResponse;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.model.Role;
import com.hei.app.security.AppPrincipal;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.security.CustomUserDetailsService;
import com.hei.app.security.JwtService;
import com.hei.app.security.SecurityConfig;
import com.hei.app.service.TranscriptService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TranscriptController.class)
@Import(SecurityConfig.class)
public class TranscriptControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private TranscriptService transcriptService;

  @MockBean private CurrentUserResolver currentUserResolver;

  @MockBean private JwtService jwtService;

  @MockBean private CustomUserDetailsService customUserDetailsService;

  private static final UUID ADMIN_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID STUDENT_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID STUDENT_ID = UUID.randomUUID();
  private static final UUID TEACHER_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID TEACHER_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void anonymousRequest_isRejected() throws Exception {
    mockMvc.perform(post("/api/transcripts/me")).andExpect(status().isForbidden());
  }

  @Test
  void student_canRequestOwnTranscript() throws Exception {
    stubStudent();
    TranscriptResponse response =
        new TranscriptResponse("https://presigned-url", "student@hei.com");
    when(transcriptService.requestOwn(any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(post("/api/transcripts/me").header("Authorization", "Bearer student-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.presignedUrl").value("https://presigned-url"))
        .andExpect(jsonPath("$.recipientEmail").value("student@hei.com"));

    verify(transcriptService).requestOwn(any(CurrentUser.class));
  }

  @Test
  void teacher_cannotRequestOwnTranscript() throws Exception {
    stubTeacher();
    doThrow(new UnauthorizedActionException("Student profile not found"))
        .when(transcriptService)
        .requestOwn(any(CurrentUser.class));

    mockMvc
        .perform(post("/api/transcripts/me").header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void admin_canRequestTranscriptForStudent() throws Exception {
    stubAdmin();
    TranscriptResponse response =
        new TranscriptResponse("https://presigned-url", "student@hei.com");
    when(transcriptService.requestForStudent(eq(STUDENT_ID), any(CurrentUser.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/api/transcripts")
                .param("studentId", STUDENT_ID.toString())
                .header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.presignedUrl").value("https://presigned-url"));

    verify(transcriptService).requestForStudent(eq(STUDENT_ID), any(CurrentUser.class));
  }

  @Test
  void student_cannotRequestTranscriptForAnotherStudent() throws Exception {
    stubStudent();
    UUID otherStudentId = UUID.randomUUID();
    doThrow(
            new UnauthorizedActionException(
                "Only admin can request a transcript for another student"))
        .when(transcriptService)
        .requestForStudent(eq(otherStudentId), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/transcripts")
                .param("studentId", otherStudentId.toString())
                .header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void teacher_cannotRequestTranscriptForStudent() throws Exception {
    stubTeacher();
    doThrow(
            new UnauthorizedActionException(
                "Only admin can request a transcript for another student"))
        .when(transcriptService)
        .requestForStudent(eq(STUDENT_ID), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/transcripts")
                .param("studentId", STUDENT_ID.toString())
                .header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isForbidden());
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
        .thenReturn(new CurrentUser(STUDENT_ACCOUNT_ID, Role.STUDENT, STUDENT_ID, null));
  }

  private void stubTeacher() {
    AppPrincipal principal = new AppPrincipal(TEACHER_ACCOUNT_ID, "teacher@hei.com", Role.TEACHER);
    AppUserDetails userDetails = new AppUserDetails(principal, "hash", true);
    when(jwtService.validateToken("teacher-token")).thenReturn(true);
    when(jwtService.extractUsername("teacher-token")).thenReturn("teacher@hei.com");
    when(customUserDetailsService.loadUserByUsername("teacher@hei.com")).thenReturn(userDetails);
    when(currentUserResolver.resolve(principal))
        .thenReturn(new CurrentUser(TEACHER_ACCOUNT_ID, Role.TEACHER, null, TEACHER_ID));
  }
}
