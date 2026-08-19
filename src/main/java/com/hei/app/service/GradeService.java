package com.hei.app.service;

import com.hei.app.dto.grade.GradeRequest;
import com.hei.app.dto.grade.GradeResponse;
import com.hei.app.exceptions.DuplicateResourceException;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.mapper.GradeMapper;
import com.hei.app.model.Exam;
import com.hei.app.model.Grade;
import com.hei.app.model.Role;
import com.hei.app.model.Student;
import com.hei.app.repository.CourseAssignmentRepository;
import com.hei.app.repository.ExamRepository;
import com.hei.app.repository.GradeRepository;
import com.hei.app.repository.StudentRepository;
import com.hei.app.security.CurrentUser;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeService {
  private final GradeRepository gradeRepository;
  private final GradeMapper gradeMapper;
  private final StudentRepository studentRepository;
  private final ExamRepository examRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;

  @Transactional
  public GradeResponse create(GradeRequest request, CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Students cannot create grades");
    }

    if (gradeRepository.existsByStudentIdAndExamId(request.studentId(), request.examId())) {
      throw new DuplicateResourceException(
          "Grade already exists for student "
              + request.studentId()
              + " and exam "
              + request.examId());
    }

    Grade grade = gradeMapper.toEntity(request);
    applyRelations(grade, request);

    if (currentUser.role() == Role.TEACHER) {
      assertTeacherAssignedToCourse(requireTeacherId(currentUser), courseIdOfGrade(grade));
    }

    Grade savedGrade = gradeRepository.save(grade);
    return gradeMapper.toResponse(savedGrade);
  }

  public GradeResponse findById(UUID id, CurrentUser currentUser) {
    Grade grade = findOrThrow(id);

    if (currentUser.role() == Role.STUDENT) {
      UUID studentId = requireStudentId(currentUser);
      if (!grade.getStudent().getId().equals(studentId)) {
        throw new UnauthorizedActionException("Student cannot access another student's grade");
      }
    } else if (currentUser.role() == Role.TEACHER) {
      assertTeacherAssignedToCourse(requireTeacherId(currentUser), courseIdOfGrade(grade));
    }

    return gradeMapper.toResponse(grade);
  }

  public List<GradeResponse> findAll(CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Students cannot list all grades");
    }

    List<Grade> grades = gradeRepository.findAll();

    if (currentUser.role() == Role.TEACHER) {
      UUID teacherId = requireTeacherId(currentUser);
      grades = grades.stream().filter(grade -> isTeacherAssigned(teacherId, grade)).toList();
    }

    return grades.stream().map(gradeMapper::toResponse).toList();
  }

  public List<GradeResponse> findByStudentId(UUID studentId, CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      studentId = requireStudentId(currentUser);
    }

    List<Grade> grades = gradeRepository.findByStudentId(studentId);

    if (currentUser.role() == Role.TEACHER) {
      UUID teacherId = requireTeacherId(currentUser);
      grades = grades.stream().filter(grade -> isTeacherAssigned(teacherId, grade)).toList();
    }

    return grades.stream().map(gradeMapper::toResponse).toList();
  }

  public List<GradeResponse> findByExamId(UUID examId, CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Students cannot list grades by exam");
    }

    if (currentUser.role() == Role.TEACHER) {
      assertTeacherAssignedToCourse(requireTeacherId(currentUser), courseIdOfExam(examId));
    }

    return gradeRepository.findByExamId(examId).stream().map(gradeMapper::toResponse).toList();
  }

  public GradeResponse findByStudentAndExam(
      UUID requestedStudentId, UUID examId, CurrentUser currentUser) {
    UUID studentId = requestedStudentId;

    if (currentUser.role() == Role.STUDENT) {
      studentId = requireStudentId(currentUser);
    }

    if (currentUser.role() == Role.TEACHER) {
      assertTeacherAssignedToCourse(requireTeacherId(currentUser), courseIdOfExam(examId));
    }

    UUID finalStudentId = studentId;

    Grade grade =
        gradeRepository
            .findByStudentIdAndExamId(finalStudentId, examId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Grade not found for student " + finalStudentId + " and exam " + examId));

    return gradeMapper.toResponse(grade);
  }

  @Transactional
  public GradeResponse update(UUID id, GradeRequest request, CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Students cannot update grades");
    }

    Grade grade = findOrThrow(id);

    if (currentUser.role() == Role.TEACHER) {
      assertTeacherAssignedToCourse(requireTeacherId(currentUser), courseIdOfGrade(grade));
    }

    grade.setValue(request.value());

    Grade updatedGrade = gradeRepository.save(grade);
    return gradeMapper.toResponse(updatedGrade);
  }

  @Transactional
  public void delete(UUID id, CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Students cannot delete grades");
    }

    Grade grade = findOrThrow(id);

    if (currentUser.role() == Role.TEACHER) {
      assertTeacherAssignedToCourse(requireTeacherId(currentUser), courseIdOfGrade(grade));
    }

    gradeRepository.deleteById(id);
  }

  private Grade findOrThrow(UUID id) {
    return gradeRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));
  }

  private UUID requireStudentId(CurrentUser currentUser) {
    if (currentUser.studentId() == null) {
      throw new UnauthorizedActionException("Student profile not found");
    }
    return currentUser.studentId();
  }

  private UUID requireTeacherId(CurrentUser currentUser) {
    if (currentUser.teacherId() == null) {
      throw new UnauthorizedActionException("Teacher profile not found");
    }
    return currentUser.teacherId();
  }

  private void assertTeacherAssignedToCourse(UUID teacherId, UUID courseId) {
    if (!courseAssignmentRepository.existsByTeacherIdAndCourseId(teacherId, courseId)) {
      throw new UnauthorizedActionException("Teacher is not assigned to this course");
    }
  }

  private boolean isTeacherAssigned(UUID teacherId, Grade grade) {
    return courseAssignmentRepository.existsByTeacherIdAndCourseId(
        teacherId, courseIdOfGrade(grade));
  }

  private UUID courseIdOfExam(UUID examId) {
    Exam exam =
        examRepository
            .findById(examId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));
    return exam.getCourse().getId();
  }

  private UUID courseIdOfGrade(Grade grade) {
    return grade.getExam().getCourse().getId();
  }

  private void applyRelations(Grade grade, GradeRequest request) {
    Student student =
        studentRepository
            .findById(request.studentId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Student not found with id: " + request.studentId()));
    Exam exam =
        examRepository
            .findById(request.examId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Exam not found with id: " + request.examId()));

    grade.setStudent(student);
    grade.setExam(exam);
  }
}
