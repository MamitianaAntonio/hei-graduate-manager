package com.hei.app.service;

import com.hei.app.dto.grade.GradeRequest;
import com.hei.app.dto.grade.GradeResponse;
import com.hei.app.exceptions.DuplicateResourceException;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.mapper.GradeMapper;
import com.hei.app.model.Exam;
import com.hei.app.model.Grade;
import com.hei.app.model.Student;
import com.hei.app.repository.ExamRepository;
import com.hei.app.repository.GradeRepository;
import com.hei.app.repository.StudentRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GradeService {
  private final GradeRepository gradeRepository;
  private final GradeMapper gradeMapper;
  private final StudentRepository studentRepository;
  private final ExamRepository examRepository;

  public GradeResponse create(GradeRequest request) {
    if (gradeRepository.existsByStudentIdAndExamId(request.studentId(), request.examId())) {
      throw new DuplicateResourceException(
          "Grade already exists for student "
              + request.studentId()
              + " and exam "
              + request.examId());
    }

    Grade grade = gradeMapper.toEntity(request);
    applyRelations(grade, request);

    Grade savedGrade = gradeRepository.save(grade);
    return gradeMapper.toResponse(savedGrade);
  }

  public GradeResponse findById(UUID id) {
    Grade grade =
        gradeRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));

    return gradeMapper.toResponse(grade);
  }

  public List<GradeResponse> findAll() {
    return gradeRepository.findAll().stream().map(gradeMapper::toResponse).toList();
  }

  public List<GradeResponse> findByStudentId(UUID studentId) {
    return gradeRepository.findByStudentId(studentId).stream()
        .map(gradeMapper::toResponse)
        .toList();
  }

  public List<GradeResponse> findByExamId(UUID examId) {
    return gradeRepository.findByExamId(examId).stream().map(gradeMapper::toResponse).toList();
  }

  public GradeResponse findByStudentAndExam(UUID studentId, UUID examId) {
    Grade grade =
        gradeRepository
            .findByStudentIdAndExamId(studentId, examId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Grade not found for student " + studentId + " and exam " + examId));

    return gradeMapper.toResponse(grade);
  }

  public GradeResponse update(UUID id, GradeRequest request) {
    Grade grade =
        gradeRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));

    grade.setValue(request.value());

    Grade updatedGrade = gradeRepository.save(grade);
    return gradeMapper.toResponse(updatedGrade);
  }

  public void delete(UUID id) {
    if (!gradeRepository.existsById(id)) {
      throw new ResourceNotFoundException("Grade not found with id: " + id);
    }

    gradeRepository.deleteById(id);
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
