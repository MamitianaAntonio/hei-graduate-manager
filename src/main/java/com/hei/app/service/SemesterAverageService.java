package com.hei.app.service;

import com.hei.app.exceptions.BusinessException;
import com.hei.app.model.Course;
import com.hei.app.model.CourseAssignment;
import com.hei.app.model.Semester;
import com.hei.app.model.StudentGroupHistory;
import com.hei.app.repository.CourseAssignmentRepository;
import com.hei.app.repository.StudentGroupHistoryRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SemesterAverageService {
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final StudentGroupHistoryRepository studentGroupHistoryRepository;
  private final CourseAverageService courseAverageService;

  public record SemesterAverage(BigDecimal average, BigDecimal totalCredits) {}

  public SemesterAverage calculate(UUID studentId, Semester semester, Integer academicYear) {
    List<UUID> groupIds = groupIdsFor(studentId, semester, academicYear);

    List<CourseAssignment> assignments =
        courseAssignmentRepository.findByGroupIdInAndSemesterAndAcademicYear(
            groupIds, semester, academicYear);

    if (assignments.isEmpty()) {
      throw new BusinessException(
          "No courses available for student "
              + studentId
              + " in semester "
              + semester
              + " of academic year "
              + academicYear);
    }

    Map<UUID, Course> courses = new LinkedHashMap<>();
    for (CourseAssignment assignment : assignments) {
      courses.putIfAbsent(assignment.getCourse().getId(), assignment.getCourse());
    }

    BigDecimal weightedSum = BigDecimal.ZERO;
    BigDecimal totalCredits = BigDecimal.ZERO;

    for (Course course : courses.values()) {
      BigDecimal courseAverage =
          courseAverageService.calculateCourseAverage(studentId, course.getId());
      BigDecimal credits = BigDecimal.valueOf(course.getCredits());

      weightedSum = weightedSum.add(courseAverage.multiply(credits));
      totalCredits = totalCredits.add(credits);
    }

    if (totalCredits.compareTo(BigDecimal.ZERO) == 0) {
      throw new BusinessException(
          "No credits available to compute semester average for student " + studentId);
    }

    BigDecimal average = weightedSum.divide(totalCredits, 2, RoundingMode.HALF_UP);
    return new SemesterAverage(average, totalCredits);
  }

  private List<UUID> groupIdsFor(UUID studentId, Semester semester, Integer academicYear) {
    List<StudentGroupHistory> history = studentGroupHistoryRepository.findByStudentId(studentId);

    if (history.isEmpty()) {
      throw new BusinessException("No group history found for student " + studentId);
    }

    SemesterPeriods.Period period = SemesterPeriods.of(semester, academicYear);

    List<UUID> groupIds = new ArrayList<>();
    for (StudentGroupHistory entry : history) {
      if (SemesterPeriods.overlaps(entry.getStartDate(), entry.getEndDate(), period)) {
        groupIds.add(entry.getGroup().getId());
      }
    }

    if (groupIds.isEmpty()) {
      throw new BusinessException(
          "No group assigned to student "
              + studentId
              + " for semester "
              + semester
              + " of academic year "
              + academicYear);
    }

    return groupIds;
  }
}
