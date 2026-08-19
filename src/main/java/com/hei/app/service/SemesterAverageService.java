package com.hei.app.service;

import com.hei.app.exceptions.BusinessException;
import com.hei.app.model.Course;
import com.hei.app.model.CourseAssignment;
import com.hei.app.model.Semester;
import com.hei.app.repository.CourseAssignmentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
  private final CourseAverageService courseAverageService;

  public record SemesterAverage(BigDecimal average, BigDecimal totalCredits) {}

  public SemesterAverage calculate(UUID studentId, Semester semester, Integer academicYear) {
    List<CourseAssignment> assignments =
        courseAssignmentRepository.findBySemesterAndAcademicYear(semester, academicYear);

    if (assignments.isEmpty()) {
      throw new BusinessException(
          "No courses available for semester " + semester + " in academic year " + academicYear);
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
}
