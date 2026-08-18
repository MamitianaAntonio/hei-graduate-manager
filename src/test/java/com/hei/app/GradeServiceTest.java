package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.app.dto.grade.GradeRequest;
import com.hei.app.dto.grade.GradeResponse;
import com.hei.app.exceptions.DuplicateResourceException;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.mapper.GradeMapper;
import com.hei.app.model.Exam;
import com.hei.app.model.Grade;
import com.hei.app.model.Student;
import com.hei.app.repository.ExamRepository;
import com.hei.app.repository.GradeRepository;
import com.hei.app.repository.StudentRepository;
import com.hei.app.service.GradeService;
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
public class GradeServiceTest {
  @Mock private GradeRepository gradeRepository;

  @Mock private GradeMapper gradeMapper;

  @Mock private StudentRepository studentRepository;

  @Mock private ExamRepository examRepository;

  @InjectMocks private GradeService gradeService;

  @Test
  void create_shouldReturnGrade() {
    GradeRequest request = mock(GradeRequest.class);
    Student student = new Student();
    Exam exam = new Exam();
    Grade grade = new Grade();
    Grade savedGrade = new Grade();
    GradeResponse response = mock(GradeResponse.class);

    when(request.studentId()).thenReturn(UUID.randomUUID());
    when(request.examId()).thenReturn(UUID.randomUUID());
    when(gradeRepository.existsByStudentIdAndExamId(request.studentId(), request.examId()))
        .thenReturn(false);
    when(studentRepository.findById(request.studentId())).thenReturn(Optional.of(student));
    when(examRepository.findById(request.examId())).thenReturn(Optional.of(exam));
    when(gradeMapper.toEntity(request)).thenReturn(grade);
    when(gradeRepository.save(grade)).thenReturn(savedGrade);
    when(gradeMapper.toResponse(savedGrade)).thenReturn(response);

    GradeResponse result = gradeService.create(request);

    assertEquals(response, result);

    verify(gradeRepository).existsByStudentIdAndExamId(request.studentId(), request.examId());
    verify(studentRepository).findById(request.studentId());
    verify(examRepository).findById(request.examId());
    verify(gradeMapper).toEntity(request);
    verify(gradeRepository).save(grade);
    verify(gradeMapper).toResponse(savedGrade);
  }

  @Test
  void create_shouldThrowWhenGradeAlreadyExists() {
    GradeRequest request = mock(GradeRequest.class);

    when(request.studentId()).thenReturn(UUID.randomUUID());
    when(request.examId()).thenReturn(UUID.randomUUID());
    when(gradeRepository.existsByStudentIdAndExamId(request.studentId(), request.examId()))
        .thenReturn(true);

    assertThrows(DuplicateResourceException.class, () -> gradeService.create(request));

    verify(gradeRepository).existsByStudentIdAndExamId(request.studentId(), request.examId());
  }

  @Test
  void findById_shouldReturnGrade() {
    UUID id = UUID.randomUUID();
    Grade grade = new Grade();
    GradeResponse response = mock(GradeResponse.class);

    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));
    when(gradeMapper.toResponse(grade)).thenReturn(response);

    GradeResponse result = gradeService.findById(id);

    assertEquals(response, result);

    verify(gradeRepository).findById(id);
    verify(gradeMapper).toResponse(grade);
  }

  @Test
  void findById_shouldThrowWhenGradeDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(gradeRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> gradeService.findById(id));
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

    List<GradeResponse> result = gradeService.findAll();

    assertEquals(List.of(response1, response2), result);

    verify(gradeRepository).findAll();
    verify(gradeMapper).toResponse(grade1);
    verify(gradeMapper).toResponse(grade2);
  }

  @Test
  void findByStudentId_shouldReturnGrades() {
    UUID studentId = UUID.randomUUID();
    Grade grade = new Grade();
    GradeResponse response = mock(GradeResponse.class);

    when(gradeRepository.findByStudentId(studentId)).thenReturn(List.of(grade));
    when(gradeMapper.toResponse(grade)).thenReturn(response);

    List<GradeResponse> result = gradeService.findByStudentId(studentId);

    assertEquals(List.of(response), result);

    verify(gradeRepository).findByStudentId(studentId);
    verify(gradeMapper).toResponse(grade);
  }

  @Test
  void findByExamId_shouldReturnGrades() {
    UUID examId = UUID.randomUUID();
    Grade grade = new Grade();
    GradeResponse response = mock(GradeResponse.class);

    when(gradeRepository.findByExamId(examId)).thenReturn(List.of(grade));
    when(gradeMapper.toResponse(grade)).thenReturn(response);

    List<GradeResponse> result = gradeService.findByExamId(examId);

    assertEquals(List.of(response), result);

    verify(gradeRepository).findByExamId(examId);
    verify(gradeMapper).toResponse(grade);
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

    GradeResponse result = gradeService.findByStudentAndExam(studentId, examId);

    assertEquals(response, result);

    verify(gradeRepository).findByStudentIdAndExamId(studentId, examId);
    verify(gradeMapper).toResponse(grade);
  }

  @Test
  void findByStudentAndExam_shouldThrowWhenGradeDoesNotExist() {
    UUID studentId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();

    when(gradeRepository.findByStudentIdAndExamId(studentId, examId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> gradeService.findByStudentAndExam(studentId, examId));
  }

  @Test
  void update_shouldReturnUpdatedGrade() {
    UUID id = UUID.randomUUID();
    GradeRequest request = mock(GradeRequest.class);
    Grade grade = new Grade();
    Grade updatedGrade = new Grade();
    GradeResponse response = mock(GradeResponse.class);

    when(request.value()).thenReturn(BigDecimal.valueOf(12.5));
    when(gradeRepository.findById(id)).thenReturn(Optional.of(grade));
    when(gradeRepository.save(grade)).thenReturn(updatedGrade);
    when(gradeMapper.toResponse(updatedGrade)).thenReturn(response);

    GradeResponse result = gradeService.update(id, request);

    assertEquals(response, result);

    verify(gradeRepository).findById(id);
    verify(gradeRepository).save(grade);
    verify(gradeMapper).toResponse(updatedGrade);
  }

  @Test
  void update_shouldThrowWhenGradeDoesNotExist() {
    UUID id = UUID.randomUUID();
    GradeRequest request = mock(GradeRequest.class);

    when(gradeRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> gradeService.update(id, request));
  }

  @Test
  void delete_shouldDeleteGrade() {
    UUID id = UUID.randomUUID();

    when(gradeRepository.existsById(id)).thenReturn(true);

    gradeService.delete(id);

    verify(gradeRepository).existsById(id);
    verify(gradeRepository).deleteById(id);
  }

  @Test
  void delete_shouldThrowWhenGradeDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(gradeRepository.existsById(id)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> gradeService.delete(id));

    verify(gradeRepository).existsById(id);
  }
}
