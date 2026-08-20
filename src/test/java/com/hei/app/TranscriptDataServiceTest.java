package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.hei.app.dto.transcript.TranscriptData;
import com.hei.app.exceptions.BusinessException;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.model.Course;
import com.hei.app.model.CourseAssignment;
import com.hei.app.model.Exam;
import com.hei.app.model.Grade;
import com.hei.app.model.Group;
import com.hei.app.model.Promotion;
import com.hei.app.model.Semester;
import com.hei.app.model.Student;
import com.hei.app.model.StudentGroupHistory;
import com.hei.app.repository.CourseAssignmentRepository;
import com.hei.app.repository.ExamRepository;
import com.hei.app.repository.GradeRepository;
import com.hei.app.repository.StudentGroupHistoryRepository;
import com.hei.app.repository.StudentRepository;
import com.hei.app.service.CourseAverageService;
import com.hei.app.service.GraduationService;
import com.hei.app.service.SemesterAverageService;
import com.hei.app.service.TranscriptDataService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TranscriptDataServiceTest {
  @Mock private StudentRepository studentRepository;

  @Mock private StudentGroupHistoryRepository studentGroupHistoryRepository;

  @Mock private CourseAssignmentRepository courseAssignmentRepository;

  @Mock private SemesterAverageService semesterAverageService;

  @Mock private CourseAverageService courseAverageService;

  @Mock private GraduationService graduationService;

  @Mock private ExamRepository examRepository;

  @Mock private GradeRepository gradeRepository;

  @InjectMocks private TranscriptDataService transcriptDataService;

  @Test
  void build_shouldAssembleTranscript() {
    UUID studentId = UUID.randomUUID();
    Student student = studentWithPromotion(studentId);
    Group group = groupWith(UUID.randomUUID());
    Course course = courseWith(UUID.randomUUID(), 4);
    Exam exam = examWith(UUID.randomUUID(), course);
    Grade grade = gradeWith(studentId, exam);

    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(studentGroupHistoryRepository.findByStudentId(studentId))
        .thenReturn(
            List.of(historyFor(group, "2024-01-01T00:00:00Z", "2024-06-30T23:59:59Z", studentId)));
    when(courseAssignmentRepository.findByGroupId(group.getId()))
        .thenReturn(List.of(assignmentFor(course, Semester.S1, 2024)));
    when(semesterAverageService.coursesFor(studentId, Semester.S1, 2024))
        .thenReturn(List.of(course));
    when(semesterAverageService.calculate(studentId, Semester.S1, 2024))
        .thenReturn(
            new SemesterAverageService.SemesterAverage(
                BigDecimal.valueOf(14), BigDecimal.valueOf(4)));
    when(courseAverageService.calculateCourseAverage(studentId, course.getId()))
        .thenReturn(BigDecimal.valueOf(14));
    when(examRepository.findByCourseId(course.getId())).thenReturn(List.of(exam));
    when(gradeRepository.findByStudentIdAndExamId(studentId, exam.getId()))
        .thenReturn(Optional.of(grade));
    when(graduationService.isGraduable(studentId)).thenReturn(true);

    TranscriptData result = transcriptDataService.build(studentId);

    assertEquals("STD001", result.std());
    assertEquals(2024, result.promotionYear());
    assertEquals(1, result.semesters().size());
    assertEquals(Semester.S1, result.semesters().get(0).semester());
    assertEquals(1, result.semesters().get(0).courses().size());
    assertEquals("ALG", result.semesters().get(0).courses().get(0).ref());
    assertEquals(1, result.semesters().get(0).courses().get(0).exams().size());
    assertEquals(new BigDecimal("14.00"), result.overallAverage());
    assertEquals(BigDecimal.valueOf(4), result.totalCredits());
    assertEquals(true, result.graduable());
  }

  @Test
  void build_shouldThrowWhenStudentNotFound() {
    UUID studentId = UUID.randomUUID();
    when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> transcriptDataService.build(studentId));
  }

  @Test
  void build_shouldThrowWhenNoGroupHistory() {
    UUID studentId = UUID.randomUUID();
    Student student = studentWithPromotion(studentId);
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(studentGroupHistoryRepository.findByStudentId(studentId)).thenReturn(List.of());

    assertThrows(BusinessException.class, () -> transcriptDataService.build(studentId));
  }

  @Test
  void build_shouldThrowWhenNoCourses() {
    UUID studentId = UUID.randomUUID();
    Student student = studentWithPromotion(studentId);
    Group group = groupWith(UUID.randomUUID());
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(studentGroupHistoryRepository.findByStudentId(studentId))
        .thenReturn(
            List.of(historyFor(group, "2024-01-01T00:00:00Z", "2024-06-30T23:59:59Z", studentId)));
    when(courseAssignmentRepository.findByGroupId(group.getId())).thenReturn(List.of());

    assertThrows(BusinessException.class, () -> transcriptDataService.build(studentId));
  }

  @Test
  void build_shouldComputeOverallAverageWeightedByCredits() {
    UUID studentId = UUID.randomUUID();
    Student student = studentWithPromotion(studentId);
    Group group = groupWith(UUID.randomUUID());
    Course courseA = courseWith(UUID.randomUUID(), 4);
    Course courseB = courseWith(UUID.randomUUID(), 8);

    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(studentGroupHistoryRepository.findByStudentId(studentId))
        .thenReturn(
            List.of(historyFor(group, "2024-01-01T00:00:00Z", "2024-06-30T23:59:59Z", studentId)));
    when(courseAssignmentRepository.findByGroupId(group.getId()))
        .thenReturn(
            List.of(
                assignmentFor(courseA, Semester.S1, 2024),
                assignmentFor(courseB, Semester.S1, 2024)));
    when(semesterAverageService.coursesFor(studentId, Semester.S1, 2024))
        .thenReturn(List.of(courseA));
    when(semesterAverageService.calculate(studentId, Semester.S1, 2024))
        .thenReturn(
            new SemesterAverageService.SemesterAverage(
                BigDecimal.valueOf(10), BigDecimal.valueOf(4)));
    when(courseAverageService.calculateCourseAverage(studentId, courseA.getId()))
        .thenReturn(BigDecimal.valueOf(10));
    when(examRepository.findByCourseId(courseA.getId())).thenReturn(List.of());
    when(graduationService.isGraduable(studentId)).thenReturn(false);

    TranscriptData result = transcriptDataService.build(studentId);

    assertEquals(new BigDecimal("10.00"), result.overallAverage());
    assertEquals(BigDecimal.valueOf(4), result.totalCredits());
    assertEquals(false, result.graduable());
  }

  private Student studentWithPromotion(UUID id) {
    Promotion promotion = new Promotion();
    promotion.setYear(2024);
    Student student = new Student();
    student.setId(id);
    student.setStd("STD001");
    student.setFirstName("John");
    student.setLastName("Doe");
    student.setPromotion(promotion);
    return student;
  }

  private Group groupWith(UUID id) {
    Group group = new Group();
    group.setId(id);
    return group;
  }

  private Course courseWith(UUID id, Integer credits) {
    Course course = new Course();
    course.setId(id);
    course.setRef("ALG");
    course.setTitle("Algèbre");
    course.setCredits(credits);
    return course;
  }

  private Exam examWith(UUID id, Course course) {
    Exam exam = new Exam();
    exam.setId(id);
    exam.setCourse(course);
    exam.setDateExam(Instant.parse("2024-03-01T10:00:00Z"));
    exam.setCoefficient(BigDecimal.valueOf(2));
    return exam;
  }

  private Grade gradeWith(UUID studentId, Exam exam) {
    Grade grade = new Grade();
    grade.setStudent(studentWithPromotion(studentId));
    grade.setExam(exam);
    grade.setValue(BigDecimal.valueOf(14));
    return grade;
  }

  private StudentGroupHistory historyFor(Group group, String start, String end, UUID studentId) {
    StudentGroupHistory history = new StudentGroupHistory();
    history.setGroup(group);
    history.setStartDate(Instant.parse(start));
    history.setEndDate(end == null ? null : Instant.parse(end));
    history.setStudent(studentWithPromotion(studentId));
    return history;
  }

  private CourseAssignment assignmentFor(Course course, Semester semester, Integer year) {
    CourseAssignment assignment = new CourseAssignment();
    assignment.setCourse(course);
    assignment.setSemester(semester);
    assignment.setAcademicYear(year);
    return assignment;
  }
}
