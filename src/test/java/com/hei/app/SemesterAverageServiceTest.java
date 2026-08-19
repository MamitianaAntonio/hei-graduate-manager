package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.app.exceptions.BusinessException;
import com.hei.app.model.Course;
import com.hei.app.model.CourseAssignment;
import com.hei.app.model.Semester;
import com.hei.app.repository.CourseAssignmentRepository;
import com.hei.app.service.CourseAverageService;
import com.hei.app.service.SemesterAverageService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SemesterAverageServiceTest {
  @Mock private CourseAssignmentRepository courseAssignmentRepository;

  @Mock private CourseAverageService courseAverageService;

  @InjectMocks private SemesterAverageService semesterAverageService;

  @Test
  void calculate_shouldReturnSemesterAverage() {
    UUID studentId = UUID.randomUUID();
    Semester semester = Semester.S1;
    Integer academicYear = 2024;

    Course courseA = courseWith(UUID.randomUUID(), 6);
    Course courseB = courseWith(UUID.randomUUID(), 3);
    Course courseC = courseWith(UUID.randomUUID(), 3);

    when(courseAssignmentRepository.findBySemesterAndAcademicYear(semester, academicYear))
        .thenReturn(
            List.of(assignmentFor(courseA), assignmentFor(courseB), assignmentFor(courseC)));
    when(courseAverageService.calculateCourseAverage(studentId, courseA.getId()))
        .thenReturn(BigDecimal.valueOf(14));
    when(courseAverageService.calculateCourseAverage(studentId, courseB.getId()))
        .thenReturn(BigDecimal.valueOf(12));
    when(courseAverageService.calculateCourseAverage(studentId, courseC.getId()))
        .thenReturn(BigDecimal.valueOf(10));

    SemesterAverageService.SemesterAverage result =
        semesterAverageService.calculate(studentId, semester, academicYear);

    assertEquals(new BigDecimal("12.50"), result.average());
    assertEquals(BigDecimal.valueOf(12), result.totalCredits());

    verify(courseAverageService).calculateCourseAverage(studentId, courseA.getId());
    verify(courseAverageService).calculateCourseAverage(studentId, courseB.getId());
    verify(courseAverageService).calculateCourseAverage(studentId, courseC.getId());
  }

  @Test
  void calculate_shouldUseCourseCredits() {
    UUID studentId = UUID.randomUUID();
    Semester semester = Semester.S1;
    Integer academicYear = 2024;

    Course courseA = courseWith(UUID.randomUUID(), 6);
    Course courseB = courseWith(UUID.randomUUID(), 2);

    when(courseAssignmentRepository.findBySemesterAndAcademicYear(semester, academicYear))
        .thenReturn(List.of(assignmentFor(courseA), assignmentFor(courseB)));
    when(courseAverageService.calculateCourseAverage(studentId, courseA.getId()))
        .thenReturn(BigDecimal.valueOf(14));
    when(courseAverageService.calculateCourseAverage(studentId, courseB.getId()))
        .thenReturn(BigDecimal.valueOf(10));

    SemesterAverageService.SemesterAverage result =
        semesterAverageService.calculate(studentId, semester, academicYear);

    assertEquals(new BigDecimal("13.00"), result.average());
    assertEquals(BigDecimal.valueOf(8), result.totalCredits());
  }

  @Test
  void calculate_shouldIgnoreDuplicateCourses() {
    UUID studentId = UUID.randomUUID();
    Semester semester = Semester.S1;
    Integer academicYear = 2024;

    Course courseA = courseWith(UUID.randomUUID(), 4);

    when(courseAssignmentRepository.findBySemesterAndAcademicYear(semester, academicYear))
        .thenReturn(List.of(assignmentFor(courseA), assignmentFor(courseA)));
    when(courseAverageService.calculateCourseAverage(studentId, courseA.getId()))
        .thenReturn(BigDecimal.valueOf(12));

    SemesterAverageService.SemesterAverage result =
        semesterAverageService.calculate(studentId, semester, academicYear);

    assertEquals(new BigDecimal("12.00"), result.average());
    assertEquals(BigDecimal.valueOf(4), result.totalCredits());
  }

  @Test
  void calculate_shouldThrowWhenNoCoursesAreAvailable() {
    UUID studentId = UUID.randomUUID();
    Semester semester = Semester.S1;
    Integer academicYear = 2024;

    when(courseAssignmentRepository.findBySemesterAndAcademicYear(semester, academicYear))
        .thenReturn(List.of());

    assertThrows(
        BusinessException.class,
        () -> semesterAverageService.calculate(studentId, semester, academicYear));
  }

  @Test
  void calculate_shouldPropagateCourseAverageErrorWhenNoGradesAreAvailable() {
    UUID studentId = UUID.randomUUID();
    Semester semester = Semester.S1;
    Integer academicYear = 2024;

    Course courseA = courseWith(UUID.randomUUID(), 6);

    when(courseAssignmentRepository.findBySemesterAndAcademicYear(semester, academicYear))
        .thenReturn(List.of(assignmentFor(courseA)));
    when(courseAverageService.calculateCourseAverage(studentId, courseA.getId()))
        .thenThrow(new BusinessException("No grades available"));

    assertThrows(
        BusinessException.class,
        () -> semesterAverageService.calculate(studentId, semester, academicYear));
  }

  private Course courseWith(UUID id, Integer credits) {
    Course course = new Course();
    course.setId(id);
    course.setCredits(credits);
    return course;
  }

  private CourseAssignment assignmentFor(Course course) {
    CourseAssignment assignment = new CourseAssignment();
    assignment.setCourse(course);
    return assignment;
  }
}
