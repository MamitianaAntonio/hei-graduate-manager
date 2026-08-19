package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.app.exceptions.BusinessException;
import com.hei.app.model.Exam;
import com.hei.app.model.Grade;
import com.hei.app.repository.ExamRepository;
import com.hei.app.repository.GradeRepository;
import com.hei.app.service.CourseAverageService;
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
public class CourseAverageServiceTest {
  @Mock private ExamRepository examRepository;

  @Mock private GradeRepository gradeRepository;

  @InjectMocks private CourseAverageService courseAverageService;

  @Test
  void calculateCourseAverage_shouldReturnWeightedAverage() {
    UUID studentId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();

    Exam exam1 = new Exam();
    exam1.setId(UUID.randomUUID());
    exam1.setCoefficient(BigDecimal.valueOf(0.25));
    Exam exam2 = new Exam();
    exam2.setId(UUID.randomUUID());
    exam2.setCoefficient(BigDecimal.valueOf(0.25));
    Exam exam3 = new Exam();
    exam3.setId(UUID.randomUUID());
    exam3.setCoefficient(BigDecimal.valueOf(0.50));

    Grade grade1 = new Grade();
    grade1.setValue(BigDecimal.valueOf(12));
    Grade grade2 = new Grade();
    grade2.setValue(BigDecimal.valueOf(15));
    Grade grade3 = new Grade();
    grade3.setValue(BigDecimal.valueOf(14));

    when(examRepository.findByCourseId(courseId)).thenReturn(List.of(exam1, exam2, exam3));
    when(gradeRepository.findByStudentIdAndExamId(studentId, exam1.getId()))
        .thenReturn(Optional.of(grade1));
    when(gradeRepository.findByStudentIdAndExamId(studentId, exam2.getId()))
        .thenReturn(Optional.of(grade2));
    when(gradeRepository.findByStudentIdAndExamId(studentId, exam3.getId()))
        .thenReturn(Optional.of(grade3));

    BigDecimal result = courseAverageService.calculateCourseAverage(studentId, courseId);

    assertEquals(BigDecimal.valueOf(13.75), result);

    verify(examRepository).findByCourseId(courseId);
    verify(gradeRepository).findByStudentIdAndExamId(studentId, exam1.getId());
    verify(gradeRepository).findByStudentIdAndExamId(studentId, exam2.getId());
    verify(gradeRepository).findByStudentIdAndExamId(studentId, exam3.getId());
  }

  @Test
  void calculateCourseAverage_shouldIgnoreExamsWithoutGrade() {
    UUID studentId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();

    Exam exam1 = new Exam();
    exam1.setId(UUID.randomUUID());
    exam1.setCoefficient(BigDecimal.valueOf(0.25));
    Exam exam2 = new Exam();
    exam2.setId(UUID.randomUUID());
    exam2.setCoefficient(BigDecimal.valueOf(0.25));

    Grade grade1 = new Grade();
    grade1.setValue(BigDecimal.valueOf(12));

    when(examRepository.findByCourseId(courseId)).thenReturn(List.of(exam1, exam2));
    when(gradeRepository.findByStudentIdAndExamId(studentId, exam1.getId()))
        .thenReturn(Optional.of(grade1));
    when(gradeRepository.findByStudentIdAndExamId(studentId, exam2.getId()))
        .thenReturn(Optional.empty());

    BigDecimal result = courseAverageService.calculateCourseAverage(studentId, courseId);

    assertEquals(new BigDecimal("12.00"), result);
  }

  @Test
  void calculateCourseAverage_shouldRoundToTwoDecimals() {
    UUID studentId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();

    Exam exam1 = new Exam();
    exam1.setId(UUID.randomUUID());
    exam1.setCoefficient(BigDecimal.valueOf(1));
    Exam exam2 = new Exam();
    exam2.setId(UUID.randomUUID());
    exam2.setCoefficient(BigDecimal.valueOf(1));
    Exam exam3 = new Exam();
    exam3.setId(UUID.randomUUID());
    exam3.setCoefficient(BigDecimal.valueOf(1));

    Grade grade1 = new Grade();
    grade1.setValue(BigDecimal.valueOf(10));
    Grade grade2 = new Grade();
    grade2.setValue(BigDecimal.valueOf(11));
    Grade grade3 = new Grade();
    grade3.setValue(BigDecimal.valueOf(13));

    when(examRepository.findByCourseId(courseId)).thenReturn(List.of(exam1, exam2, exam3));
    when(gradeRepository.findByStudentIdAndExamId(studentId, exam1.getId()))
        .thenReturn(Optional.of(grade1));
    when(gradeRepository.findByStudentIdAndExamId(studentId, exam2.getId()))
        .thenReturn(Optional.of(grade2));
    when(gradeRepository.findByStudentIdAndExamId(studentId, exam3.getId()))
        .thenReturn(Optional.of(grade3));

    BigDecimal result = courseAverageService.calculateCourseAverage(studentId, courseId);

    assertEquals(BigDecimal.valueOf(11.33), result);
  }

  @Test
  void calculateCourseAverage_shouldThrowWhenNoGradesAvailable() {
    UUID studentId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();

    Exam exam1 = new Exam();
    exam1.setId(UUID.randomUUID());
    exam1.setCoefficient(BigDecimal.valueOf(0.25));

    when(examRepository.findByCourseId(courseId)).thenReturn(List.of(exam1));
    when(gradeRepository.findByStudentIdAndExamId(studentId, exam1.getId()))
        .thenReturn(Optional.empty());

    assertThrows(
        BusinessException.class,
        () -> courseAverageService.calculateCourseAverage(studentId, courseId));
  }

  @Test
  void calculateCourseAverage_shouldThrowWhenNoExamsForCourse() {
    UUID studentId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();

    when(examRepository.findByCourseId(courseId)).thenReturn(List.of());

    assertThrows(
        BusinessException.class,
        () -> courseAverageService.calculateCourseAverage(studentId, courseId));
  }
}
