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

import com.hei.app.controller.TeacherController;
import com.hei.app.dto.teacher.TeacherRequest;
import com.hei.app.dto.teacher.TeacherResponse;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.model.Role;
import com.hei.app.security.AppPrincipal;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.security.CustomUserDetailsService;
import com.hei.app.security.JwtService;
import com.hei.app.security.SecurityConfig;
import com.hei.app.service.TeacherService;
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

@WebMvcTest(controllers = TeacherController.class)
@Import(SecurityConfig.class)
public class TeacherControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private TeacherService teacherService;

  @MockBean private CurrentUserResolver currentUserResolver;

  @MockBean private JwtService jwtService;

  @MockBean private CustomUserDetailsService customUserDetailsService;

  private static final UUID ADMIN_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID TEACHER_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID TEACHER_ID = UUID.randomUUID();
  private static final UUID OTHER_TEACHER_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID OTHER_TEACHER_ID = UUID.randomUUID();
  private static final UUID STUDENT_ACCOUNT_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void anonymousRequest_isRejected() throws Exception {
    mockMvc.perform(get("/api/teachers")).andExpect(status().isForbidden());
  }

  @Test
  void admin_canCreateTeacher() throws Exception {
    stubAdmin();
    UUID newId = UUID.randomUUID();
    TeacherResponse response = new TeacherResponse(newId, "Jean", "Dupont", null);
    when(teacherService.create(any(TeacherRequest.class), any(CurrentUser.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/api/teachers")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Jean\",\"lastName\":\"Dupont\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.firstName").value("Jean"))
        .andExpect(jsonPath("$.lastName").value("Dupont"));

    verify(teacherService).create(any(TeacherRequest.class), any(CurrentUser.class));
  }

  @Test
  void admin_canGetTeacherById() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();
    TeacherResponse response = new TeacherResponse(id, "Jean", "Dupont", null);
    when(teacherService.findById(eq(id), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/teachers/" + id).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Jean"));
  }

  @Test
  void admin_canListTeachers() throws Exception {
    stubAdmin();
    TeacherResponse t1 = new TeacherResponse(UUID.randomUUID(), "Jean", "Dupont", null);
    TeacherResponse t2 = new TeacherResponse(UUID.randomUUID(), "Marie", "Curie", null);
    when(teacherService.findAll(any(CurrentUser.class))).thenReturn(List.of(t1, t2));

    mockMvc
        .perform(get("/api/teachers").header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));

    verify(teacherService).findAll(any(CurrentUser.class));
  }

  @Test
  void admin_canUpdateTeacher() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();
    TeacherResponse response = new TeacherResponse(id, "Marie", "Curie", null);
    when(teacherService.update(eq(id), any(TeacherRequest.class), any(CurrentUser.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            put("/api/teachers/" + id)
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Marie\",\"lastName\":\"Curie\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Marie"));
  }

  @Test
  void admin_canDeleteTeacher() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/teachers/" + id).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isNoContent());

    verify(teacherService).delete(eq(id), any(CurrentUser.class));
  }

  @Test
  void teacher_canGetOwnProfile() throws Exception {
    stubTeacher();
    TeacherResponse response =
        new TeacherResponse(TEACHER_ID, "Jean", "Dupont", TEACHER_ACCOUNT_ID);
    when(teacherService.findByUserAccountId(eq(TEACHER_ACCOUNT_ID), any(CurrentUser.class)))
        .thenReturn(response);

    mockMvc
        .perform(get("/api/teachers/me").header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Jean"));
  }

  @Test
  void teacher_canGetOwnProfileById() throws Exception {
    stubTeacher();
    TeacherResponse response =
        new TeacherResponse(TEACHER_ID, "Jean", "Dupont", TEACHER_ACCOUNT_ID);
    when(teacherService.findById(eq(TEACHER_ID), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/teachers/" + TEACHER_ID).header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Jean"));
  }

  @Test
  void teacher_cannotGetAnotherTeacher() throws Exception {
    stubTeacher();
    when(teacherService.findById(eq(OTHER_TEACHER_ID), any(CurrentUser.class)))
        .thenThrow(
            new UnauthorizedActionException("Teacher cannot access another teacher's profile"));

    mockMvc
        .perform(
            get("/api/teachers/" + OTHER_TEACHER_ID)
                .header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403));
  }

  @Test
  void teacher_cannotCreateTeacher() throws Exception {
    stubTeacher();
    doThrow(new UnauthorizedActionException("Only admin can create teachers"))
        .when(teacherService)
        .create(any(TeacherRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/teachers")
                .header("Authorization", "Bearer teacher-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Jean\",\"lastName\":\"Dupont\"}"))
        .andExpect(status().isForbidden());

    verify(teacherService).create(any(TeacherRequest.class), any(CurrentUser.class));
  }

  @Test
  void teacher_cannotModifyTeacher() throws Exception {
    stubTeacher();
    doThrow(new UnauthorizedActionException("Only admin can update teachers"))
        .when(teacherService)
        .update(eq(OTHER_TEACHER_ID), any(TeacherRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            put("/api/teachers/" + OTHER_TEACHER_ID)
                .header("Authorization", "Bearer teacher-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Marie\",\"lastName\":\"Curie\"}"))
        .andExpect(status().isForbidden());

    verify(teacherService)
        .update(eq(OTHER_TEACHER_ID), any(TeacherRequest.class), any(CurrentUser.class));
  }

  @Test
  void teacher_cannotDeleteTeacher() throws Exception {
    stubTeacher();
    doThrow(new UnauthorizedActionException("Only admin can delete teachers"))
        .when(teacherService)
        .delete(eq(OTHER_TEACHER_ID), any(CurrentUser.class));

    mockMvc
        .perform(
            delete("/api/teachers/" + OTHER_TEACHER_ID)
                .header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isForbidden());

    verify(teacherService).delete(eq(OTHER_TEACHER_ID), any(CurrentUser.class));
  }

  @Test
  void teacher_cannotListAllTeachers() throws Exception {
    stubTeacher();
    doThrow(new UnauthorizedActionException("Only admin can list all teachers"))
        .when(teacherService)
        .findAll(any(CurrentUser.class));

    mockMvc
        .perform(get("/api/teachers").header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void student_cannotManageTeachers() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Only admin can create teachers"))
        .when(teacherService)
        .create(any(TeacherRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/teachers")
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Jean\",\"lastName\":\"Dupont\"}"))
        .andExpect(status().isForbidden());

    verify(teacherService).create(any(TeacherRequest.class), any(CurrentUser.class));
  }

  @Test
  void student_cannotListTeachers() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Only admin can list all teachers"))
        .when(teacherService)
        .findAll(any(CurrentUser.class));

    mockMvc
        .perform(get("/api/teachers").header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void insufficientRole_isRejected() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Only admin can create teachers"))
        .when(teacherService)
        .create(any(TeacherRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/teachers")
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Jean\",\"lastName\":\"Dupont\"}"))
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

  private void stubTeacher() {
    AppPrincipal principal = new AppPrincipal(TEACHER_ACCOUNT_ID, "teacher@hei.com", Role.TEACHER);
    AppUserDetails userDetails = new AppUserDetails(principal, "hash", true);
    when(jwtService.validateToken("teacher-token")).thenReturn(true);
    when(jwtService.extractUsername("teacher-token")).thenReturn("teacher@hei.com");
    when(customUserDetailsService.loadUserByUsername("teacher@hei.com")).thenReturn(userDetails);
    when(currentUserResolver.resolve(principal))
        .thenReturn(new CurrentUser(TEACHER_ACCOUNT_ID, Role.TEACHER, null, TEACHER_ID));
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
}
