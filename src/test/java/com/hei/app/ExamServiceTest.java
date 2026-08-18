package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.app.dto.exam.ExamRequest;
import com.hei.app.dto.exam.ExamResponse;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.mapper.ExamMapper;
import com.hei.app.model.Course;
import com.hei.app.model.Exam;
import com.hei.app.repository.CourseRepository;
import com.hei.app.repository.ExamRepository;
import com.hei.app.service.ExamService;
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

    ExamResponse result = examService.create(request);

    assertEquals(response, result);

    verify(courseRepository).findById(request.courseId());
    verify(examMapper).toEntity(request);
    verify(examRepository).save(exam);
    verify(examMapper).toResponse(savedExam);
  }

  @Test
  void findById_shouldReturnExam() {
    UUID id = UUID.randomUUID();
    Exam exam = new Exam();
    ExamResponse response = mock(ExamResponse.class);

    when(examRepository.findById(id)).thenReturn(Optional.of(exam));
    when(examMapper.toResponse(exam)).thenReturn(response);

    ExamResponse result = examService.findById(id);

    assertEquals(response, result);

    verify(examRepository).findById(id);
    verify(examMapper).toResponse(exam);
  }

  @Test
  void findById_shouldThrowWhenExamDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(examRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> examService.findById(id));
  }

  @Test
  void findAll_shouldReturnExams() {
    Exam exam1 = new Exam();
    exam1.setCoefficient(java.math.BigDecimal.ONE);
    Exam exam2 = new Exam();
    exam2.setCoefficient(java.math.BigDecimal.TWO);
    ExamResponse response1 = mock(ExamResponse.class);
    ExamResponse response2 = mock(ExamResponse.class);

    when(examRepository.findAll()).thenReturn(List.of(exam1, exam2));
    when(examMapper.toResponse(exam1)).thenReturn(response1);
    when(examMapper.toResponse(exam2)).thenReturn(response2);

    List<ExamResponse> result = examService.findAll();

    assertEquals(List.of(response1, response2), result);

    verify(examRepository).findAll();
    verify(examMapper).toResponse(exam1);
    verify(examMapper).toResponse(exam2);
  }

  @Test
  void findByCourseId_shouldReturnExams() {
    UUID courseId = UUID.randomUUID();
    Exam exam = new Exam();
    ExamResponse response = mock(ExamResponse.class);

    when(examRepository.findByCourseId(courseId)).thenReturn(List.of(exam));
    when(examMapper.toResponse(exam)).thenReturn(response);

    List<ExamResponse> result = examService.findByCourseId(courseId);

    assertEquals(List.of(response), result);

    verify(examRepository).findByCourseId(courseId);
    verify(examMapper).toResponse(exam);
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

    ExamResponse result = examService.update(id, request);

    assertEquals(response, result);

    verify(examRepository).findById(id);
    verify(courseRepository).findById(request.courseId());
    verify(examRepository).save(exam);
    verify(examMapper).toResponse(updatedExam);
  }

  @Test
  void update_shouldThrowWhenExamDoesNotExist() {
    UUID id = UUID.randomUUID();
    ExamRequest request = mock(ExamRequest.class);

    when(examRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> examService.update(id, request));
  }

  @Test
  void delete_shouldDeleteExam() {
    UUID id = UUID.randomUUID();

    when(examRepository.existsById(id)).thenReturn(true);

    examService.delete(id);

    verify(examRepository).existsById(id);
    verify(examRepository).deleteById(id);
  }

  @Test
  void delete_shouldThrowWhenExamDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(examRepository.existsById(id)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> examService.delete(id));

    verify(examRepository).existsById(id);
  }
}
