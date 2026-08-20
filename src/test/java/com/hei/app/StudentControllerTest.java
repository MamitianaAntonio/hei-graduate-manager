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

import com.hei.app.controller.StudentController;
import com.hei.app.dto.student.StudentRequest;
import com.hei.app.dto.student.StudentResponse;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.model.Role;
import com.hei.app.security.AppPrincipal;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.security.CustomUserDetailsService;
import com.hei.app.security.JwtService;
import com.hei.app.security.SecurityConfig;
import com.hei.app.service.StudentService;
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

@WebMvcTest(controllers = StudentController.class)
@Import(SecurityConfig.class)
public class StudentControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private StudentService studentService;

  @MockBean private CurrentUserResolver currentUserResolver;

  @MockBean private JwtService jwtService;

  @MockBean private CustomUserDetailsService customUserDetailsService;

  private static final UUID ADMIN_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID STUDENT_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID STUDENT_ID = UUID.randomUUID();
  private static final UUID OTHER_STUDENT_ID = UUID.randomUUID();
  private static final UUID TEACHER_ACCOUNT_ID = UUID.randomUUID();
  private static final UUID TEACHER_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void anonymousRequest_shouldReturn403() throws Exception {
    mockMvc.perform(get("/api/students")).andExpect(status().isForbidden());
  }

  @Test
  void admin_canCreateStudent() throws Exception {
    stubAdmin();
    UUID newId = UUID.randomUUID();
    StudentResponse response = new StudentResponse(newId, "STD24191", "John", "Doe", null, null);
    when(studentService.create(any(StudentRequest.class), any(CurrentUser.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/api/students")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"std\":\"STD24191\",\"firstName\":\"John\",\"lastName\":\"Doe\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.std").value("STD24191"))
        .andExpect(jsonPath("$.firstName").value("John"));

    verify(studentService).create(any(StudentRequest.class), any(CurrentUser.class));
  }

  @Test
  void admin_canGetStudentById() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();
    StudentResponse response = new StudentResponse(id, "STD24191", "John", "Doe", null, null);
    when(studentService.findById(eq(id), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/students/" + id).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.std").value("STD24191"));
  }

  @Test
  void admin_canGetStudentByStd() throws Exception {
    stubAdmin();
    StudentResponse response =
        new StudentResponse(UUID.randomUUID(), "STD24191", "John", "Doe", null, null);
    when(studentService.findByStd(eq("STD24191"), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/students/std/STD24191").header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.std").value("STD24191"));
  }

  @Test
  void admin_canPerformAllowedStudentOperations() throws Exception {
    stubAdmin();
    StudentResponse s1 = new StudentResponse(UUID.randomUUID(), "STD1", "A", "B", null, null);
    StudentResponse s2 = new StudentResponse(UUID.randomUUID(), "STD2", "C", "D", null, null);
    when(studentService.findAll(any(CurrentUser.class))).thenReturn(List.of(s1, s2));

    mockMvc
        .perform(get("/api/students").header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));

    verify(studentService).findAll(any(CurrentUser.class));
  }

  @Test
  void admin_canUpdateStudent() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();
    StudentResponse response = new StudentResponse(id, "STD24191", "Jane", "Doe", null, null);
    when(studentService.update(eq(id), any(StudentRequest.class), any(CurrentUser.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            put("/api/students/" + id)
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"std\":\"STD24191\",\"firstName\":\"Jane\",\"lastName\":\"Doe\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Jane"));
  }

  @Test
  void admin_canDeleteStudent() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/students/" + id).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isNoContent());

    verify(studentService).delete(eq(id), any(CurrentUser.class));
  }

  @Test
  void student_canGetOwnProfile() throws Exception {
    stubStudent();
    StudentResponse response =
        new StudentResponse(STUDENT_ID, "STD24191", "John", "Doe", null, null);
    when(studentService.findByUserAccountId(eq(STUDENT_ACCOUNT_ID), any(CurrentUser.class)))
        .thenReturn(response);

    mockMvc
        .perform(get("/api/students/me").header("Authorization", "Bearer student-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.std").value("STD24191"));
  }

  @Test
  void student_canGetOwnProfileById() throws Exception {
    stubStudent();
    StudentResponse response =
        new StudentResponse(STUDENT_ID, "STD24191", "John", "Doe", null, null);
    when(studentService.findById(eq(STUDENT_ID), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/students/" + STUDENT_ID).header("Authorization", "Bearer student-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.std").value("STD24191"));
  }

  @Test
  void student_cannotGetAnotherStudentProfile() throws Exception {
    stubStudent();
    when(studentService.findById(eq(OTHER_STUDENT_ID), any(CurrentUser.class)))
        .thenThrow(
            new UnauthorizedActionException("Student cannot access another student's profile"));

    mockMvc
        .perform(
            get("/api/students/" + OTHER_STUDENT_ID)
                .header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403));
  }

  @Test
  void student_cannotCreateStudent() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Only admin can create students"))
        .when(studentService)
        .create(any(StudentRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/students")
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"std\":\"STD24191\",\"firstName\":\"John\",\"lastName\":\"Doe\"}"))
        .andExpect(status().isForbidden());

    verify(studentService).create(any(StudentRequest.class), any(CurrentUser.class));
  }

  @Test
  void student_cannotModifyStudent() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Only admin can update students"))
        .when(studentService)
        .update(eq(OTHER_STUDENT_ID), any(StudentRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            put("/api/students/" + OTHER_STUDENT_ID)
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"std\":\"STD24191\",\"firstName\":\"Jane\",\"lastName\":\"Doe\"}"))
        .andExpect(status().isForbidden());

    verify(studentService)
        .update(eq(OTHER_STUDENT_ID), any(StudentRequest.class), any(CurrentUser.class));
  }

  @Test
  void student_cannotDeleteStudent() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Only admin can delete students"))
        .when(studentService)
        .delete(eq(OTHER_STUDENT_ID), any(CurrentUser.class));

    mockMvc
        .perform(
            delete("/api/students/" + OTHER_STUDENT_ID)
                .header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());

    verify(studentService).delete(eq(OTHER_STUDENT_ID), any(CurrentUser.class));
  }

  @Test
  void student_cannotListAllStudents() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Student cannot list all students"))
        .when(studentService)
        .findAll(any(CurrentUser.class));

    mockMvc
        .perform(get("/api/students").header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void student_cannotGetByStd() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Student cannot look up students by std"))
        .when(studentService)
        .findByStd(eq("STD24191"), any(CurrentUser.class));

    mockMvc
        .perform(get("/api/students/std/STD24191").header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void teacher_cannotManageStudents() throws Exception {
    stubTeacher();
    doThrow(new UnauthorizedActionException("Only admin can create students"))
        .when(studentService)
        .create(any(StudentRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/students")
                .header("Authorization", "Bearer teacher-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"std\":\"STD24191\",\"firstName\":\"John\",\"lastName\":\"Doe\"}"))
        .andExpect(status().isForbidden());

    verify(studentService).create(any(StudentRequest.class), any(CurrentUser.class));
  }

  @Test
  void teacher_cannotListStudents() throws Exception {
    stubTeacher();
    doThrow(new UnauthorizedActionException("Only admin can list all students"))
        .when(studentService)
        .findAll(any(CurrentUser.class));

    mockMvc
        .perform(get("/api/students").header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void insufficientRole_shouldReturn403() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Only admin can create students"))
        .when(studentService)
        .create(any(StudentRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/students")
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"std\":\"STD24191\",\"firstName\":\"John\",\"lastName\":\"Doe\"}"))
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
