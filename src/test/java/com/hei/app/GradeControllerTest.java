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

import com.hei.app.controller.GradeController;
import com.hei.app.dto.grade.GradeRequest;
import com.hei.app.dto.grade.GradeResponse;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.model.Role;
import com.hei.app.security.AppPrincipal;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.security.CustomUserDetailsService;
import com.hei.app.security.JwtService;
import com.hei.app.security.SecurityConfig;
import com.hei.app.service.GradeService;
import java.math.BigDecimal;
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

@WebMvcTest(controllers = GradeController.class)
@Import(SecurityConfig.class)
public class GradeControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private GradeService gradeService;

  @MockBean private CurrentUserResolver currentUserResolver;

  @MockBean private JwtService jwtService;

  @MockBean private CustomUserDetailsService customUserDetailsService;

  private static final UUID ADMIN_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID STUDENT_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID STUDENT_ID = UUID.randomUUID();
  private static final UUID TEACHER_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID TEACHER_ID = UUID.randomUUID();
  private static final UUID EXAM_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void anonymousRequest_isRejected() throws Exception {
    mockMvc.perform(get("/api/grades")).andExpect(status().isForbidden());
  }

  @Test
  void admin_canCreateGrade() throws Exception {
    stubAdmin();
    UUID newId = UUID.randomUUID();
    GradeResponse response = new GradeResponse(newId, STUDENT_ID, EXAM_ID, new BigDecimal("15.5"));
    when(gradeService.create(any(GradeRequest.class), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/grades")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"studentId\":\""
                        + STUDENT_ID
                        + "\",\"examId\":\""
                        + EXAM_ID
                        + "\",\"value\":15.5}"))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.value").value(15.5));

    verify(gradeService).create(any(GradeRequest.class), any(CurrentUser.class));
  }

  @Test
  void admin_canGetGradeById() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();
    GradeResponse response = new GradeResponse(id, STUDENT_ID, EXAM_ID, new BigDecimal("15.5"));
    when(gradeService.findById(eq(id), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/grades/" + id).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.value").value(15.5));
  }

  @Test
  void admin_canListGrades() throws Exception {
    stubAdmin();
    GradeResponse g1 =
        new GradeResponse(UUID.randomUUID(), STUDENT_ID, EXAM_ID, new BigDecimal("15.5"));
    GradeResponse g2 =
        new GradeResponse(UUID.randomUUID(), STUDENT_ID, EXAM_ID, new BigDecimal("12.0"));
    when(gradeService.findAll(any(CurrentUser.class))).thenReturn(List.of(g1, g2));

    mockMvc
        .perform(get("/api/grades").header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));

    verify(gradeService).findAll(any(CurrentUser.class));
  }

  @Test
  void admin_canListByStudentId() throws Exception {
    stubAdmin();
    GradeResponse g1 =
        new GradeResponse(UUID.randomUUID(), STUDENT_ID, EXAM_ID, new BigDecimal("15.5"));
    when(gradeService.findByStudentId(eq(STUDENT_ID), any(CurrentUser.class)))
        .thenReturn(List.of(g1));

    mockMvc
        .perform(
            get("/api/grades/student/" + STUDENT_ID).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void admin_canListByExamId() throws Exception {
    stubAdmin();
    GradeResponse g1 =
        new GradeResponse(UUID.randomUUID(), STUDENT_ID, EXAM_ID, new BigDecimal("15.5"));
    when(gradeService.findByExamId(eq(EXAM_ID), any(CurrentUser.class))).thenReturn(List.of(g1));

    mockMvc
        .perform(get("/api/grades/exam/" + EXAM_ID).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void admin_canGetByStudentAndExam() throws Exception {
    stubAdmin();
    UUID studentId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    GradeResponse response =
        new GradeResponse(UUID.randomUUID(), studentId, examId, new BigDecimal("15.5"));
    when(gradeService.findByStudentAndExam(eq(studentId), eq(examId), any(CurrentUser.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            get("/api/grades/student/" + studentId + "/exam/" + examId)
                .header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.value").value(15.5));
  }

  @Test
  void admin_canUpdateGrade() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();
    GradeResponse response = new GradeResponse(id, STUDENT_ID, EXAM_ID, new BigDecimal("18.0"));
    when(gradeService.update(eq(id), any(GradeRequest.class), any(CurrentUser.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            put("/api/grades/" + id)
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"studentId\":\""
                        + STUDENT_ID
                        + "\",\"examId\":\""
                        + EXAM_ID
                        + "\",\"value\":18.0}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.value").value(18.0));
  }

  @Test
  void admin_canDeleteGrade() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/grades/" + id).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isNoContent());

    verify(gradeService).delete(eq(id), any(CurrentUser.class));
  }

  @Test
  void student_canReadOwnGrades() throws Exception {
    stubStudent();
    GradeResponse g1 =
        new GradeResponse(UUID.randomUUID(), STUDENT_ID, EXAM_ID, new BigDecimal("15.5"));
    when(gradeService.findByStudentId(eq(STUDENT_ID), any(CurrentUser.class)))
        .thenReturn(List.of(g1));

    mockMvc
        .perform(
            get("/api/grades/student/" + STUDENT_ID)
                .header("Authorization", "Bearer student-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void student_cannotReadAnotherStudentGrades() throws Exception {
    stubStudent();
    UUID otherStudentId = UUID.randomUUID();
    when(gradeService.findByStudentId(eq(otherStudentId), any(CurrentUser.class)))
        .thenThrow(
            new UnauthorizedActionException("Student cannot access another student's grade"));

    mockMvc
        .perform(
            get("/api/grades/student/" + otherStudentId)
                .header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void student_cannotCreateGrade() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Students cannot create grades"))
        .when(gradeService)
        .create(any(GradeRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/grades")
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"studentId\":\""
                        + STUDENT_ID
                        + "\",\"examId\":\""
                        + EXAM_ID
                        + "\",\"value\":15.5}"))
        .andExpect(status().isForbidden());

    verify(gradeService).create(any(GradeRequest.class), any(CurrentUser.class));
  }

  @Test
  void student_cannotUpdateGrade() throws Exception {
    stubStudent();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Students cannot update grades"))
        .when(gradeService)
        .update(eq(id), any(GradeRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            put("/api/grades/" + id)
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"studentId\":\""
                        + STUDENT_ID
                        + "\",\"examId\":\""
                        + EXAM_ID
                        + "\",\"value\":18.0}"))
        .andExpect(status().isForbidden());

    verify(gradeService).update(eq(id), any(GradeRequest.class), any(CurrentUser.class));
  }

  @Test
  void student_cannotDeleteGrade() throws Exception {
    stubStudent();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Students cannot delete grades"))
        .when(gradeService)
        .delete(eq(id), any(CurrentUser.class));

    mockMvc
        .perform(delete("/api/grades/" + id).header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());

    verify(gradeService).delete(eq(id), any(CurrentUser.class));
  }

  @Test
  void teacher_canReadGradesForAssignedCourse() throws Exception {
    stubTeacher();
    GradeResponse g1 =
        new GradeResponse(UUID.randomUUID(), STUDENT_ID, EXAM_ID, new BigDecimal("15.5"));
    when(gradeService.findByExamId(eq(EXAM_ID), any(CurrentUser.class))).thenReturn(List.of(g1));

    mockMvc
        .perform(get("/api/grades/exam/" + EXAM_ID).header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void teacher_cannotReadGradesForUnassignedCourse() throws Exception {
    stubTeacher();
    UUID otherExamId = UUID.randomUUID();
    when(gradeService.findByExamId(eq(otherExamId), any(CurrentUser.class)))
        .thenThrow(new UnauthorizedActionException("Teacher is not assigned to this course"));

    mockMvc
        .perform(
            get("/api/grades/exam/" + otherExamId).header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403));
  }

  @Test
  void teacher_canUpdateGradeForAssignedCourse() throws Exception {
    stubTeacher();
    UUID id = UUID.randomUUID();
    GradeResponse response = new GradeResponse(id, STUDENT_ID, EXAM_ID, new BigDecimal("18.0"));
    when(gradeService.update(eq(id), any(GradeRequest.class), any(CurrentUser.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            put("/api/grades/" + id)
                .header("Authorization", "Bearer teacher-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"studentId\":\""
                        + STUDENT_ID
                        + "\",\"examId\":\""
                        + EXAM_ID
                        + "\",\"value\":18.0}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.value").value(18.0));
  }

  @Test
  void teacher_cannotUpdateGradeForUnassignedCourse() throws Exception {
    stubTeacher();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Teacher is not assigned to this course"))
        .when(gradeService)
        .update(eq(id), any(GradeRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            put("/api/grades/" + id)
                .header("Authorization", "Bearer teacher-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"studentId\":\""
                        + STUDENT_ID
                        + "\",\"examId\":\""
                        + EXAM_ID
                        + "\",\"value\":18.0}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void insufficientRole_isRejected() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Students cannot create grades"))
        .when(gradeService)
        .create(any(GradeRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/grades")
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"studentId\":\""
                        + STUDENT_ID
                        + "\",\"examId\":\""
                        + EXAM_ID
                        + "\",\"value\":15.5}"))
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
