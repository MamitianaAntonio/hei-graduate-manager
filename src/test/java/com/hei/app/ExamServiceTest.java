package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.app.dto.exam.ExamRequest;
import com.hei.app.dto.exam.ExamResponse;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.mapper.ExamMapper;
import com.hei.app.model.Course;
import com.hei.app.model.Exam;
import com.hei.app.model.Role;
import com.hei.app.repository.CourseRepository;
import com.hei.app.repository.ExamRepository;
import com.hei.app.security.CurrentUser;
import com.hei.app.service.ExamService;
import com.hei.app.service.SecurityAsserts;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExamServiceTest {
  @Mock private ExamRepository examRepository;

  @Mock private ExamMapper examMapper;

  @Mock private CourseRepository courseRepository;

  @Mock private SecurityAsserts securityAsserts;

  @InjectMocks private ExamService examService;

  @Test
  void create_shouldReturnExam() {
    ExamRequest request = mock(ExamRequest.class);
    Course course = new Course();
    Exam exam = new Exam();
    Exam savedExam = new Exam();
    ExamResponse response = mock(ExamResponse.class);

    when(request.courseId()).thenReturn(UUID.randomUUID());
    when(courseRepository.findById(request.courseId())).thenReturn(Optional.of(course));
    when(examMapper.toEntity(request)).thenReturn(exam);
    when(examRepository.save(exam)).thenReturn(savedExam);
    when(examMapper.toResponse(savedExam)).thenReturn(response);

    ExamResponse result = examService.create(request, admin());

    assertEquals(response, result);
  }

  @Test
  void create_shouldThrowWhenCourseDoesNotExist() {
    ExamRequest request = mock(ExamRequest.class);

    when(request.courseId()).thenReturn(UUID.randomUUID());
    when(courseRepository.findById(request.courseId())).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> examService.create(request, admin()));
  }

  @Test
  void student_cannotCreateExam() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> examService.create(mock(ExamRequest.class), student(UUID.randomUUID())));
  }

  @Test
  void teacher_canCreateExamForAssignedCourse() {
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    CurrentUser currentUser = teacher(teacherId);
    ExamRequest request = mock(ExamRequest.class);
    Course course = courseWith(courseId);
    Exam exam = new Exam();
    Exam savedExam = new Exam();
    ExamResponse response = mock(ExamResponse.class);

    when(securityAsserts.requireTeacherId(currentUser)).thenReturn(teacherId);
    when(request.courseId()).thenReturn(courseId);
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
    when(examMapper.toEntity(request)).thenReturn(exam);
    when(examRepository.save(exam)).thenReturn(savedExam);
    when(examMapper.toResponse(savedExam)).thenReturn(response);

    ExamResponse result = examService.create(request, currentUser);

    assertEquals(response, result);
    verify(securityAsserts).assertTeacherAssignedToCourse(teacherId, courseId);
  }

  @Test
  void teacher_cannotCreateExamForUnassignedCourse() {
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    CurrentUser currentUser = teacher(teacherId);
    ExamRequest request = mock(ExamRequest.class);

    when(securityAsserts.requireTeacherId(currentUser)).thenReturn(teacherId);
    when(request.courseId()).thenReturn(courseId);
    org.mockito.Mockito.doThrow(
            new UnauthorizedActionException("Teacher is not assigned to this course"))
        .when(securityAsserts)
        .assertTeacherAssignedToCourse(teacherId, courseId);

    assertThrows(UnauthorizedActionException.class, () -> examService.create(request, currentUser));
  }

  @Test
  void findById_shouldReturnExam() {
    UUID id = UUID.randomUUID();
    Exam exam = new Exam();
    ExamResponse response = mock(ExamResponse.class);

    when(examRepository.findById(id)).thenReturn(Optional.of(exam));
    when(examMapper.toResponse(exam)).thenReturn(response);

    ExamResponse result = examService.findById(id, admin());

    assertEquals(response, result);
  }

  @Test
  void findById_shouldThrowWhenExamDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(examRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> examService.findById(id, admin()));
  }

  @Test
  void student_cannotReadExam() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> examService.findById(UUID.randomUUID(), student(UUID.randomUUID())));
  }

  @Test
  void teacher_canReadExamForAssignedCourse() {
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    CurrentUser currentUser = teacher(teacherId);
    UUID id = UUID.randomUUID();
    Exam exam = examOf(courseWith(courseId));
    ExamResponse response = mock(ExamResponse.class);

    when(securityAsserts.requireTeacherId(currentUser)).thenReturn(teacherId);
    when(examRepository.findById(id)).thenReturn(Optional.of(exam));
    when(examMapper.toResponse(exam)).thenReturn(response);

    ExamResponse result = examService.findById(id, currentUser);

    assertEquals(response, result);
    verify(securityAsserts).assertTeacherAssignedToCourse(teacherId, courseId);
  }

  @Test
  void teacher_cannotReadExamFromUnassignedCourse() {
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    CurrentUser currentUser = teacher(teacherId);
    UUID id = UUID.randomUUID();
    Exam exam = examOf(courseWith(courseId));

    when(securityAsserts.requireTeacherId(currentUser)).thenReturn(teacherId);
    when(examRepository.findById(id)).thenReturn(Optional.of(exam));
    org.mockito.Mockito.doThrow(
            new UnauthorizedActionException("Teacher is not assigned to this course"))
        .when(securityAsserts)
        .assertTeacherAssignedToCourse(teacherId, courseId);

    assertThrows(UnauthorizedActionException.class, () -> examService.findById(id, currentUser));
  }

  @Test
  void findAll_shouldReturnExams() {
    Exam exam1 = new Exam();
    exam1.setCoefficient(BigDecimal.ONE);
    Exam exam2 = new Exam();
    exam2.setCoefficient(BigDecimal.TWO);
    ExamResponse response1 = mock(ExamResponse.class);
    ExamResponse response2 = mock(ExamResponse.class);

    when(examRepository.findAll()).thenReturn(List.of(exam1, exam2));
    when(examMapper.toResponse(exam1)).thenReturn(response1);
    when(examMapper.toResponse(exam2)).thenReturn(response2);

    List<ExamResponse> result = examService.findAll(admin());

    assertEquals(List.of(response1, response2), result);
  }

  @Test
  void student_cannotListAllExams() {
    assertThrows(
        UnauthorizedActionException.class, () -> examService.findAll(student(UUID.randomUUID())));
  }

  @Test
  void teacher_findAll_shouldReturnOnlyAssignedCourseExams() {
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    UUID otherCourseId = UUID.randomUUID();
    CurrentUser currentUser = teacher(teacherId);
    Exam exam1 = examOf(courseWith(courseId));
    Exam exam2 = examOf(courseWith(otherCourseId));
    ExamResponse response1 = mock(ExamResponse.class);

    when(securityAsserts.requireTeacherId(currentUser)).thenReturn(teacherId);
    when(examRepository.findAll()).thenReturn(List.of(exam1, exam2));
    when(securityAsserts.isTeacherAssignedToCourse(teacherId, courseId)).thenReturn(true);
    when(securityAsserts.isTeacherAssignedToCourse(teacherId, otherCourseId)).thenReturn(false);
    when(examMapper.toResponse(exam1)).thenReturn(response1);

    List<ExamResponse> result = examService.findAll(currentUser);

    assertEquals(List.of(response1), result);
    verify(examMapper, never()).toResponse(exam2);
  }

  @Test
  void findByCourseId_shouldReturnExams() {
    UUID courseId = UUID.randomUUID();
    Exam exam = new Exam();
    ExamResponse response = mock(ExamResponse.class);

    when(examRepository.findByCourseId(courseId)).thenReturn(List.of(exam));
    when(examMapper.toResponse(exam)).thenReturn(response);

    List<ExamResponse> result = examService.findByCourseId(courseId, admin());

    assertEquals(List.of(response), result);
  }

  @Test
  void student_cannotListExamsByCourse() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> examService.findByCourseId(UUID.randomUUID(), student(UUID.randomUUID())));
  }

  @Test
  void teacher_canListExamsForAssignedCourse() {
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    CurrentUser currentUser = teacher(teacherId);
    Exam exam = new Exam();
    ExamResponse response = mock(ExamResponse.class);

    when(securityAsserts.requireTeacherId(currentUser)).thenReturn(teacherId);
    when(examRepository.findByCourseId(courseId)).thenReturn(List.of(exam));
    when(examMapper.toResponse(exam)).thenReturn(response);

    List<ExamResponse> result = examService.findByCourseId(courseId, currentUser);

    assertEquals(List.of(response), result);
    verify(securityAsserts).assertTeacherAssignedToCourse(teacherId, courseId);
  }

  @Test
  void update_shouldReturnUpdatedExam() {
    UUID id = UUID.randomUUID();
    ExamRequest request = mock(ExamRequest.class);
    Course course = new Course();
    Exam exam = new Exam();
    Exam updatedExam = new Exam();
    ExamResponse response = mock(ExamResponse.class);

    when(request.courseId()).thenReturn(UUID.randomUUID());
    when(courseRepository.findById(request.courseId())).thenReturn(Optional.of(course));
    when(examRepository.findById(id)).thenReturn(Optional.of(exam));
    when(examRepository.save(exam)).thenReturn(updatedExam);
    when(examMapper.toResponse(updatedExam)).thenReturn(response);

    ExamResponse result = examService.update(id, request, admin());

    assertEquals(response, result);
  }

  @Test
  void update_shouldThrowWhenExamDoesNotExist() {
    UUID id = UUID.randomUUID();
    ExamRequest request = mock(ExamRequest.class);

    when(examRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> examService.update(id, request, admin()));
  }

  @Test
  void student_cannotUpdateExam() {
    assertThrows(
        UnauthorizedActionException.class,
        () ->
            examService.update(
                UUID.randomUUID(), mock(ExamRequest.class), student(UUID.randomUUID())));
  }

  @Test
  void teacher_canUpdateExamForAssignedCourse() {
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    CurrentUser currentUser = teacher(teacherId);
    UUID id = UUID.randomUUID();
    ExamRequest request = mock(ExamRequest.class);
    Course course = courseWith(courseId);
    Exam exam = examOf(courseWith(courseId));
    Exam updatedExam = new Exam();
    ExamResponse response = mock(ExamResponse.class);

    when(securityAsserts.requireTeacherId(currentUser)).thenReturn(teacherId);
    when(request.courseId()).thenReturn(courseId);
    when(examRepository.findById(id)).thenReturn(Optional.of(exam));
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
    when(examRepository.save(exam)).thenReturn(updatedExam);
    when(examMapper.toResponse(updatedExam)).thenReturn(response);

    ExamResponse result = examService.update(id, request, currentUser);

    assertEquals(response, result);
    verify(securityAsserts, org.mockito.Mockito.times(2))
        .assertTeacherAssignedToCourse(teacherId, courseId);
  }

  @Test
  void teacher_cannotUpdateExamForUnassignedCourse() {
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    CurrentUser currentUser = teacher(teacherId);
    UUID id = UUID.randomUUID();
    ExamRequest request = mock(ExamRequest.class);
    Exam exam = examOf(courseWith(courseId));

    when(securityAsserts.requireTeacherId(currentUser)).thenReturn(teacherId);
    when(examRepository.findById(id)).thenReturn(Optional.of(exam));
    org.mockito.Mockito.doThrow(
            new UnauthorizedActionException("Teacher is not assigned to this course"))
        .when(securityAsserts)
        .assertTeacherAssignedToCourse(teacherId, courseId);

    assertThrows(
        UnauthorizedActionException.class, () -> examService.update(id, request, currentUser));
    verify(examRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void delete_shouldDeleteExam() {
    UUID id = UUID.randomUUID();
    Exam exam = examOf(courseWith(UUID.randomUUID()));

    when(examRepository.findById(id)).thenReturn(Optional.of(exam));

    examService.delete(id, admin());

    verify(examRepository).deleteById(id);
  }

  @Test
  void delete_shouldThrowWhenExamDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(examRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> examService.delete(id, admin()));
  }

  @Test
  void student_cannotDeleteExam() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> examService.delete(UUID.randomUUID(), student(UUID.randomUUID())));
  }

  @Test
  void teacher_canDeleteExamForAssignedCourse() {
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    CurrentUser currentUser = teacher(teacherId);
    UUID id = UUID.randomUUID();
    Exam exam = examOf(courseWith(courseId));

    when(securityAsserts.requireTeacherId(currentUser)).thenReturn(teacherId);
    when(examRepository.findById(id)).thenReturn(Optional.of(exam));

    examService.delete(id, currentUser);

    verify(securityAsserts).assertTeacherAssignedToCourse(teacherId, courseId);
    verify(examRepository).deleteById(id);
  }

  @Test
  void teacher_cannotDeleteExamForUnassignedCourse() {
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    CurrentUser currentUser = teacher(teacherId);
    UUID id = UUID.randomUUID();
    Exam exam = examOf(courseWith(courseId));

    when(securityAsserts.requireTeacherId(currentUser)).thenReturn(teacherId);
    when(examRepository.findById(id)).thenReturn(Optional.of(exam));
    org.mockito.Mockito.doThrow(
            new UnauthorizedActionException("Teacher is not assigned to this course"))
        .when(securityAsserts)
        .assertTeacherAssignedToCourse(teacherId, courseId);

    assertThrows(UnauthorizedActionException.class, () -> examService.delete(id, currentUser));
    verify(examRepository, never()).deleteById(org.mockito.ArgumentMatchers.any());
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
