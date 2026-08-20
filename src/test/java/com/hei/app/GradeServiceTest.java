package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hei.app.dto.grade.GradeRequest;
import com.hei.app.dto.grade.GradeResponse;
import com.hei.app.dto.grade.GradeUpdateRequest;
import com.hei.app.exceptions.DuplicateResourceException;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.mapper.GradeHistoryMapper;
import com.hei.app.mapper.GradeMapper;
import com.hei.app.model.Course;
import com.hei.app.model.Exam;
import com.hei.app.model.Grade;
import com.hei.app.model.GradeHistory;
import com.hei.app.model.Role;
import com.hei.app.model.Student;
import com.hei.app.model.UserAccount;
import com.hei.app.repository.CourseAssignmentRepository;
import com.hei.app.repository.ExamRepository;
import com.hei.app.repository.GradeHistoryRepository;
import com.hei.app.repository.GradeRepository;
import com.hei.app.repository.StudentRepository;
import com.hei.app.repository.UserAccountRepository;
import com.hei.app.security.CurrentUser;
import com.hei.app.service.GradeService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GradeServiceTest {
  @Mock private GradeRepository gradeRepository;

  @Mock private GradeMapper gradeMapper;

  @Mock private StudentRepository studentRepository;

  @Mock private ExamRepository examRepository;

  @Mock private CourseAssignmentRepository courseAssignmentRepository;

  @Mock private GradeHistoryRepository gradeHistoryRepository;

  @Mock private GradeHistoryMapper gradeHistoryMapper;

  @Mock private UserAccountRepository userAccountRepository;

  @InjectMocks private GradeService gradeService;

  @Test
  void admin_canCreateGrade() {
    GradeRequest request = mock(GradeRequest.class);
    UUID studentId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    Student student = new Student();
    Exam exam = new Exam();
    Grade grade = new Grade();
    Grade savedGrade = new Grade();
    GradeResponse response = mock(GradeResponse.class);

    when(request.studentId()).thenReturn(studentId);
    when(request.examId()).thenReturn(examId);
    when(gradeRepository.existsByStudentIdAndExamId(studentId, examId)).thenReturn(false);
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(gradeMapper.toEntity(request)).thenReturn(grade);
    when(gradeRepository.save(grade)).thenReturn(savedGrade);
    when(gradeMapper.toResponse(savedGrade)).thenReturn(response);

    GradeResponse result = gradeService.create(request, admin());

    assertEquals(response, result);
    verifyNoInteractions(courseAssignmentRepository);
  }

  @Test
  void create_shouldThrowWhenGradeAlreadyExists() {
    GradeRequest request = mock(GradeRequest.class);
    UUID studentId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();

    when(request.studentId()).thenReturn(studentId);
    when(request.examId()).thenReturn(examId);
    when(gradeRepository.existsByStudentIdAndExamId(studentId, examId)).thenReturn(true);

    assertThrows(DuplicateResourceException.class, () -> gradeService.create(request, admin()));
  }

  @Test
  void admin_canAccessAnyGrade() {
    UUID id = UUID.randomUUID();
    Grade grade = gradeOf(studentWith(UUID.randomUUID()), examOf(courseWith(UUID.randomUUID())));
    GradeResponse response = mock(GradeResponse.class);

    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));
    when(gradeMapper.toResponse(grade)).thenReturn(response);

    GradeResponse result = gradeService.findById(id, admin());

    assertEquals(response, result);
    verifyNoInteractions(courseAssignmentRepository);
  }

  @Test
  void findById_shouldThrowWhenGradeDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(gradeRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> gradeService.findById(id, admin()));
  }

  @Test
  void findAll_shouldReturnGrades() {
    Grade grade1 = new Grade();
    grade1.setValue(BigDecimal.TEN);
    Grade grade2 = new Grade();
    grade2.setValue(BigDecimal.ONE);
    GradeResponse response1 = mock(GradeResponse.class);
    GradeResponse response2 = mock(GradeResponse.class);

    when(gradeRepository.findAll()).thenReturn(List.of(grade1, grade2));
    when(gradeMapper.toResponse(grade1)).thenReturn(response1);
    when(gradeMapper.toResponse(grade2)).thenReturn(response2);

    List<GradeResponse> result = gradeService.findAll(admin());

    assertEquals(List.of(response1, response2), result);
  }

  @Test
  void findByStudentId_shouldReturnGrades() {
    UUID studentId = UUID.randomUUID();
    Grade grade = new Grade();
    GradeResponse response = mock(GradeResponse.class);

    when(gradeRepository.findByStudentId(studentId)).thenReturn(List.of(grade));
    when(gradeMapper.toResponse(grade)).thenReturn(response);

    List<GradeResponse> result = gradeService.findByStudentId(studentId, admin());

    assertEquals(List.of(response), result);
  }

  @Test
  void findByExamId_shouldReturnGrades() {
    UUID examId = UUID.randomUUID();
    Grade grade = new Grade();
    GradeResponse response = mock(GradeResponse.class);

    when(gradeRepository.findByExamId(examId)).thenReturn(List.of(grade));
    when(gradeMapper.toResponse(grade)).thenReturn(response);

    List<GradeResponse> result = gradeService.findByExamId(examId, admin());

    assertEquals(List.of(response), result);
  }

  @Test
  void findByStudentAndExam_shouldReturnGrade() {
    UUID studentId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    Grade grade = new Grade();
    GradeResponse response = mock(GradeResponse.class);

    when(gradeRepository.findByStudentIdAndExamId(studentId, examId))
        .thenReturn(Optional.of(grade));
    when(gradeMapper.toResponse(grade)).thenReturn(response);

    GradeResponse result = gradeService.findByStudentAndExam(studentId, examId, admin());

    assertEquals(response, result);
  }

  @Test
  void findByStudentAndExam_shouldThrowWhenGradeDoesNotExist() {
    UUID studentId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();

    when(gradeRepository.findByStudentIdAndExamId(studentId, examId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> gradeService.findByStudentAndExam(studentId, examId, admin()));
  }

  @Test
  void admin_canUpdateGrade() {
    UUID id = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    GradeUpdateRequest request = mock(GradeUpdateRequest.class);
    Grade grade = new Grade();
    grade.setValue(BigDecimal.TEN);
    Grade updatedGrade = new Grade();
    GradeResponse response = mock(GradeResponse.class);
    UserAccount userAccount = new UserAccount();

    when(request.value()).thenReturn(BigDecimal.valueOf(12.5));
    when(request.reason()).thenReturn("Correction");
    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));
    when(userAccountRepository.findById(accountId)).thenReturn(Optional.of(userAccount));
    when(gradeRepository.save(grade)).thenReturn(updatedGrade);
    when(gradeMapper.toResponse(updatedGrade)).thenReturn(response);

    GradeResponse result = gradeService.update(id, request, adminWith(accountId));

    assertEquals(response, result);
    verify(gradeHistoryRepository).save(any());

    ArgumentCaptor<GradeHistory> captor = ArgumentCaptor.forClass(GradeHistory.class);
    verify(gradeHistoryRepository).save(captor.capture());
    GradeHistory savedHistory = captor.getValue();
    assertEquals(BigDecimal.TEN, savedHistory.getOldValue());
    assertEquals(BigDecimal.valueOf(12.5), savedHistory.getNewValue());
    assertEquals("Correction", savedHistory.getReason());
    assertEquals(userAccount, savedHistory.getModifiedBy());
    assertNotNull(savedHistory.getModifiedAt());
  }

  @Test
  void update_shouldThrowWhenGradeDoesNotExist() {
    UUID id = UUID.randomUUID();
    GradeUpdateRequest request = mock(GradeUpdateRequest.class);

    when(gradeRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> gradeService.update(id, request, admin()));
  }

  @Test
  void admin_canDeleteGrade() {
    UUID id = UUID.randomUUID();
    Grade grade = gradeOf(studentWith(UUID.randomUUID()), examOf(courseWith(UUID.randomUUID())));

    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));

    gradeService.delete(id, admin());

    verify(gradeRepository).deleteById(id);
    verifyNoInteractions(courseAssignmentRepository);
  }

  @Test
  void delete_shouldThrowWhenGradeDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(gradeRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> gradeService.delete(id, admin()));
  }

  @Test
  void student_canReadOwnGrade() {
    UUID id = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    Grade grade = gradeOf(studentWith(studentId), examOf(courseWith(UUID.randomUUID())));
    GradeResponse response = mock(GradeResponse.class);

    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));
    when(gradeMapper.toResponse(grade)).thenReturn(response);

    GradeResponse result = gradeService.findById(id, student(studentId));

    assertEquals(response, result);
  }

  @Test
  void student_cannotReadAnotherStudentGrade() {
    UUID id = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    Grade grade = gradeOf(studentWith(UUID.randomUUID()), examOf(courseWith(UUID.randomUUID())));

    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));

    assertThrows(
        UnauthorizedActionException.class, () -> gradeService.findById(id, student(studentId)));
  }

  @Test
  void student_cannotCreateGrade() {
    GradeRequest request = mock(GradeRequest.class);

    assertThrows(
        UnauthorizedActionException.class,
        () -> gradeService.create(request, student(UUID.randomUUID())));
  }

  @Test
  void student_cannotUpdateGrade() {
    GradeUpdateRequest request = mock(GradeUpdateRequest.class);

    assertThrows(
        UnauthorizedActionException.class,
        () -> gradeService.update(UUID.randomUUID(), request, student(UUID.randomUUID())));
  }

  @Test
  void student_cannotDeleteGrade() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> gradeService.delete(UUID.randomUUID(), student(UUID.randomUUID())));
  }

  @Test
  void studentProfileMissing_shouldThrow() {
    UUID id = UUID.randomUUID();
    Grade grade = gradeOf(studentWith(UUID.randomUUID()), examOf(courseWith(UUID.randomUUID())));
    CurrentUser currentUser = new CurrentUser(UUID.randomUUID(), Role.STUDENT, null, null);

    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));

    assertThrows(UnauthorizedActionException.class, () -> gradeService.findById(id, currentUser));
  }

  @Test
  void teacher_canCreateGradeForAssignedCourse() {
    GradeRequest request = mock(GradeRequest.class);
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    Student student = studentWith(studentId);
    Exam exam = examOf(courseWith(courseId));
    Grade grade = new Grade();
    Grade savedGrade = new Grade();
    GradeResponse response = mock(GradeResponse.class);

    when(request.studentId()).thenReturn(studentId);
    when(request.examId()).thenReturn(examId);
    when(gradeRepository.existsByStudentIdAndExamId(studentId, examId)).thenReturn(false);
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(gradeMapper.toEntity(request)).thenReturn(grade);
    when(gradeRepository.save(grade)).thenReturn(savedGrade);
    when(gradeMapper.toResponse(savedGrade)).thenReturn(response);
    when(courseAssignmentRepository.existsByTeacherIdAndCourseId(teacherId, courseId))
        .thenReturn(true);

    GradeResponse result = gradeService.create(request, teacher(teacherId));

    assertEquals(response, result);
    verify(courseAssignmentRepository).existsByTeacherIdAndCourseId(teacherId, courseId);
  }

  @Test
  void teacher_canUpdateGradeForAssignedCourse() {
    UUID id = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    GradeUpdateRequest request = mock(GradeUpdateRequest.class);
    Grade grade = gradeOf(studentWith(UUID.randomUUID()), examOf(courseWith(courseId)));
    grade.setValue(BigDecimal.TEN);
    Grade updatedGrade = new Grade();
    GradeResponse response = mock(GradeResponse.class);
    UserAccount userAccount = new UserAccount();

    when(request.value()).thenReturn(BigDecimal.valueOf(12.5));
    when(request.reason()).thenReturn("Re-evaluation");
    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));
    when(courseAssignmentRepository.existsByTeacherIdAndCourseId(teacherId, courseId))
        .thenReturn(true);
    when(userAccountRepository.findById(accountId)).thenReturn(Optional.of(userAccount));
    when(gradeRepository.save(grade)).thenReturn(updatedGrade);
    when(gradeMapper.toResponse(updatedGrade)).thenReturn(response);

    GradeResponse result = gradeService.update(id, request, teacherWith(teacherId, accountId));

    assertEquals(response, result);
    verify(courseAssignmentRepository).existsByTeacherIdAndCourseId(teacherId, courseId);
    verify(gradeHistoryRepository).save(any());
  }

  @Test
  void teacher_canDeleteGradeForAssignedCourse() {
    UUID id = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    Grade grade = gradeOf(studentWith(UUID.randomUUID()), examOf(courseWith(courseId)));

    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));
    when(courseAssignmentRepository.existsByTeacherIdAndCourseId(teacherId, courseId))
        .thenReturn(true);

    gradeService.delete(id, teacher(teacherId));

    verify(gradeRepository).deleteById(id);
    verify(courseAssignmentRepository).existsByTeacherIdAndCourseId(teacherId, courseId);
  }

  @Test
  void teacher_cannotModifyGradeForUnassignedCourse() {
    UUID id = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    GradeUpdateRequest request = mock(GradeUpdateRequest.class);
    Grade grade = gradeOf(studentWith(UUID.randomUUID()), examOf(courseWith(courseId)));

    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));
    when(courseAssignmentRepository.existsByTeacherIdAndCourseId(teacherId, courseId))
        .thenReturn(false);

    assertThrows(
        UnauthorizedActionException.class,
        () -> gradeService.update(id, request, teacher(teacherId)));

    verify(gradeRepository, never()).save(any());
  }

  @Test
  void teacher_cannotReadGradeFromUnassignedCourse() {
    UUID id = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    Grade grade = gradeOf(studentWith(UUID.randomUUID()), examOf(courseWith(courseId)));

    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));
    when(courseAssignmentRepository.existsByTeacherIdAndCourseId(teacherId, courseId))
        .thenReturn(false);

    assertThrows(
        UnauthorizedActionException.class, () -> gradeService.findById(id, teacher(teacherId)));
  }

  @Test
  void teacher_findAll_shouldReturnOnlyAssignedCourseGrades() {
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    UUID otherCourseId = UUID.randomUUID();
    Grade grade1 = gradeOf(studentWith(UUID.randomUUID()), examOf(courseWith(courseId)));
    Grade grade2 = gradeOf(studentWith(UUID.randomUUID()), examOf(courseWith(otherCourseId)));
    GradeResponse response1 = mock(GradeResponse.class);

    when(gradeRepository.findAll()).thenReturn(List.of(grade1, grade2));
    when(courseAssignmentRepository.existsByTeacherIdAndCourseId(teacherId, courseId))
        .thenReturn(true);
    when(courseAssignmentRepository.existsByTeacherIdAndCourseId(teacherId, otherCourseId))
        .thenReturn(false);
    when(gradeMapper.toResponse(grade1)).thenReturn(response1);

    List<GradeResponse> result = gradeService.findAll(teacher(teacherId));

    assertEquals(List.of(response1), result);
    verify(gradeMapper, never()).toResponse(grade2);
  }

  @Test
  void teacherProfileMissing_shouldThrow() {
    UUID id = UUID.randomUUID();
    GradeUpdateRequest request = mock(GradeUpdateRequest.class);
    Grade grade = gradeOf(studentWith(UUID.randomUUID()), examOf(courseWith(UUID.randomUUID())));
    CurrentUser currentUser = new CurrentUser(UUID.randomUUID(), Role.TEACHER, null, null);

    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));

    assertThrows(
        UnauthorizedActionException.class, () -> gradeService.update(id, request, currentUser));
  }

  @Test
  void update_createsGradeHistoryWithCorrectFields() {
    UUID id = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    GradeUpdateRequest request = mock(GradeUpdateRequest.class);
    Grade grade = new Grade();
    grade.setValue(BigDecimal.valueOf(10));
    Grade updatedGrade = new Grade();
    GradeResponse response = mock(GradeResponse.class);
    UserAccount userAccount = new UserAccount();
    userAccount.setId(accountId);

    when(request.value()).thenReturn(BigDecimal.valueOf(15));
    when(request.reason()).thenReturn("Grade adjustment");
    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));
    when(userAccountRepository.findById(accountId)).thenReturn(Optional.of(userAccount));
    when(gradeRepository.save(grade)).thenReturn(updatedGrade);
    when(gradeMapper.toResponse(updatedGrade)).thenReturn(response);

    gradeService.update(id, request, adminWith(accountId));

    ArgumentCaptor<GradeHistory> captor = ArgumentCaptor.forClass(GradeHistory.class);
    verify(gradeHistoryRepository).save(captor.capture());

    GradeHistory history = captor.getValue();
    assertEquals(grade, history.getGrade());
    assertEquals(BigDecimal.valueOf(10), history.getOldValue());
    assertEquals(BigDecimal.valueOf(15), history.getNewValue());
    assertEquals("Grade adjustment", history.getReason());
    assertEquals(userAccount, history.getModifiedBy());
    assertNotNull(history.getModifiedAt());
  }

  @Test
  void update_doesNotCreateHistoryWhenStudentTriesToUpdate() {
    GradeUpdateRequest request = mock(GradeUpdateRequest.class);

    assertThrows(
        UnauthorizedActionException.class,
        () -> gradeService.update(UUID.randomUUID(), request, student(UUID.randomUUID())));

    verifyNoInteractions(gradeHistoryRepository);
  }

  private CurrentUser admin() {
    return new CurrentUser(UUID.randomUUID(), Role.ADMIN, null, null);
  }

  private CurrentUser adminWith(UUID accountId) {
    return new CurrentUser(accountId, Role.ADMIN, null, null);
  }

  private CurrentUser student(UUID studentId) {
    return new CurrentUser(UUID.randomUUID(), Role.STUDENT, studentId, null);
  }

  private CurrentUser teacher(UUID teacherId) {
    return new CurrentUser(UUID.randomUUID(), Role.TEACHER, null, teacherId);
  }

  private CurrentUser teacherWith(UUID teacherId, UUID accountId) {
    return new CurrentUser(accountId, Role.TEACHER, null, teacherId);
  }

  private Grade gradeOf(Student student, Exam exam) {
    Grade grade = new Grade();
    grade.setStudent(student);
    grade.setExam(exam);
    return grade;
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
