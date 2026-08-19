package com.hei.app.service;

import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.repository.CourseAssignmentRepository;
import com.hei.app.security.CurrentUser;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityAsserts {
  private final CourseAssignmentRepository courseAssignmentRepository;

  public UUID requireStudentId(CurrentUser currentUser) {
    if (currentUser.studentId() == null) {
      throw new UnauthorizedActionException("Student profile not found");
    }
    return currentUser.studentId();
  }

  public UUID requireTeacherId(CurrentUser currentUser) {
    if (currentUser.teacherId() == null) {
      throw new UnauthorizedActionException("Teacher profile not found");
    }
    return currentUser.teacherId();
  }

  public void assertTeacherAssignedToCourse(UUID teacherId, UUID courseId) {
    if (!isTeacherAssignedToCourse(teacherId, courseId)) {
      throw new UnauthorizedActionException("Teacher is not assigned to this course");
    }
  }

  public boolean isTeacherAssignedToCourse(UUID teacherId, UUID courseId) {
    return courseAssignmentRepository.existsByTeacherIdAndCourseId(teacherId, courseId);
  }
}
