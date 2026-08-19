package com.hei.app.security;

import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.model.Role;
import com.hei.app.model.Student;
import com.hei.app.model.Teacher;
import com.hei.app.repository.StudentRepository;
import com.hei.app.repository.TeacherRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserResolver {
  private final StudentRepository studentRepository;
  private final TeacherRepository teacherRepository;

  public CurrentUser resolve(AppPrincipal principal) {
    UUID accountId = principal.accountId();
    Role role = principal.role();

    return switch (role) {
      case STUDENT -> new CurrentUser(accountId, role, resolveStudentId(accountId), null);
      case TEACHER -> new CurrentUser(accountId, role, null, resolveTeacherId(accountId));
      case ADMIN -> new CurrentUser(accountId, role, null, null);
    };
  }

  private UUID resolveStudentId(UUID accountId) {
    Student student =
        studentRepository
            .findByUserAccountId(accountId)
            .orElseThrow(
                () ->
                    new UnauthorizedActionException(
                        "Student profile not found for user account: " + accountId));
    return student.getId();
  }

  private UUID resolveTeacherId(UUID accountId) {
    Teacher teacher =
        teacherRepository
            .findByUserAccountId(accountId)
            .orElseThrow(
                () ->
                    new UnauthorizedActionException(
                        "Teacher profile not found for user account: " + accountId));
    return teacher.getId();
  }
}
