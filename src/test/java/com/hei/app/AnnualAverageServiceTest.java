package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.app.exceptions.BusinessException;
import com.hei.app.model.CourseAssignment;
import com.hei.app.model.Semester;
import com.hei.app.repository.CourseAssignmentRepository;
import com.hei.app.service.AnnualAverageService;
import com.hei.app.service.SemesterAverageService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AnnualAverageServiceTest {
  @Mock private CourseAssignmentRepository courseAssignmentRepository;

  @Mock private SemesterAverageService semesterAverageService;

  @InjectMocks private AnnualAverageService annualAverageService;

  @Test
  void calculate_shouldReturnAnnualAverage() {
    UUID studentId = UUID.randomUUID();
    Integer academicYear = 2025;

    when(courseAssignmentRepository.findByAcademicYear(academicYear))
        .thenReturn(List.of(assignmentFor(Semester.S3), assignmentFor(Semester.S4)));
    when(semesterAverageService.calculate(studentId, Semester.S3, academicYear))
        .thenReturn(
            new SemesterAverageService.SemesterAverage(
                BigDecimal.valueOf(13), BigDecimal.valueOf(6)));
    when(semesterAverageService.calculate(studentId, Semester.S4, academicYear))
        .thenReturn(
            new SemesterAverageService.SemesterAverage(
                BigDecimal.valueOf(11), BigDecimal.valueOf(6)));

    BigDecimal result = annualAverageService.calculate(studentId, academicYear);

    assertEquals(new BigDecimal("12.00"), result);

    verify(courseAssignmentRepository).findByAcademicYear(academicYear);
    verify(semesterAverageService).calculate(studentId, Semester.S3, academicYear);
    verify(semesterAverageService).calculate(studentId, Semester.S4, academicYear);
  }

  @Test
  void calculate_shouldUseSemesterAveragesCorrectly() {
    UUID studentId = UUID.randomUUID();
    Integer academicYear = 2025;

    when(courseAssignmentRepository.findByAcademicYear(academicYear))
        .thenReturn(List.of(assignmentFor(Semester.S3), assignmentFor(Semester.S4)));
    when(semesterAverageService.calculate(studentId, Semester.S3, academicYear))
        .thenReturn(
            new SemesterAverageService.SemesterAverage(
                BigDecimal.valueOf(14), BigDecimal.valueOf(6)));
    when(semesterAverageService.calculate(studentId, Semester.S4, academicYear))
        .thenReturn(
            new SemesterAverageService.SemesterAverage(
                BigDecimal.valueOf(10), BigDecimal.valueOf(2)));

    BigDecimal result = annualAverageService.calculate(studentId, academicYear);

    assertEquals(new BigDecimal("13.00"), result);
  }

  @Test
  void calculate_shouldThrowWhenNoResultsAreAvailable() {
    UUID studentId = UUID.randomUUID();
    Integer academicYear = 2025;

    when(courseAssignmentRepository.findByAcademicYear(academicYear)).thenReturn(List.of());

    assertThrows(
        BusinessException.class, () -> annualAverageService.calculate(studentId, academicYear));
  }

  @Test
  void calculate_shouldPropagateSemesterErrorWhenNoGradesAreAvailable() {
    UUID studentId = UUID.randomUUID();
    Integer academicYear = 2025;

    when(courseAssignmentRepository.findByAcademicYear(academicYear))
        .thenReturn(List.of(assignmentFor(Semester.S3), assignmentFor(Semester.S4)));
    when(semesterAverageService.calculate(studentId, Semester.S3, academicYear))
        .thenThrow(new BusinessException("No courses available"));

    assertThrows(
        BusinessException.class, () -> annualAverageService.calculate(studentId, academicYear));
  }

  private CourseAssignment assignmentFor(Semester semester) {
    CourseAssignment assignment = new CourseAssignment();
    assignment.setSemester(semester);
    return assignment;
  }
}
