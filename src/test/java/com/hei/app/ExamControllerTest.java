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

import com.hei.app.controller.ExamController;
import com.hei.app.dto.exam.ExamRequest;
import com.hei.app.dto.exam.ExamResponse;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.model.Role;
import com.hei.app.security.AppPrincipal;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.security.CustomUserDetailsService;
import com.hei.app.security.JwtService;
import com.hei.app.security.SecurityConfig;
import com.hei.app.service.ExamService;
import java.math.BigDecimal;
import java.time.Instant;
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

@WebMvcTest(controllers = ExamController.class)
@Import(SecurityConfig.class)
public class ExamControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private ExamService examService;

  @MockBean private CurrentUserResolver currentUserResolver;

  @MockBean private JwtService jwtService;

  @MockBean private CustomUserDetailsService customUserDetailsService;

  private static final UUID ADMIN_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID STUDENT_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID TEACHER_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID TEACHER_ID = UUID.randomUUID();
  private static final UUID COURSE_ID = UUID.randomUUID();
  private static final Instant NOW = Instant.parse("2026-03-15T10:00:00Z");

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void anonymousRequest_isRejected() throws Exception {
    mockMvc.perform(get("/api/exams")).andExpect(status().isForbidden());
  }

  @Test
  void admin_canCreateExam() throws Exception {
    stubAdmin();
    UUID newId = UUID.randomUUID();
    ExamResponse response = new ExamResponse(newId, NOW, new BigDecimal("2.5"), COURSE_ID);
    when(examService.create(any(ExamRequest.class), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/exams")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"dateExam\":\"2026-03-15T10:00:00Z\",\"coefficient\":2.5,\"courseId\":\""
                        + COURSE_ID
                        + "\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.coefficient").value(2.5));

    verify(examService).create(any(ExamRequest.class), any(CurrentUser.class));
  }

  @Test
  void admin_canGetExamById() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();
    ExamResponse response = new ExamResponse(id, NOW, new BigDecimal("2.5"), COURSE_ID);
    when(examService.findById(eq(id), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/exams/" + id).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.coefficient").value(2.5));
  }

  @Test
  void admin_canListExams() throws Exception {
    stubAdmin();
    ExamResponse e1 = new ExamResponse(UUID.randomUUID(), NOW, new BigDecimal("2.5"), COURSE_ID);
    ExamResponse e2 = new ExamResponse(UUID.randomUUID(), NOW, new BigDecimal("3.0"), COURSE_ID);
    when(examService.findAll(any(CurrentUser.class))).thenReturn(List.of(e1, e2));

    mockMvc
        .perform(get("/api/exams").header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));

    verify(examService).findAll(any(CurrentUser.class));
  }

  @Test
  void admin_canListExamsByCourseId() throws Exception {
    stubAdmin();
    ExamResponse e1 = new ExamResponse(UUID.randomUUID(), NOW, new BigDecimal("2.5"), COURSE_ID);
    when(examService.findByCourseId(eq(COURSE_ID), any(CurrentUser.class))).thenReturn(List.of(e1));

    mockMvc
        .perform(
            get("/api/exams/course/" + COURSE_ID).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void admin_canUpdateExam() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();
    ExamResponse response = new ExamResponse(id, NOW, new BigDecimal("3.0"), COURSE_ID);
    when(examService.update(eq(id), any(ExamRequest.class), any(CurrentUser.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            put("/api/exams/" + id)
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"dateExam\":\"2026-03-15T10:00:00Z\",\"coefficient\":3.0,\"courseId\":\""
                        + COURSE_ID
                        + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.coefficient").value(3.0));
  }

  @Test
  void admin_canDeleteExam() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/exams/" + id).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isNoContent());

    verify(examService).delete(eq(id), any(CurrentUser.class));
  }

  @Test
  void teacher_canReadExamForAssignedCourse() throws Exception {
    stubTeacher();
    UUID id = UUID.randomUUID();
    ExamResponse response = new ExamResponse(id, NOW, new BigDecimal("2.5"), COURSE_ID);
    when(examService.findById(eq(id), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/exams/" + id).header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.coefficient").value(2.5));
  }

  @Test
  void teacher_cannotReadExamForUnassignedCourse() throws Exception {
    stubTeacher();
    UUID id = UUID.randomUUID();
    when(examService.findById(eq(id), any(CurrentUser.class)))
        .thenThrow(new UnauthorizedActionException("Teacher is not assigned to this course"));

    mockMvc
        .perform(get("/api/exams/" + id).header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403));
  }

  @Test
  void teacher_canListExamsForAssignedCourses() throws Exception {
    stubTeacher();
    ExamResponse e1 = new ExamResponse(UUID.randomUUID(), NOW, new BigDecimal("2.5"), COURSE_ID);
    when(examService.findAll(any(CurrentUser.class))).thenReturn(List.of(e1));

    mockMvc
        .perform(get("/api/exams").header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void teacher_canCreateExamForAssignedCourse() throws Exception {
    stubTeacher();
    UUID newId = UUID.randomUUID();
    ExamResponse response = new ExamResponse(newId, NOW, new BigDecimal("2.5"), COURSE_ID);
    when(examService.create(any(ExamRequest.class), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/exams")
                .header("Authorization", "Bearer teacher-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"dateExam\":\"2026-03-15T10:00:00Z\",\"coefficient\":2.5,\"courseId\":\""
                        + COURSE_ID
                        + "\"}"))
        .andExpect(status().isCreated());

    verify(examService).create(any(ExamRequest.class), any(CurrentUser.class));
  }

  @Test
  void teacher_cannotCreateExamForUnassignedCourse() throws Exception {
    stubTeacher();
    UUID otherCourseId = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Teacher is not assigned to this course"))
        .when(examService)
        .create(any(ExamRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/exams")
                .header("Authorization", "Bearer teacher-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"dateExam\":\"2026-03-15T10:00:00Z\",\"coefficient\":2.5,\"courseId\":\""
                        + otherCourseId
                        + "\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void student_cannotReadExams() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Student cannot read exam details"))
        .when(examService)
        .findById(any(UUID.class), any(CurrentUser.class));

    mockMvc
        .perform(
            get("/api/exams/" + UUID.randomUUID()).header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void student_cannotListExams() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Student cannot list all exams"))
        .when(examService)
        .findAll(any(CurrentUser.class));

    mockMvc
        .perform(get("/api/exams").header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void student_cannotCreateExam() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Student cannot create exams"))
        .when(examService)
        .create(any(ExamRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/exams")
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"dateExam\":\"2026-03-15T10:00:00Z\",\"coefficient\":2.5,\"courseId\":\""
                        + COURSE_ID
                        + "\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void student_cannotUpdateExam() throws Exception {
    stubStudent();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Student cannot update exams"))
        .when(examService)
        .update(eq(id), any(ExamRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            put("/api/exams/" + id)
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"dateExam\":\"2026-03-15T10:00:00Z\",\"coefficient\":3.0,\"courseId\":\""
                        + COURSE_ID
                        + "\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void student_cannotDeleteExam() throws Exception {
    stubStudent();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Student cannot delete exams"))
        .when(examService)
        .delete(eq(id), any(CurrentUser.class));

    mockMvc
        .perform(delete("/api/exams/" + id).header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void insufficientRole_isRejected() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Student cannot create exams"))
        .when(examService)
        .create(any(ExamRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/exams")
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"dateExam\":\"2026-03-15T10:00:00Z\",\"coefficient\":2.5,\"courseId\":\""
                        + COURSE_ID
                        + "\"}"))
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
        .thenReturn(new CurrentUser(TEACHER_ACCOUNT_ID, Role.TEACHER, null, TEACHER_ID));
  }
}
