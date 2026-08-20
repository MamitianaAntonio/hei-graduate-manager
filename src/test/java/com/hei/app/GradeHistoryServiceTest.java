package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.app.dto.gradeHistory.GradeHistoryResponse;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.mapper.GradeHistoryMapper;
import com.hei.app.model.Course;
import com.hei.app.model.Exam;
import com.hei.app.model.Grade;
import com.hei.app.model.GradeHistory;
import com.hei.app.model.Role;
import com.hei.app.model.Student;
import com.hei.app.model.UserAccount;
import com.hei.app.repository.GradeHistoryRepository;
import com.hei.app.repository.GradeRepository;
import com.hei.app.security.CurrentUser;
import com.hei.app.service.GradeHistoryService;
import com.hei.app.service.SecurityAsserts;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GradeHistoryServiceTest {
  @Mock private GradeHistoryRepository gradeHistoryRepository;

  @Mock private GradeHistoryMapper gradeHistoryMapper;

  @Mock private GradeRepository gradeRepository;

  @Mock private SecurityAsserts securityAsserts;

  @InjectMocks private GradeHistoryService gradeHistoryService;

  @Test
  void admin_canReadAnyGradeHistory() {
    UUID gradeId = UUID.randomUUID();
    Grade grade = gradeOf(studentWith(UUID.randomUUID()), examOf(courseWith(UUID.randomUUID())));
    GradeHistory history = historyFor(grade);
    GradeHistoryResponse response = mockResponse(history);

    when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));
    when(gradeHistoryRepository.findByGradeIdOrderByModifiedAtDesc(gradeId))
        .thenReturn(List.of(history));
    when(gradeHistoryMapper.toResponse(history)).thenReturn(response);

    List<GradeHistoryResponse> result = gradeHistoryService.findByGradeId(gradeId, admin());

    assertEquals(List.of(response), result);
    verify(securityAsserts, never()).requireStudentId(any());
    verify(securityAsserts, never()).requireTeacherId(any());
  }

  @Test
  void student_canReadOwnGradeHistory() {
    UUID gradeId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    Grade grade = gradeOf(studentWith(studentId), examOf(courseWith(UUID.randomUUID())));
    GradeHistory history = historyFor(grade);
    GradeHistoryResponse response = mockResponse(history);

    when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));
    when(securityAsserts.requireStudentId(any())).thenReturn(studentId);
    when(gradeHistoryRepository.findByGradeIdOrderByModifiedAtDesc(gradeId))
        .thenReturn(List.of(history));
    when(gradeHistoryMapper.toResponse(history)).thenReturn(response);

    List<GradeHistoryResponse> result =
        gradeHistoryService.findByGradeId(gradeId, student(studentId));

    assertEquals(List.of(response), result);
  }

  @Test
  void student_cannotReadAnotherStudentGradeHistory() {
    UUID gradeId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    Grade grade = gradeOf(studentWith(UUID.randomUUID()), examOf(courseWith(UUID.randomUUID())));
    GradeHistory history = historyFor(grade);

    when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));
    when(securityAsserts.requireStudentId(any())).thenReturn(studentId);

    assertThrows(
        UnauthorizedActionException.class,
        () -> gradeHistoryService.findByGradeId(gradeId, student(studentId)));

    verify(gradeHistoryRepository, never()).findByGradeIdOrderByModifiedAtDesc(gradeId);
  }

  @Test
  void teacher_canReadHistoryForAssignedCourse() {
    UUID gradeId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    Grade grade = gradeOf(studentWith(UUID.randomUUID()), examOf(courseWith(courseId)));
    GradeHistory history = historyFor(grade);
    GradeHistoryResponse response = mockResponse(history);

    when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));
    when(securityAsserts.requireTeacherId(any())).thenReturn(teacherId);
    when(gradeHistoryRepository.findByGradeIdOrderByModifiedAtDesc(gradeId))
        .thenReturn(List.of(history));
    when(gradeHistoryMapper.toResponse(history)).thenReturn(response);

    List<GradeHistoryResponse> result =
        gradeHistoryService.findByGradeId(gradeId, teacher(teacherId));

    assertEquals(List.of(response), result);
    verify(securityAsserts).assertTeacherAssignedToCourse(teacherId, courseId);
  }

  @Test
  void teacher_cannotReadHistoryForUnassignedCourse() {
    UUID gradeId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    Grade grade = gradeOf(studentWith(UUID.randomUUID()), examOf(courseWith(courseId)));

    when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));
    when(securityAsserts.requireTeacherId(any())).thenReturn(teacherId);
    org.mockito.Mockito.doThrow(
            new UnauthorizedActionException("Teacher is not assigned to this course"))
        .when(securityAsserts)
        .assertTeacherAssignedToCourse(teacherId, courseId);

    assertThrows(
        UnauthorizedActionException.class,
        () -> gradeHistoryService.findByGradeId(gradeId, teacher(teacherId)));

    verify(gradeHistoryRepository, never()).findByGradeIdOrderByModifiedAtDesc(gradeId);
  }

  @Test
  void findByGradeId_shouldThrowWhenGradeDoesNotExist() {
    UUID gradeId = UUID.randomUUID();

    when(gradeRepository.findById(gradeId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> gradeHistoryService.findByGradeId(gradeId, admin()));
  }

  @Test
  void findByGradeId_shouldReturnEmptyListWhenNoHistory() {
    UUID gradeId = UUID.randomUUID();
    Grade grade = gradeOf(studentWith(UUID.randomUUID()), examOf(courseWith(UUID.randomUUID())));

    when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));
    when(gradeHistoryRepository.findByGradeIdOrderByModifiedAtDesc(gradeId)).thenReturn(List.of());

    List<GradeHistoryResponse> result = gradeHistoryService.findByGradeId(gradeId, admin());

    assertEquals(0, result.size());
  }

  private CurrentUser admin() {
    return new CurrentUser(UUID.randomUUID(), Role.ADMIN, null, null);
  }

  private CurrentUser student(UUID studentId) {
    return new CurrentUser(UUID.randomUUID(), Role.STUDENT, studentId, null);
  }

  private CurrentUser teacher(UUID teacherId) {
    return new CurrentUser(UUID.randomUUID(), Role.TEACHER, null, teacherId);
  }

  private Grade gradeOf(Student student, Exam exam) {
    Grade grade = new Grade();
    grade.setStudent(student);
    grade.setExam(exam);
    return grade;
  }

  private GradeHistory historyFor(Grade grade) {
    UserAccount userAccount = new UserAccount();
    GradeHistory history = new GradeHistory();
    history.setGrade(grade);
    history.setOldValue(BigDecimal.TEN);
    history.setNewValue(BigDecimal.valueOf(15));
    history.setReason("Correction");
    history.setModifiedBy(userAccount);
    history.setModifiedAt(Instant.now());
    return history;
  }

  private GradeHistoryResponse mockResponse(GradeHistory history) {
    return new GradeHistoryResponse(
        UUID.randomUUID(),
        history.getGrade().getId(),
        history.getOldValue(),
        history.getNewValue(),
        history.getReason(),
        history.getModifiedBy().getId(),
        history.getModifiedAt());
  }

  private Student studentWith(UUID id) {
    Student student = new Student();
    student.setId(id);
    return student;
  }

  private Exam examOf(Course course) {
    Exam exam = new Exam();
    exam.setCourse(course);
    return exam;
  }

  private Course courseWith(UUID id) {
    Course course = new Course();
    course.setId(id);
    return course;
  }
}
