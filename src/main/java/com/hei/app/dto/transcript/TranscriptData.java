package com.hei.app.dto.transcript;

import com.hei.app.model.Semester;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record TranscriptData(
    String firstName,
    String lastName,
    String std,
    Integer promotionYear,
    List<SemesterBlock> semesters,
    BigDecimal overallAverage,
    BigDecimal totalCredits,
    boolean graduable) {

  public record SemesterBlock(
      Semester semester,
      Integer academicYear,
      List<CourseBlock> courses,
      BigDecimal average,
      BigDecimal totalCredits) {}

  public record CourseBlock(
      String ref, String title, Integer credits, BigDecimal average, List<ExamGrade> exams) {}

  public record ExamGrade(Instant date, BigDecimal coefficient, BigDecimal grade) {}
}
