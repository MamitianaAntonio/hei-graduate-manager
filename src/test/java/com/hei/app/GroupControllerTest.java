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

import com.hei.app.controller.GroupController;
import com.hei.app.dto.group.GroupRequest;
import com.hei.app.dto.group.GroupResponse;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.model.Role;
import com.hei.app.model.Track;
import com.hei.app.security.AppPrincipal;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.security.CustomUserDetailsService;
import com.hei.app.security.JwtService;
import com.hei.app.security.SecurityConfig;
import com.hei.app.service.GroupService;
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

@WebMvcTest(controllers = GroupController.class)
@Import(SecurityConfig.class)
public class GroupControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private GroupService groupService;

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
    mockMvc.perform(get("/api/groups")).andExpect(status().isForbidden());
  }

  @Test
  void admin_canCreateGroup() throws Exception {
    stubAdmin();
    UUID newId = UUID.randomUUID();
    GroupResponse response = new GroupResponse(newId, "G1", Track.COMMON);
    when(groupService.create(any(GroupRequest.class), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/groups")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ref\":\"G1\",\"track\":\"COMMON\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.ref").value("G1"))
        .andExpect(jsonPath("$.track").value("COMMON"));

    verify(groupService).create(any(GroupRequest.class), any(CurrentUser.class));
  }

  @Test
  void admin_canGetGroupById() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();
    GroupResponse response = new GroupResponse(id, "G1", Track.COMMON);
    when(groupService.findById(eq(id), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/groups/" + id).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ref").value("G1"));
  }

  @Test
  void admin_canGetGroupByRef() throws Exception {
    stubAdmin();
    GroupResponse response = new GroupResponse(UUID.randomUUID(), "G1", Track.EL);
    when(groupService.findByRef(eq("G1"), any(CurrentUser.class))).thenReturn(response);

    mockMvc
        .perform(get("/api/groups/ref/G1").header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ref").value("G1"))
        .andExpect(jsonPath("$.track").value("EL"));
  }

  @Test
  void admin_canListGroups() throws Exception {
    stubAdmin();
    GroupResponse g1 = new GroupResponse(UUID.randomUUID(), "G1", Track.COMMON);
    GroupResponse g2 = new GroupResponse(UUID.randomUUID(), "G2", Track.TN);
    when(groupService.findAll(any(CurrentUser.class))).thenReturn(List.of(g1, g2));

    mockMvc
        .perform(get("/api/groups").header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));

    verify(groupService).findAll(any(CurrentUser.class));
  }

  @Test
  void admin_canUpdateGroup() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();
    GroupResponse response = new GroupResponse(id, "G2", Track.TN);
    when(groupService.update(eq(id), any(GroupRequest.class), any(CurrentUser.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            put("/api/groups/" + id)
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ref\":\"G2\",\"track\":\"TN\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ref").value("G2"))
        .andExpect(jsonPath("$.track").value("TN"));
  }

  @Test
  void admin_canDeleteGroup() throws Exception {
    stubAdmin();
    UUID id = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/groups/" + id).header("Authorization", "Bearer admin-token"))
        .andExpect(status().isNoContent());

    verify(groupService).delete(eq(id), any(CurrentUser.class));
  }

  @Test
  void student_cannotCreateGroup() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Only admin can create groups"))
        .when(groupService)
        .create(any(GroupRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/groups")
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ref\":\"G1\",\"track\":\"COMMON\"}"))
        .andExpect(status().isForbidden());

    verify(groupService).create(any(GroupRequest.class), any(CurrentUser.class));
  }

  @Test
  void student_cannotUpdateGroup() throws Exception {
    stubStudent();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can update groups"))
        .when(groupService)
        .update(eq(id), any(GroupRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            put("/api/groups/" + id)
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ref\":\"G2\",\"track\":\"TN\"}"))
        .andExpect(status().isForbidden());

    verify(groupService).update(eq(id), any(GroupRequest.class), any(CurrentUser.class));
  }

  @Test
  void student_cannotDeleteGroup() throws Exception {
    stubStudent();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can delete groups"))
        .when(groupService)
        .delete(eq(id), any(CurrentUser.class));

    mockMvc
        .perform(delete("/api/groups/" + id).header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());

    verify(groupService).delete(eq(id), any(CurrentUser.class));
  }

  @Test
  void teacher_cannotCreateGroup() throws Exception {
    stubTeacher();
    doThrow(new UnauthorizedActionException("Only admin can create groups"))
        .when(groupService)
        .create(any(GroupRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/groups")
                .header("Authorization", "Bearer teacher-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ref\":\"G1\",\"track\":\"COMMON\"}"))
        .andExpect(status().isForbidden());

    verify(groupService).create(any(GroupRequest.class), any(CurrentUser.class));
  }

  @Test
  void teacher_cannotUpdateGroup() throws Exception {
    stubTeacher();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can update groups"))
        .when(groupService)
        .update(eq(id), any(GroupRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            put("/api/groups/" + id)
                .header("Authorization", "Bearer teacher-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ref\":\"G2\",\"track\":\"TN\"}"))
        .andExpect(status().isForbidden());

    verify(groupService).update(eq(id), any(GroupRequest.class), any(CurrentUser.class));
  }

  @Test
  void teacher_cannotDeleteGroup() throws Exception {
    stubTeacher();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can delete groups"))
        .when(groupService)
        .delete(eq(id), any(CurrentUser.class));

    mockMvc
        .perform(delete("/api/groups/" + id).header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isForbidden());

    verify(groupService).delete(eq(id), any(CurrentUser.class));
  }

  @Test
  void student_cannotGetGroupById() throws Exception {
    stubStudent();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can read groups"))
        .when(groupService)
        .findById(eq(id), any(CurrentUser.class));

    mockMvc
        .perform(get("/api/groups/" + id).header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void student_cannotListGroups() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Only admin can read groups"))
        .when(groupService)
        .findAll(any(CurrentUser.class));

    mockMvc
        .perform(get("/api/groups").header("Authorization", "Bearer student-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void teacher_cannotGetGroupById() throws Exception {
    stubTeacher();
    UUID id = UUID.randomUUID();
    doThrow(new UnauthorizedActionException("Only admin can read groups"))
        .when(groupService)
        .findById(eq(id), any(CurrentUser.class));

    mockMvc
        .perform(get("/api/groups/" + id).header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void teacher_cannotListGroups() throws Exception {
    stubTeacher();
    doThrow(new UnauthorizedActionException("Only admin can read groups"))
        .when(groupService)
        .findAll(any(CurrentUser.class));

    mockMvc
        .perform(get("/api/groups").header("Authorization", "Bearer teacher-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void insufficientRole_isRejected() throws Exception {
    stubStudent();
    doThrow(new UnauthorizedActionException("Only admin can create groups"))
        .when(groupService)
        .create(any(GroupRequest.class), any(CurrentUser.class));

    mockMvc
        .perform(
            post("/api/groups")
                .header("Authorization", "Bearer student-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ref\":\"G1\",\"track\":\"COMMON\"}"))
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
