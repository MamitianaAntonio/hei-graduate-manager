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

import com.hei.app.controller.CourseController;
import com.hei.app.dto.course.CourseRequest;
import com.hei.app.dto.course.CourseResponse;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.model.Role;
import com.hei.app.security.AppPrincipal;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.security.CustomUserDetailsService;
import com.hei.app.security.JwtService;
import com.hei.app.security.SecurityConfig;
import com.hei.app.service.CourseService;
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

@WebMvcTest(controllers = CourseController.class)
@Import(SecurityConfig.class)
public class CourseControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private CourseService courseService;

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
    mockMvc.perform(get("/api/courses")).andExpect(status().isForbidden());
  }

  @Test
  void admin_canCreateCourse() throws Exception {
    stubAdmin();
    UUID newId = UUID.randomUUID();
    CourseResponse response = new CourseResponse(newId, "PROG4", "Programmation 4", 6);
    when(courseService.create(any(CourseRequest.class), any(CurrentUser.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/api/courses")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ref\":\"PROG4\",\"title\":\"Programmation 4\",\"credits\":6}"))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.ref").value("PROG4"))
        .andExpect(jsonPath("$.title").value("Programmation 4"))
        .andExpect(jsonPath("$.credits").value(6));

    verify(courseService).create(any(CourseRequest.class), any(CurrentUser.class));
  }

  @Test
  void admin_canGetCourseById() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();
    CourseResponse response = new CourseResponse(id, "PROG4", "Programmation 4", 6);
    when(courseService.findById(eq(id), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/courses/" + id).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ref").value("PROG4"));
  }

  @Test
  void admin_canGetCourseByRef() throws Exception {
    stubAdmin();
    CourseResponse response = new CourseResponse(UUID.randomUUID(), "PROG4", "Programmation 4", 6);
    when(courseService.findByRef(eq("PROG4"), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/courses/ref/PROG4").header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ref").value("PROG4"));
  }

  @Test
  void admin_canListCourses() throws Exception {
    stubAdmin();
    CourseResponse c1 = new CourseResponse(UUID.randomUUID(), "PROG4", "Programmation 4", 6);
    CourseResponse c2 = new CourseResponse(UUID.randomUUID(), "SYS3", "Systemes 3", 5);
    when(courseService.findAll(any(CurrentUser.class))).thenReturn(List.of(c1, c2));

    mockMvc
        .perform(get("/api/courses").header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));

    verify(courseService).findAll(any(CurrentUser.class));
  }

  @Test
  void admin_canUpdateCourse() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();
    CourseResponse response = new CourseResponse(id, "PROG5", "Programmation 5", 7);
    when(courseService.update(eq(id), any(CourseRequest.class), any(CurrentUser.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            put("/api/courses/" + id)
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ref\":\"PROG5\",\"title\":\"Programmation 5\",\"credits\":7}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ref").value("PROG5"));
  }

  @Test
  void admin_canDeleteCourse() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/courses/" + id).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isNoContent());

    verify(courseService).delete(eq(id), any(CurrentUser.class));
  }

  @Test
  void student_canReadCourse() throws Exception {
    stubStudent();
    UUID id = UUID.randomUUID();
    CourseResponse response = new CourseResponse(id, "PROG4", "Programmation 4", 6);
    when(courseService.findById(eq(id), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/courses/" + id).header("Authorization", "Bearer student-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ref").value("PROG4"));
  }

  @Test
  void student_canListCourses() throws Exception {
    stubStudent();
    CourseResponse c1 = new CourseResponse(UUID.randomUUID(), "PROG4", "Programmation 4", 6);
    when(courseService.findAll(any(CurrentUser.class))).thenReturn(List.of(c1));

    mockMvc
        .perform(get("/api/courses").header("Authorization", "Bearer student-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void student_cannotCreateCourse() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Only admin can create courses"))
        .when(courseService)
        .create(any(CourseRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/courses")
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ref\":\"PROG4\",\"title\":\"Programmation 4\",\"credits\":6}"))
        .andExpect(status().isForbidden());

    verify(courseService).create(any(CourseRequest.class), any(CurrentUser.class));
  }

  @Test
  void student_cannotUpdateCourse() throws Exception {
    stubStudent();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can update courses"))
        .when(courseService)
        .update(eq(id), any(CourseRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            put("/api/courses/" + id)
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ref\":\"PROG5\",\"title\":\"Programmation 5\",\"credits\":7}"))
        .andExpect(status().isForbidden());

    verify(courseService).update(eq(id), any(CourseRequest.class), any(CurrentUser.class));
  }

  @Test
  void student_cannotDeleteCourse() throws Exception {
    stubStudent();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can delete courses"))
        .when(courseService)
        .delete(eq(id), any(CurrentUser.class));

    mockMvc
        .perform(delete("/api/courses/" + id).header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());

    verify(courseService).delete(eq(id), any(CurrentUser.class));
  }

  @Test
  void teacher_canReadCourse() throws Exception {
    stubTeacher();
    UUID id = UUID.randomUUID();
    CourseResponse response = new CourseResponse(id, "PROG4", "Programmation 4", 6);
    when(courseService.findById(eq(id), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/courses/" + id).header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ref").value("PROG4"));
  }

  @Test
  void teacher_cannotCreateCourse() throws Exception {
    stubTeacher();
    doThrow(new UnauthorizedActionException("Only admin can create courses"))
        .when(courseService)
        .create(any(CourseRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/courses")
                .header("Authorization", "Bearer teacher-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ref\":\"PROG4\",\"title\":\"Programmation 4\",\"credits\":6}"))
        .andExpect(status().isForbidden());

    verify(courseService).create(any(CourseRequest.class), any(CurrentUser.class));
  }

  @Test
  void teacher_cannotUpdateCourse() throws Exception {
    stubTeacher();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can update courses"))
        .when(courseService)
        .update(eq(id), any(CourseRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            put("/api/courses/" + id)
                .header("Authorization", "Bearer teacher-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ref\":\"PROG5\",\"title\":\"Programmation 5\",\"credits\":7}"))
        .andExpect(status().isForbidden());

    verify(courseService).update(eq(id), any(CourseRequest.class), any(CurrentUser.class));
  }

  @Test
  void teacher_cannotDeleteCourse() throws Exception {
    stubTeacher();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can delete courses"))
        .when(courseService)
        .delete(eq(id), any(CurrentUser.class));

    mockMvc
        .perform(delete("/api/courses/" + id).header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isForbidden());

    verify(courseService).delete(eq(id), any(CurrentUser.class));
  }

  @Test
  void insufficientRole_isRejected() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Only admin can create courses"))
        .when(courseService)
        .create(any(CourseRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/courses")
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ref\":\"PROG4\",\"title\":\"Programmation 4\",\"credits\":6}"))
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
