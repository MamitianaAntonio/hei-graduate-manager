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

import com.hei.app.controller.CourseAssignmentController;
import com.hei.app.dto.assignment.CourseAssignmentRequest;
import com.hei.app.dto.assignment.CourseAssignmentResponse;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.model.Role;
import com.hei.app.model.Semester;
import com.hei.app.security.AppPrincipal;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.security.CustomUserDetailsService;
import com.hei.app.security.JwtService;
import com.hei.app.security.SecurityConfig;
import com.hei.app.service.CourseAssignmentService;
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

@WebMvcTest(controllers = CourseAssignmentController.class)
@Import(SecurityConfig.class)
public class CourseAssignmentControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private CourseAssignmentService courseAssignmentService;

  @MockBean private CurrentUserResolver currentUserResolver;

  @MockBean private JwtService jwtService;

  @MockBean private CustomUserDetailsService customUserDetailsService;

  private static final UUID ADMIN_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID STUDENT_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID TEACHER_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID TEACHER_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void anonymousRequest_isRejected() throws Exception {
    mockMvc.perform(get("/api/course-assignments")).andExpect(status().isForbidden());
  }

  @Test
  void admin_canCreateAssignment() throws Exception {
    stubAdmin();
    UUID newId = UUID.randomUUID();
    CourseAssignmentResponse response =
        new CourseAssignmentResponse(
            newId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Semester.S1, 2026);
    when(courseAssignmentService.create(any(CourseAssignmentRequest.class), any(CurrentUser.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/api/course-assignments")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"courseId\":\""
                        + response.courseId()
                        + "\",\"teacherId\":\""
                        + response.teacherId()
                        + "\",\"groupId\":\""
                        + response.groupId()
                        + "\",\"semester\":\"S1\",\"academicYear\":2026}"))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"));

    verify(courseAssignmentService)
        .create(any(CourseAssignmentRequest.class), any(CurrentUser.class));
  }

  @Test
  void admin_canGetAssignmentById() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();
    CourseAssignmentResponse response =
        new CourseAssignmentResponse(
            id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Semester.S1, 2026);
    when(courseAssignmentService.findById(eq(id), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/course-assignments/" + id).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.semester").value("S1"))
        .andExpect(jsonPath("$.academicYear").value(2026));
  }

  @Test
  void admin_canListAssignments() throws Exception {
    stubAdmin();
    CourseAssignmentResponse a1 =
        new CourseAssignmentResponse(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            Semester.S1,
            2026);
    CourseAssignmentResponse a2 =
        new CourseAssignmentResponse(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            Semester.S2,
            2026);
    when(courseAssignmentService.findAll(any(CurrentUser.class))).thenReturn(List.of(a1, a2));

    mockMvc
        .perform(get("/api/course-assignments").header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));

    verify(courseAssignmentService).findAll(any(CurrentUser.class));
  }

  @Test
  void admin_canListByTeacherId() throws Exception {
    stubAdmin();
    UUID teacherId = UUID.randomUUID();
    CourseAssignmentResponse a1 =
        new CourseAssignmentResponse(
            UUID.randomUUID(), UUID.randomUUID(), teacherId, UUID.randomUUID(), Semester.S1, 2026);
    when(courseAssignmentService.findByTeacherId(eq(teacherId), any(CurrentUser.class)))
        .thenReturn(List.of(a1));

    mockMvc
        .perform(
            get("/api/course-assignments/teacher/" + teacherId)
                .header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void admin_canListByCourseId() throws Exception {
    stubAdmin();
    UUID courseId = UUID.randomUUID();
    CourseAssignmentResponse a1 =
        new CourseAssignmentResponse(
            UUID.randomUUID(), courseId, UUID.randomUUID(), UUID.randomUUID(), Semester.S1, 2026);
    when(courseAssignmentService.findByCourseId(eq(courseId), any(CurrentUser.class)))
        .thenReturn(List.of(a1));

    mockMvc
        .perform(
            get("/api/course-assignments/course/" + courseId)
                .header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void admin_canListByGroupId() throws Exception {
    stubAdmin();
    UUID groupId = UUID.randomUUID();
    CourseAssignmentResponse a1 =
        new CourseAssignmentResponse(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), groupId, Semester.S1, 2026);
    when(courseAssignmentService.findByGroupId(eq(groupId), any(CurrentUser.class)))
        .thenReturn(List.of(a1));

    mockMvc
        .perform(
            get("/api/course-assignments/group/" + groupId)
                .header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void admin_canUpdateAssignment() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();
    CourseAssignmentResponse response =
        new CourseAssignmentResponse(
            id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Semester.S2, 2027);
    when(courseAssignmentService.update(
            eq(id), any(CourseAssignmentRequest.class), any(CurrentUser.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            put("/api/course-assignments/" + id)
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"courseId\":\""
                        + response.courseId()
                        + "\",\"teacherId\":\""
                        + response.teacherId()
                        + "\",\"groupId\":\""
                        + response.groupId()
                        + "\",\"semester\":\"S2\",\"academicYear\":2027}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.semester").value("S2"));
  }

  @Test
  void admin_canDeleteAssignment() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();

    mockMvc
        .perform(
            delete("/api/course-assignments/" + id).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isNoContent());

    verify(courseAssignmentService).delete(eq(id), any(CurrentUser.class));
  }

  @Test
  void teacher_canGetOwnAssignment() throws Exception {
    stubTeacher();
    UUID id = UUID.randomUUID();
    CourseAssignmentResponse response =
        new CourseAssignmentResponse(
            id, UUID.randomUUID(), TEACHER_ID, UUID.randomUUID(), Semester.S1, 2026);
    when(courseAssignmentService.findById(eq(id), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(
            get("/api/course-assignments/" + id).header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.semester").value("S1"));
  }

  @Test
  void teacher_cannotGetAnotherTeacherAssignment() throws Exception {
    stubTeacher();
    UUID id = UUID.randomUUID();
    when(courseAssignmentService.findById(eq(id), any(CurrentUser.class)))
        .thenThrow(
            new UnauthorizedActionException("Teacher cannot access another teacher's assignment"));

    mockMvc
        .perform(
            get("/api/course-assignments/" + id).header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403));
  }

  @Test
  void teacher_canListOwnAssignments() throws Exception {
    stubTeacher();
    CourseAssignmentResponse a1 =
        new CourseAssignmentResponse(
            UUID.randomUUID(), UUID.randomUUID(), TEACHER_ID, UUID.randomUUID(), Semester.S1, 2026);
    when(courseAssignmentService.findAll(any(CurrentUser.class))).thenReturn(List.of(a1));

    mockMvc
        .perform(get("/api/course-assignments").header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void teacher_cannotCreateAssignment() throws Exception {
    stubTeacher();
    doThrow(new UnauthorizedActionException("Only admin can create course assignments"))
        .when(courseAssignmentService)
        .create(any(CourseAssignmentRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/course-assignments")
                .header("Authorization", "Bearer teacher-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"courseId\":\""
                        + UUID.randomUUID()
                        + "\",\"teacherId\":\""
                        + UUID.randomUUID()
                        + "\",\"groupId\":\""
                        + UUID.randomUUID()
                        + "\",\"semester\":\"S1\",\"academicYear\":2026}"))
        .andExpect(status().isForbidden());

    verify(courseAssignmentService)
        .create(any(CourseAssignmentRequest.class), any(CurrentUser.class));
  }

  @Test
  void teacher_cannotUpdateAssignment() throws Exception {
    stubTeacher();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can update course assignments"))
        .when(courseAssignmentService)
        .update(eq(id), any(CourseAssignmentRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            put("/api/course-assignments/" + id)
                .header("Authorization", "Bearer teacher-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"courseId\":\""
                        + UUID.randomUUID()
                        + "\",\"teacherId\":\""
                        + UUID.randomUUID()
                        + "\",\"groupId\":\""
                        + UUID.randomUUID()
                        + "\",\"semester\":\"S2\",\"academicYear\":2027}"))
        .andExpect(status().isForbidden());

    verify(courseAssignmentService)
        .update(eq(id), any(CourseAssignmentRequest.class), any(CurrentUser.class));
  }

  @Test
  void teacher_cannotDeleteAssignment() throws Exception {
    stubTeacher();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can delete course assignments"))
        .when(courseAssignmentService)
        .delete(eq(id), any(CurrentUser.class));

    mockMvc
        .perform(
            delete("/api/course-assignments/" + id).header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isForbidden());

    verify(courseAssignmentService).delete(eq(id), any(CurrentUser.class));
  }

  @Test
  void student_cannotAccessAssignments() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Student cannot access course assignments"))
        .when(courseAssignmentService)
        .findAll(any(CurrentUser.class));

    mockMvc
        .perform(get("/api/course-assignments").header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void student_cannotCreateAssignment() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Only admin can create course assignments"))
        .when(courseAssignmentService)
        .create(any(CourseAssignmentRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/course-assignments")
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"courseId\":\""
                        + UUID.randomUUID()
                        + "\",\"teacherId\":\""
                        + UUID.randomUUID()
                        + "\",\"groupId\":\""
                        + UUID.randomUUID()
                        + "\",\"semester\":\"S1\",\"academicYear\":2026}"))
        .andExpect(status().isForbidden());

    verify(courseAssignmentService)
        .create(any(CourseAssignmentRequest.class), any(CurrentUser.class));
  }

  @Test
  void student_cannotDeleteAssignment() throws Exception {
    stubStudent();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can delete course assignments"))
        .when(courseAssignmentService)
        .delete(eq(id), any(CurrentUser.class));

    mockMvc
        .perform(
            delete("/api/course-assignments/" + id).header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());

    verify(courseAssignmentService).delete(eq(id), any(CurrentUser.class));
  }

  @Test
  void insufficientRole_isRejected() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Only admin can create course assignments"))
        .when(courseAssignmentService)
        .create(any(CourseAssignmentRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/course-assignments")
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"courseId\":\""
                        + UUID.randomUUID()
                        + "\",\"teacherId\":\""
                        + UUID.randomUUID()
                        + "\",\"groupId\":\""
                        + UUID.randomUUID()
                        + "\",\"semester\":\"S1\",\"academicYear\":2026}"))
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
