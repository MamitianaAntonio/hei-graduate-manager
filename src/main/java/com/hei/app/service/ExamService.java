package com.hei.app.service;

import com.hei.app.dto.exam.ExamRequest;
import com.hei.app.dto.exam.ExamResponse;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.mapper.ExamMapper;
import com.hei.app.model.Course;
import com.hei.app.model.Exam;
import com.hei.app.model.Role;
import com.hei.app.repository.CourseRepository;
import com.hei.app.repository.ExamRepository;
import com.hei.app.security.CurrentUser;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamService {
  private final ExamRepository examRepository;
  private final ExamMapper examMapper;
  private final CourseRepository courseRepository;
  private final SecurityAsserts securityAsserts;

  @Transactional
  public ExamResponse create(ExamRequest request, CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Student cannot create exams");
    }

    if (currentUser.role() == Role.TEACHER) {
      securityAsserts.assertTeacherAssignedToCourse(
          securityAsserts.requireTeacherId(currentUser), request.courseId());
    }

    Course course =
        courseRepository
            .findById(request.courseId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Course not found with id: " + request.courseId()));

    Exam exam = examMapper.toEntity(request);
    exam.setCourse(course);

    Exam savedExam = examRepository.save(exam);

    return examMapper.toResponse(savedExam);
  }

  public ExamResponse findById(UUID id, CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Student cannot read exam details");
    }

    Exam exam =
        examRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));

    if (currentUser.role() == Role.TEACHER) {
      securityAsserts.assertTeacherAssignedToCourse(
          securityAsserts.requireTeacherId(currentUser), courseIdOfExam(exam));
    }

    return examMapper.toResponse(exam);
  }

  public List<ExamResponse> findAll(CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Student cannot list all exams");
    }

    List<Exam> exams = examRepository.findAll();

    if (currentUser.role() == Role.TEACHER) {
      UUID teacherId = securityAsserts.requireTeacherId(currentUser);
      exams = exams.stream().filter(exam -> isTeacherAssigned(teacherId, exam)).toList();
    }

    return exams.stream().map(examMapper::toResponse).toList();
  }

  public List<ExamResponse> findByCourseId(UUID courseId, CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Student cannot list exams by course");
    }

    if (currentUser.role() == Role.TEACHER) {
      securityAsserts.assertTeacherAssignedToCourse(
          securityAsserts.requireTeacherId(currentUser), courseId);
    }

    return examRepository.findByCourseId(courseId).stream().map(examMapper::toResponse).toList();
  }

  @Transactional
  public ExamResponse update(UUID id, ExamRequest request, CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Student cannot update exams");
    }

    Exam exam =
        examRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));

    if (currentUser.role() == Role.TEACHER) {
      UUID teacherId = securityAsserts.requireTeacherId(currentUser);
      securityAsserts.assertTeacherAssignedToCourse(teacherId, courseIdOfExam(exam));
      securityAsserts.assertTeacherAssignedToCourse(teacherId, request.courseId());
    }

    Course course =
        courseRepository
            .findById(request.courseId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Course not found with id: " + request.courseId()));

    exam.setDateExam(request.dateExam());
    exam.setCoefficient(request.coefficient());
    exam.setCourse(course);

    Exam updatedExam = examRepository.save(exam);
    return examMapper.toResponse(updatedExam);
  }

  @Transactional
  public void delete(UUID id, CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Student cannot delete exams");
    }

    Exam exam =
        examRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));

    if (currentUser.role() == Role.TEACHER) {
      securityAsserts.assertTeacherAssignedToCourse(
          securityAsserts.requireTeacherId(currentUser), courseIdOfExam(exam));
    }

    examRepository.deleteById(id);
  }

  private boolean isTeacherAssigned(UUID teacherId, Exam exam) {
    return securityAsserts.isTeacherAssignedToCourse(teacherId, courseIdOfExam(exam));
  }

  private UUID courseIdOfExam(Exam exam) {
    return exam.getCourse().getId();
  }
}
