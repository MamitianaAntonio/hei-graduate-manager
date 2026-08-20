package com.hei.app.service;

import com.hei.app.dto.gradeHistory.GradeHistoryResponse;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.mapper.GradeHistoryMapper;
import com.hei.app.model.Grade;
import com.hei.app.model.Role;
import com.hei.app.repository.GradeHistoryRepository;
import com.hei.app.repository.GradeRepository;
import com.hei.app.security.CurrentUser;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeHistoryService {
  private final GradeHistoryRepository gradeHistoryRepository;
  private final GradeHistoryMapper gradeHistoryMapper;
  private final GradeRepository gradeRepository;
  private final SecurityAsserts securityAsserts;

  public List<GradeHistoryResponse> findByGradeId(UUID gradeId, CurrentUser currentUser) {
    Grade grade =
        gradeRepository
            .findById(gradeId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Grade not found with id: " + gradeId));

    if (currentUser.role() == Role.STUDENT) {
      UUID studentId = securityAsserts.requireStudentId(currentUser);
      if (!grade.getStudent().getId().equals(studentId)) {
        throw new UnauthorizedActionException("Student cannot access another student's grade");
      }
    } else if (currentUser.role() == Role.TEACHER) {
      UUID teacherId = securityAsserts.requireTeacherId(currentUser);
      securityAsserts.assertTeacherAssignedToCourse(teacherId, courseIdOfGrade(grade));
    }

    return gradeHistoryRepository.findByGradeIdOrderByModifiedAtDesc(gradeId).stream()
        .map(gradeHistoryMapper::toResponse)
        .toList();
  }

  private UUID courseIdOfGrade(Grade grade) {
    return grade.getExam().getCourse().getId();
  }
}
