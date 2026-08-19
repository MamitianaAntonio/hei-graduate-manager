package com.hei.app.service;

import com.hei.app.exceptions.BusinessException;
import com.hei.app.model.Exam;
import com.hei.app.model.Grade;
import com.hei.app.repository.ExamRepository;
import com.hei.app.repository.GradeRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseAverageService {
  private final ExamRepository examRepository;
  private final GradeRepository gradeRepository;

  public BigDecimal calculateCourseAverage(UUID studentId, UUID courseId) {
    List<Exam> exams = examRepository.findByCourseId(courseId);

    BigDecimal weightedSum = BigDecimal.ZERO;
    BigDecimal totalCoefficient = BigDecimal.ZERO;

    for (Exam exam : exams) {
      Optional<Grade> grade = gradeRepository.findByStudentIdAndExamId(studentId, exam.getId());
      if (grade.isPresent()) {
        weightedSum = weightedSum.add(exam.getCoefficient().multiply(grade.get().getValue()));
        totalCoefficient = totalCoefficient.add(exam.getCoefficient());
      }
    }

    if (totalCoefficient.compareTo(BigDecimal.ZERO) == 0) {
      throw new BusinessException(
          "No grades available to compute average for student "
              + studentId
              + " in course "
              + courseId);
    }

    return weightedSum.divide(totalCoefficient, 2, RoundingMode.HALF_UP);
  }
}
