package com.hei.app.service;

import com.hei.app.dto.exam.ExamRequest;
import com.hei.app.dto.exam.ExamResponse;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.mapper.ExamMapper;
import com.hei.app.model.Course;
import com.hei.app.model.Exam;
import com.hei.app.repository.CourseRepository;
import com.hei.app.repository.ExamRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExamService {
  private final ExamRepository examRepository;
  private final ExamMapper examMapper;
  private final CourseRepository courseRepository;

  public ExamResponse create(ExamRequest request) {
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

  public ExamResponse findById(UUID id) {
    Exam exam =
        examRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));

    return examMapper.toResponse(exam);
  }

  public List<ExamResponse> findAll() {
    return examRepository.findAll().stream().map(examMapper::toResponse).toList();
  }

  public List<ExamResponse> findByCourseId(UUID courseId) {
    return examRepository.findByCourseId(courseId).stream().map(examMapper::toResponse).toList();
  }

  public ExamResponse update(UUID id, ExamRequest request) {
    Exam exam =
        examRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));

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

  public void delete(UUID id) {
    if (!examRepository.existsById(id)) {
      throw new ResourceNotFoundException("Exam not found with id: " + id);
    }

    examRepository.deleteById(id);
  }
}
