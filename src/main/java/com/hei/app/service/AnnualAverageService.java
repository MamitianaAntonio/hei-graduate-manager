package com.hei.app.service;

import com.hei.app.exceptions.BusinessException;
import com.hei.app.model.Semester;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnnualAverageService {
  private final SemesterAverageService semesterAverageService;

  public BigDecimal calculate(UUID studentId, Integer academicYear) {
    List<Semester> semesters = semestersForAcademicYear(academicYear);

    BigDecimal weightedSum = BigDecimal.ZERO;
    BigDecimal totalCredits = BigDecimal.ZERO;

    for (Semester semester : semesters) {
      SemesterAverageService.SemesterAverage semesterAverage =
          semesterAverageService.calculate(studentId, semester, academicYear);

      weightedSum =
          weightedSum.add(semesterAverage.average().multiply(semesterAverage.totalCredits()));
      totalCredits = totalCredits.add(semesterAverage.totalCredits());
    }

    if (totalCredits.compareTo(BigDecimal.ZERO) == 0) {
      throw new BusinessException(
          "No results available to compute annual average for student " + studentId);
    }

    return weightedSum.divide(totalCredits, 2, RoundingMode.HALF_UP);
  }

  private List<Semester> semestersForAcademicYear(Integer academicYear) {
    int yearIndex = academicYear - 2023;
    if (yearIndex < 1 || yearIndex > 3) {
      throw new BusinessException("Unsupported academic year: " + academicYear);
    }

    return List.of(
        Semester.values()[(yearIndex - 1) * 2], Semester.values()[(yearIndex - 1) * 2 + 1]);
  }
}
