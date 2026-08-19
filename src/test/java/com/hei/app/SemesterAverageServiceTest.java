package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.app.exceptions.BusinessException;
import com.hei.app.model.Course;
import com.hei.app.model.CourseAssignment;
import com.hei.app.model.Group;
import com.hei.app.model.Semester;
import com.hei.app.model.StudentGroupHistory;
import com.hei.app.repository.CourseAssignmentRepository;
import com.hei.app.repository.StudentGroupHistoryRepository;
import com.hei.app.service.CourseAverageService;
import com.hei.app.service.SemesterAverageService;
import java.math.BigDecimal;
import java.time.Instant;
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

  @Mock private StudentGroupHistoryRepository studentGroupHistoryRepository;

  @Mock private CourseAverageService courseAverageService;

  @InjectMocks private SemesterAverageService semesterAverageService;

  @Test
  void calculate_shouldReturnSemesterAverage() {
    UUID studentId = UUID.randomUUID();
    Semester semester = Semester.S1;
    Integer academicYear = 2024;

    Group group = groupWith(UUID.randomUUID());
    Course courseA = courseWith(UUID.randomUUID(), 6);
    Course courseB = courseWith(UUID.randomUUID(), 3);
    Course courseC = courseWith(UUID.randomUUID(), 3);

    when(studentGroupHistoryRepository.findByStudentId(studentId))
        .thenReturn(List.of(historyFor(group, "2024-01-01T00:00:00Z", "2024-06-30T23:59:59Z")));
    when(courseAssignmentRepository.findByGroupIdInAndSemesterAndAcademicYear(
            List.of(group.getId()), semester, academicYear))
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

    Group group = groupWith(UUID.randomUUID());
    Course courseA = courseWith(UUID.randomUUID(), 6);
    Course courseB = courseWith(UUID.randomUUID(), 2);

    when(studentGroupHistoryRepository.findByStudentId(studentId))
        .thenReturn(List.of(historyFor(group, "2024-01-01T00:00:00Z", "2024-06-30T23:59:59Z")));
    when(courseAssignmentRepository.findByGroupIdInAndSemesterAndAcademicYear(
            List.of(group.getId()), semester, academicYear))
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

    Group group = groupWith(UUID.randomUUID());
    Course courseA = courseWith(UUID.randomUUID(), 4);

    when(studentGroupHistoryRepository.findByStudentId(studentId))
        .thenReturn(List.of(historyFor(group, "2024-01-01T00:00:00Z", "2024-06-30T23:59:59Z")));
    when(courseAssignmentRepository.findByGroupIdInAndSemesterAndAcademicYear(
            List.of(group.getId()), semester, academicYear))
        .thenReturn(List.of(assignmentFor(courseA), assignmentFor(courseA)));
    when(courseAverageService.calculateCourseAverage(studentId, courseA.getId()))
        .thenReturn(BigDecimal.valueOf(12));

    SemesterAverageService.SemesterAverage result =
        semesterAverageService.calculate(studentId, semester, academicYear);

    assertEquals(new BigDecimal("12.00"), result.average());
    assertEquals(BigDecimal.valueOf(4), result.totalCredits());

    verify(courseAverageService, times(1)).calculateCourseAverage(studentId, courseA.getId());
  }

  @Test
  void calculate_shouldUseOnlyCoursesOfStudentGroup() {
    UUID studentId = UUID.randomUUID();
    Semester semester = Semester.S1;
    Integer academicYear = 2024;

    Group studentGroup = groupWith(UUID.randomUUID());
    Course courseA = courseWith(UUID.randomUUID(), 4);

    when(studentGroupHistoryRepository.findByStudentId(studentId))
        .thenReturn(
            List.of(historyFor(studentGroup, "2024-01-01T00:00:00Z", "2024-06-30T23:59:59Z")));
    when(courseAssignmentRepository.findByGroupIdInAndSemesterAndAcademicYear(
            List.of(studentGroup.getId()), semester, academicYear))
        .thenReturn(List.of(assignmentFor(courseA)));
    when(courseAverageService.calculateCourseAverage(studentId, courseA.getId()))
        .thenReturn(BigDecimal.valueOf(12));

    SemesterAverageService.SemesterAverage result =
        semesterAverageService.calculate(studentId, semester, academicYear);

    assertEquals(new BigDecimal("12.00"), result.average());
    assertEquals(BigDecimal.valueOf(4), result.totalCredits());

    verify(courseAssignmentRepository)
        .findByGroupIdInAndSemesterAndAcademicYear(
            List.of(studentGroup.getId()), semester, academicYear);
  }

  @Test
  void calculate_shouldUseCurrentGroupAfterGroupChange() {
    UUID studentId = UUID.randomUUID();
    Integer academicYear = 2024;

    Group firstGroup = groupWith(UUID.randomUUID());
    Group secondGroup = groupWith(UUID.randomUUID());
    Course firstCourse = courseWith(UUID.randomUUID(), 4);
    Course secondCourse = courseWith(UUID.randomUUID(), 4);

    when(studentGroupHistoryRepository.findByStudentId(studentId))
        .thenReturn(
            List.of(
                historyFor(firstGroup, "2024-01-01T00:00:00Z", "2024-06-30T23:59:59Z"),
                historyFor(secondGroup, "2024-07-01T00:00:00Z", null)));
    when(courseAssignmentRepository.findByGroupIdInAndSemesterAndAcademicYear(
            List.of(firstGroup.getId()), Semester.S1, academicYear))
        .thenReturn(List.of(assignmentFor(firstCourse)));
    when(courseAssignmentRepository.findByGroupIdInAndSemesterAndAcademicYear(
            List.of(secondGroup.getId()), Semester.S2, academicYear))
        .thenReturn(List.of(assignmentFor(secondCourse)));
    when(courseAverageService.calculateCourseAverage(studentId, firstCourse.getId()))
        .thenReturn(BigDecimal.valueOf(12));
    when(courseAverageService.calculateCourseAverage(studentId, secondCourse.getId()))
        .thenReturn(BigDecimal.valueOf(14));

    SemesterAverageService.SemesterAverage firstSemester =
        semesterAverageService.calculate(studentId, Semester.S1, academicYear);
    SemesterAverageService.SemesterAverage secondSemester =
        semesterAverageService.calculate(studentId, Semester.S2, academicYear);

    assertEquals(new BigDecimal("12.00"), firstSemester.average());
    assertEquals(new BigDecimal("14.00"), secondSemester.average());

    verify(courseAssignmentRepository)
        .findByGroupIdInAndSemesterAndAcademicYear(
            List.of(firstGroup.getId()), Semester.S1, academicYear);
    verify(courseAssignmentRepository)
        .findByGroupIdInAndSemesterAndAcademicYear(
            List.of(secondGroup.getId()), Semester.S2, academicYear);
  }

  @Test
  void calculate_shouldThrowWhenNoGroupHistory() {
    UUID studentId = UUID.randomUUID();

    when(studentGroupHistoryRepository.findByStudentId(studentId)).thenReturn(List.of());

    assertThrows(
        BusinessException.class,
        () -> semesterAverageService.calculate(studentId, Semester.S1, 2024));
  }

  @Test
  void calculate_shouldThrowWhenNoGroupForSemester() {
    UUID studentId = UUID.randomUUID();
    Group group = groupWith(UUID.randomUUID());

    when(studentGroupHistoryRepository.findByStudentId(studentId))
        .thenReturn(List.of(historyFor(group, "2024-01-01T00:00:00Z", "2024-06-30T23:59:59Z")));

    assertThrows(
        BusinessException.class,
        () -> semesterAverageService.calculate(studentId, Semester.S2, 2024));
  }

  @Test
  void calculate_shouldThrowWhenNoCoursesAreAvailable() {
    UUID studentId = UUID.randomUUID();
    Semester semester = Semester.S1;
    Integer academicYear = 2024;

    Group group = groupWith(UUID.randomUUID());

    when(studentGroupHistoryRepository.findByStudentId(studentId))
        .thenReturn(List.of(historyFor(group, "2024-01-01T00:00:00Z", "2024-06-30T23:59:59Z")));
    when(courseAssignmentRepository.findByGroupIdInAndSemesterAndAcademicYear(
            List.of(group.getId()), semester, academicYear))
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

    Group group = groupWith(UUID.randomUUID());
    Course courseA = courseWith(UUID.randomUUID(), 6);

    when(studentGroupHistoryRepository.findByStudentId(studentId))
        .thenReturn(List.of(historyFor(group, "2024-01-01T00:00:00Z", "2024-06-30T23:59:59Z")));
    when(courseAssignmentRepository.findByGroupIdInAndSemesterAndAcademicYear(
            List.of(group.getId()), semester, academicYear))
        .thenReturn(List.of(assignmentFor(courseA)));
    when(courseAverageService.calculateCourseAverage(studentId, courseA.getId()))
        .thenThrow(new BusinessException("No grades available"));

    assertThrows(
        BusinessException.class,
        () -> semesterAverageService.calculate(studentId, semester, academicYear));
  }

  private Group groupWith(UUID id) {
    Group group = new Group();
    group.setId(id);
    return group;
  }

  private Course courseWith(UUID id, Integer credits) {
    Course course = new Course();
    course.setId(id);
    course.setCredits(credits);
    return course;
  }

  private StudentGroupHistory historyFor(Group group, String start, String end) {
    StudentGroupHistory history = new StudentGroupHistory();
    history.setGroup(group);
    history.setStartDate(Instant.parse(start));
    history.setEndDate(end == null ? null : Instant.parse(end));
    return history;
  }

  private CourseAssignment assignmentFor(Course course) {
    CourseAssignment assignment = new CourseAssignment();
    assignment.setCourse(course);
    return assignment;
  }
}
