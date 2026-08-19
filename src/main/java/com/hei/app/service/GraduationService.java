package com.hei.app.service;

import com.hei.app.exceptions.BusinessException;
import com.hei.app.model.Course;
import com.hei.app.model.CourseAssignment;
import com.hei.app.model.Group;
import com.hei.app.model.StudentGroupHistory;
import com.hei.app.repository.CourseAssignmentRepository;
import com.hei.app.repository.StudentGroupHistoryRepository;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GraduationService {
  private static final BigDecimal PASS_MARK = BigDecimal.valueOf(10);

  private final StudentGroupHistoryRepository studentGroupHistoryRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final CourseAverageService courseAverageService;

  public boolean isGraduable(UUID studentId) {
    List<StudentGroupHistory> history = studentGroupHistoryRepository.findByStudentId(studentId);

    if (history.isEmpty()) {
      throw new BusinessException("No group history found for student " + studentId);
    }

    Map<UUID, Course> courses = coursesOf(studentId, history);

    for (Course course : courses.values()) {
      BigDecimal average;
      try {
        average = courseAverageService.calculateCourseAverage(studentId, course.getId());
      } catch (BusinessException e) {
        return false;
      }

      if (average.compareTo(PASS_MARK) < 0) {
        return false;
      }
    }

    return true;
  }

  private Map<UUID, Course> coursesOf(UUID studentId, List<StudentGroupHistory> history) {
    Map<UUID, Course> courses = new LinkedHashMap<>();

    for (StudentGroupHistory entry : history) {
      Group group = entry.getGroup();

      for (CourseAssignment assignment : courseAssignmentRepository.findByGroupId(group.getId())) {
        SemesterPeriods.Period period =
            SemesterPeriods.of(assignment.getSemester(), assignment.getAcademicYear());

        if (SemesterPeriods.overlaps(entry.getStartDate(), entry.getEndDate(), period)) {
          courses.putIfAbsent(assignment.getCourse().getId(), assignment.getCourse());
        }
      }
    }

    if (courses.isEmpty()) {
      throw new BusinessException("No courses found for student " + studentId);
    }

    return courses;
  }
}
