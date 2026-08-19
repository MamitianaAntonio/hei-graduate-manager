package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.model.Role;
import com.hei.app.model.Student;
import com.hei.app.model.Teacher;
import com.hei.app.repository.StudentRepository;
import com.hei.app.repository.TeacherRepository;
import com.hei.app.security.AppPrincipal;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CurrentUserResolverTest {
  @Mock private StudentRepository studentRepository;

  @Mock private TeacherRepository teacherRepository;

  @InjectMocks private CurrentUserResolver currentUserResolver;

  @Test
  void resolve_shouldReturnStudentId_whenProfileExists() {
    UUID accountId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    Student student = studentWith(studentId);
    when(studentRepository.findByUserAccountId(accountId)).thenReturn(Optional.of(student));

    CurrentUser currentUser =
        currentUserResolver.resolve(new AppPrincipal(accountId, "student@hei.com", Role.STUDENT));

    assertEquals(accountId, currentUser.accountId());
    assertEquals(Role.STUDENT, currentUser.role());
    assertEquals(studentId, currentUser.studentId());
    assertNull(currentUser.teacherId());
  }

  @Test
  void resolve_shouldThrow_whenStudentProfileMissing() {
    UUID accountId = UUID.randomUUID();
    when(studentRepository.findByUserAccountId(accountId)).thenReturn(Optional.empty());

    assertThrows(
        UnauthorizedActionException.class,
        () ->
            currentUserResolver.resolve(
                new AppPrincipal(accountId, "student@hei.com", Role.STUDENT)));
  }

  @Test
  void resolve_shouldReturnTeacherId_whenProfileExists() {
    UUID accountId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    Teacher teacher = teacherWith(teacherId);
    when(teacherRepository.findByUserAccountId(accountId)).thenReturn(Optional.of(teacher));

    CurrentUser currentUser =
        currentUserResolver.resolve(new AppPrincipal(accountId, "teacher@hei.com", Role.TEACHER));

    assertEquals(accountId, currentUser.accountId());
    assertEquals(Role.TEACHER, currentUser.role());
    assertEquals(teacherId, currentUser.teacherId());
    assertNull(currentUser.studentId());
  }

  @Test
  void resolve_shouldThrow_whenTeacherProfileMissing() {
    UUID accountId = UUID.randomUUID();
    when(teacherRepository.findByUserAccountId(accountId)).thenReturn(Optional.empty());

    assertThrows(
        UnauthorizedActionException.class,
        () ->
            currentUserResolver.resolve(
                new AppPrincipal(accountId, "teacher@hei.com", Role.TEACHER)));
  }

  @Test
  void resolve_shouldNotLookupProfileForAdmin() {
    UUID accountId = UUID.randomUUID();

    CurrentUser currentUser =
        currentUserResolver.resolve(new AppPrincipal(accountId, "admin@hei.com", Role.ADMIN));

    assertEquals(accountId, currentUser.accountId());
    assertEquals(Role.ADMIN, currentUser.role());
    assertNull(currentUser.studentId());
    assertNull(currentUser.teacherId());
    verifyNoInteractions(studentRepository, teacherRepository);
  }

  @Test
  void resolve_shouldReturnAccountIdAndRole() {
    UUID accountId = UUID.randomUUID();
    when(studentRepository.findByUserAccountId(accountId))
        .thenReturn(Optional.of(studentWith(UUID.randomUUID())));

    CurrentUser currentUser =
        currentUserResolver.resolve(new AppPrincipal(accountId, "student@hei.com", Role.STUDENT));

    assertEquals(accountId, currentUser.accountId());
    assertEquals(Role.STUDENT, currentUser.role());
  }

  private Student studentWith(UUID id) {
    Student student = new Student();
    student.setId(id);
    return student;
  }

  private Teacher teacherWith(UUID id) {
    Teacher teacher = new Teacher();
    teacher.setId(id);
    return teacher;
  }
}
