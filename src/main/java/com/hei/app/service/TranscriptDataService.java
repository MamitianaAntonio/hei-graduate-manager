package com.hei.app.service;

import com.hei.app.dto.transcript.TranscriptData;
import com.hei.app.exceptions.BusinessException;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.model.Course;
import com.hei.app.model.CourseAssignment;
import com.hei.app.model.Grade;
import com.hei.app.model.Semester;
import com.hei.app.model.Student;
import com.hei.app.model.StudentGroupHistory;
import com.hei.app.repository.CourseAssignmentRepository;
import com.hei.app.repository.ExamRepository;
import com.hei.app.repository.GradeRepository;
import com.hei.app.repository.StudentGroupHistoryRepository;
import com.hei.app.repository.StudentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TranscriptDataService {
  private final StudentRepository studentRepository;
  private final StudentGroupHistoryRepository studentGroupHistoryRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final SemesterAverageService semesterAverageService;
  private final CourseAverageService courseAverageService;
  private final GraduationService graduationService;
  private final ExamRepository examRepository;
  private final GradeRepository gradeRepository;

  public TranscriptData build(UUID studentId) {
    Student student =
        studentRepository
            .findById(studentId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Student not found with id: " + studentId));

    List<SemesterYear> semesterYears = semesterYearsOf(studentId);

    List<TranscriptData.SemesterBlock> semesterBlocks = new ArrayList<>();
    BigDecimal totalCredits = BigDecimal.ZERO;

    for (SemesterYear semesterYear : semesterYears) {
      TranscriptData.SemesterBlock block = semesterBlock(studentId, semesterYear);
      semesterBlocks.add(block);
      totalCredits = totalCredits.add(block.totalCredits());
    }

    BigDecimal overallAverage = overallAverage(semesterBlocks);
    boolean graduable = graduationService.isGraduable(studentId);

    return new TranscriptData(
        student.getFirstName(),
        student.getLastName(),
        student.getStd(),
        student.getPromotion().getYear(),
        semesterBlocks,
        overallAverage,
        totalCredits,
        graduable);
  }

  private List<SemesterYear> semesterYearsOf(UUID studentId) {
    List<StudentGroupHistory> history = studentGroupHistoryRepository.findByStudentId(studentId);

    if (history.isEmpty()) {
      throw new BusinessException("No group history found for student " + studentId);
    }

    Set<SemesterYear> semesterYears = new LinkedHashSet<>();
    for (StudentGroupHistory entry : history) {
      for (CourseAssignment assignment :
          courseAssignmentRepository.findByGroupId(entry.getGroup().getId())) {
        SemesterPeriods.Period period =
            SemesterPeriods.of(assignment.getSemester(), assignment.getAcademicYear());
        if (SemesterPeriods.overlaps(entry.getStartDate(), entry.getEndDate(), period)) {
          semesterYears.add(
              new SemesterYear(assignment.getSemester(), assignment.getAcademicYear()));
        }
      }
    }

    if (semesterYears.isEmpty()) {
      throw new BusinessException("No courses found for student " + studentId);
    }

    List<SemesterYear> sorted = new ArrayList<>(semesterYears);
    sorted.sort(
        Comparator.comparing(SemesterYear::academicYear).thenComparing(SemesterYear::semester));
    return sorted;
  }

  private TranscriptData.SemesterBlock semesterBlock(UUID studentId, SemesterYear semesterYear) {
    List<Course> courses =
        semesterAverageService.coursesFor(
            studentId, semesterYear.semester(), semesterYear.academicYear());

    List<TranscriptData.CourseBlock> courseBlocks = new ArrayList<>();
    for (Course course : courses) {
      courseBlocks.add(courseBlock(studentId, course));
    }

    SemesterAverageService.SemesterAverage semesterAverage =
        semesterAverageService.calculate(
            studentId, semesterYear.semester(), semesterYear.academicYear());

    return new TranscriptData.SemesterBlock(
        semesterYear.semester(),
        semesterYear.academicYear(),
        courseBlocks,
        semesterAverage.average(),
        semesterAverage.totalCredits());
  }

  private TranscriptData.CourseBlock courseBlock(UUID studentId, Course course) {
    BigDecimal average = courseAverageService.calculateCourseAverage(studentId, course.getId());

    List<TranscriptData.ExamGrade> examGrades =
        examRepository.findByCourseId(course.getId()).stream()
            .map(
                exam -> {
                  Optional<Grade> grade =
                      gradeRepository.findByStudentIdAndExamId(studentId, exam.getId());
                  return new TranscriptData.ExamGrade(
                      exam.getDateExam(),
                      exam.getCoefficient(),
                      grade.map(Grade::getValue).orElse(null));
                })
            .sorted(Comparator.comparing(TranscriptData.ExamGrade::date))
            .toList();

    return new TranscriptData.CourseBlock(
        course.getRef(), course.getTitle(), course.getCredits(), average, examGrades);
  }

  private BigDecimal overallAverage(List<TranscriptData.SemesterBlock> semesterBlocks) {
    BigDecimal weightedSum = BigDecimal.ZERO;
    BigDecimal totalCredits = BigDecimal.ZERO;

    for (TranscriptData.SemesterBlock block : semesterBlocks) {
      weightedSum = weightedSum.add(block.average().multiply(block.totalCredits()));
      totalCredits = totalCredits.add(block.totalCredits());
    }

    if (totalCredits.compareTo(BigDecimal.ZERO) == 0) {
      throw new BusinessException("No credits available to compute overall average");
    }

    return weightedSum.divide(totalCredits, 2, RoundingMode.HALF_UP);
  }

  private record SemesterYear(Semester semester, Integer academicYear) {}
}
