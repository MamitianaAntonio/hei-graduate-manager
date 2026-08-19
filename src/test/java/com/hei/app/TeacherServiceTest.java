package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.app.dto.teacher.TeacherRequest;
import com.hei.app.dto.teacher.TeacherResponse;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.mapper.TeacherMapper;
import com.hei.app.model.Role;
import com.hei.app.model.Teacher;
import com.hei.app.repository.TeacherRepository;
import com.hei.app.security.CurrentUser;
import com.hei.app.service.SecurityAsserts;
import com.hei.app.service.TeacherService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TeacherServiceTest {
  @Mock private TeacherRepository teacherRepository;

  @Mock private TeacherMapper teacherMapper;

  @Mock private SecurityAsserts securityAsserts;

  @InjectMocks private TeacherService teacherService;

  @Test
  void create_shouldReturnTeacher() {
    TeacherRequest request = mock(TeacherRequest.class);
    Teacher teacher = new Teacher();
    Teacher savedTeacher = new Teacher();
    TeacherResponse response = mock(TeacherResponse.class);

    when(teacherMapper.toEntity(request)).thenReturn(teacher);
    when(teacherRepository.save(teacher)).thenReturn(savedTeacher);
    when(teacherMapper.toResponse(savedTeacher)).thenReturn(response);

    TeacherResponse result = teacherService.create(request, admin());

    assertEquals(response, result);
  }

  @Test
  void student_cannotCreateTeacher() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> teacherService.create(mock(TeacherRequest.class), student(UUID.randomUUID())));
  }

  @Test
  void teacher_cannotCreateTeacher() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> teacherService.create(mock(TeacherRequest.class), teacher(UUID.randomUUID())));
  }

  @Test
  void findById_shouldReturnTeacher() {
    UUID id = UUID.randomUUID();
    Teacher teacher = new Teacher();
    TeacherResponse response = mock(TeacherResponse.class);

    when(teacherRepository.findById(id)).thenReturn(Optional.of(teacher));
    when(teacherMapper.toResponse(teacher)).thenReturn(response);

    TeacherResponse result = teacherService.findById(id, admin());

    assertEquals(response, result);
  }

  @Test
  void findById_shouldThrowWhenTeacherDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(teacherRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> teacherService.findById(id, admin()));
  }

  @Test
  void teacher_canReadOwnProfile() {
    UUID teacherId = UUID.randomUUID();
    CurrentUser currentUser = teacher(teacherId);
    Teacher teacher = new Teacher();
    TeacherResponse response = mock(TeacherResponse.class);

    when(securityAsserts.requireTeacherId(currentUser)).thenReturn(teacherId);
    when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
    when(teacherMapper.toResponse(teacher)).thenReturn(response);

    TeacherResponse result = teacherService.findById(teacherId, currentUser);

    assertEquals(response, result);
  }

  @Test
  void student_cannotAccessTeacherProfile() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> teacherService.findById(UUID.randomUUID(), student(UUID.randomUUID())));
  }

  @Test
  void findAll_shouldReturnTeachers() {
    Teacher teacher1 = new Teacher();
    teacher1.setFirstName("Teacher1");
    Teacher teacher2 = new Teacher();
    teacher2.setFirstName("Teacher2");
    TeacherResponse response1 = mock(TeacherResponse.class);
    TeacherResponse response2 = mock(TeacherResponse.class);

    when(teacherRepository.findAll()).thenReturn(List.of(teacher1, teacher2));
    when(teacherMapper.toResponse(teacher1)).thenReturn(response1);
    when(teacherMapper.toResponse(teacher2)).thenReturn(response2);

    List<TeacherResponse> result = teacherService.findAll(admin());

    assertEquals(List.of(response1, response2), result);
  }

  @Test
  void teacher_cannotListAllTeachers() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> teacherService.findAll(teacher(UUID.randomUUID())));
  }

  @Test
  void findByUserAccountId_shouldReturnTeacher() {
    UUID userAccountId = UUID.randomUUID();
    Teacher teacher = new Teacher();
    TeacherResponse response = mock(TeacherResponse.class);

    when(teacherRepository.findByUserAccountId(userAccountId)).thenReturn(Optional.of(teacher));
    when(teacherMapper.toResponse(teacher)).thenReturn(response);

    TeacherResponse result = teacherService.findByUserAccountId(userAccountId, admin());

    assertEquals(response, result);
  }

  @Test
  void findByUserAccountId_shouldThrowWhenTeacherDoesNotExist() {
    UUID userAccountId = UUID.randomUUID();

    when(teacherRepository.findByUserAccountId(userAccountId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> teacherService.findByUserAccountId(userAccountId, admin()));
  }

  @Test
  void teacher_canReadOwnProfileByUserAccount() {
    UUID accountId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    Teacher teacher = new Teacher();
    TeacherResponse response = mock(TeacherResponse.class);

    when(teacherRepository.findByUserAccountId(accountId)).thenReturn(Optional.of(teacher));
    when(teacherMapper.toResponse(teacher)).thenReturn(response);

    TeacherResponse result =
        teacherService.findByUserAccountId(
            accountId, new CurrentUser(accountId, Role.TEACHER, null, teacherId));

    assertEquals(response, result);
  }

  @Test
  void update_shouldReturnUpdatedTeacher() {
    UUID id = UUID.randomUUID();
    TeacherRequest request = mock(TeacherRequest.class);
    Teacher teacher = new Teacher();
    Teacher updatedTeacher = new Teacher();
    TeacherResponse response = mock(TeacherResponse.class);

    when(request.firstName()).thenReturn("Jean");
    when(request.lastName()).thenReturn("Dupont");
    when(teacherRepository.findById(id)).thenReturn(Optional.of(teacher));
    when(teacherRepository.save(teacher)).thenReturn(updatedTeacher);
    when(teacherMapper.toResponse(updatedTeacher)).thenReturn(response);

    TeacherResponse result = teacherService.update(id, request, admin());

    assertEquals(response, result);
  }

  @Test
  void update_shouldThrowWhenTeacherDoesNotExist() {
    UUID id = UUID.randomUUID();
    TeacherRequest request = mock(TeacherRequest.class);

    when(teacherRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> teacherService.update(id, request, admin()));
  }

  @Test
  void student_cannotUpdateTeacher() {
    assertThrows(
        UnauthorizedActionException.class,
        () ->
            teacherService.update(
                UUID.randomUUID(), mock(TeacherRequest.class), student(UUID.randomUUID())));
  }

  @Test
  void delete_shouldDeleteTeacher() {
    UUID id = UUID.randomUUID();

    when(teacherRepository.existsById(id)).thenReturn(true);

    teacherService.delete(id, admin());

    verify(teacherRepository).deleteById(id);
  }

  @Test
  void delete_shouldThrowWhenTeacherDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(teacherRepository.existsById(id)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> teacherService.delete(id, admin()));
  }

  @Test
  void teacher_cannotDeleteTeacher() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> teacherService.delete(UUID.randomUUID(), teacher(UUID.randomUUID())));
  }

  private CurrentUser admin() {
    return new CurrentUser(UUID.randomUUID(), Role.ADMIN, null, null);
  }

  private CurrentUser student(UUID studentId) {
    return new CurrentUser(UUID.randomUUID(), Role.STUDENT, studentId, null);
  }

  private CurrentUser teacher(UUID teacherId) {
    return new CurrentUser(UUID.randomUUID(), Role.TEACHER, null, teacherId);
  }
}
