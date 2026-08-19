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
import com.hei.app.model.StudentGroupHistory;
import com.hei.app.repository.CourseAssignmentRepository;
import com.hei.app.repository.StudentGroupHistoryRepository;
import com.hei.app.service.CourseAverageService;
import com.hei.app.service.GraduationService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GraduationServiceTest {
  @Mock private StudentGroupHistoryRepository studentGroupHistoryRepository;

  @Mock private CourseAssignmentRepository courseAssignmentRepository;

  @Mock private CourseAverageService courseAverageService;

  @InjectMocks private GraduationService graduationService;

  @Test
  void isGraduable_shouldReturnTrueWhenAllCoursesValidated() {
    UUID studentId = UUID.randomUUID();

    Group group = groupWith(UUID.randomUUID());
    Course courseA = courseWith(UUID.randomUUID());
    Course courseB = courseWith(UUID.randomUUID());

    when(studentGroupHistoryRepository.findByStudentId(studentId))
        .thenReturn(List.of(historyFor(group)));
    when(courseAssignmentRepository.findByGroupId(group.getId()))
        .thenReturn(List.of(assignmentFor(courseA), assignmentFor(courseB)));
    when(courseAverageService.calculateCourseAverage(studentId, courseA.getId()))
        .thenReturn(BigDecimal.valueOf(14));
    when(courseAverageService.calculateCourseAverage(studentId, courseB.getId()))
        .thenReturn(BigDecimal.valueOf(15));

    boolean result = graduationService.isGraduable(studentId);

    assertEquals(true, result);

    verify(courseAverageService).calculateCourseAverage(studentId, courseA.getId());
    verify(courseAverageService).calculateCourseAverage(studentId, courseB.getId());
  }

  @Test
  void isGraduable_shouldReturnFalseWhenAtLeastOneCourseBelowTen() {
    UUID studentId = UUID.randomUUID();

    Group group = groupWith(UUID.randomUUID());
    Course courseA = courseWith(UUID.randomUUID());
    Course courseB = courseWith(UUID.randomUUID());

    when(studentGroupHistoryRepository.findByStudentId(studentId))
        .thenReturn(List.of(historyFor(group)));
    when(courseAssignmentRepository.findByGroupId(group.getId()))
        .thenReturn(List.of(assignmentFor(courseA), assignmentFor(courseB)));
    when(courseAverageService.calculateCourseAverage(studentId, courseA.getId()))
        .thenReturn(BigDecimal.valueOf(14));
    when(courseAverageService.calculateCourseAverage(studentId, courseB.getId()))
        .thenReturn(BigDecimal.valueOf(9));

    boolean result = graduationService.isGraduable(studentId);

    assertEquals(false, result);
  }

  @Test
  void isGraduable_shouldReturnFalseWhenACourseHasNoGrades() {
    UUID studentId = UUID.randomUUID();

    Group group = groupWith(UUID.randomUUID());
    Course courseA = courseWith(UUID.randomUUID());

    when(studentGroupHistoryRepository.findByStudentId(studentId))
        .thenReturn(List.of(historyFor(group)));
    when(courseAssignmentRepository.findByGroupId(group.getId()))
        .thenReturn(List.of(assignmentFor(courseA)));
    when(courseAverageService.calculateCourseAverage(studentId, courseA.getId()))
        .thenThrow(new BusinessException("No grades available"));

    boolean result = graduationService.isGraduable(studentId);

    assertEquals(false, result);
  }

  @Test
  void isGraduable_shouldDeduplicateCoursesFromMultipleGroups() {
    UUID studentId = UUID.randomUUID();

    Group group1 = groupWith(UUID.randomUUID());
    Group group2 = groupWith(UUID.randomUUID());
    Course courseA = courseWith(UUID.randomUUID());

    when(studentGroupHistoryRepository.findByStudentId(studentId))
        .thenReturn(List.of(historyFor(group1), historyFor(group2)));
    when(courseAssignmentRepository.findByGroupId(group1.getId()))
        .thenReturn(List.of(assignmentFor(courseA)));
    when(courseAssignmentRepository.findByGroupId(group2.getId()))
        .thenReturn(List.of(assignmentFor(courseA)));
    when(courseAverageService.calculateCourseAverage(studentId, courseA.getId()))
        .thenReturn(BigDecimal.valueOf(14));

    boolean result = graduationService.isGraduable(studentId);

    assertEquals(true, result);

    verify(courseAverageService, times(1)).calculateCourseAverage(studentId, courseA.getId());
  }

  @Test
  void isGraduable_shouldThrowWhenNoGroupHistory() {
    UUID studentId = UUID.randomUUID();

    when(studentGroupHistoryRepository.findByStudentId(studentId)).thenReturn(List.of());

    assertThrows(BusinessException.class, () -> graduationService.isGraduable(studentId));
  }

  private Group groupWith(UUID id) {
    Group group = new Group();
    group.setId(id);
    return group;
  }

  private Course courseWith(UUID id) {
    Course course = new Course();
    course.setId(id);
    return course;
  }

  private StudentGroupHistory historyFor(Group group) {
    StudentGroupHistory history = new StudentGroupHistory();
    history.setGroup(group);
    return history;
  }

  private CourseAssignment assignmentFor(Course course) {
    CourseAssignment assignment = new CourseAssignment();
    assignment.setCourse(course);
    return assignment;
  }
}
