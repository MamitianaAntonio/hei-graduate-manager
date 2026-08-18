package com.hei.app.service;

import com.hei.app.dto.assignment.CourseAssignmentRequest;
import com.hei.app.dto.assignment.CourseAssignmentResponse;
import com.hei.app.exceptions.DuplicateResourceException;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.mapper.CourseAssignmentMapper;
import com.hei.app.model.CourseAssignment;
import com.hei.app.repository.CourseAssignmentRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseAssignmentService {
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final CourseAssignmentMapper courseAssignmentMapper;

  public CourseAssignmentResponse create(CourseAssignmentRequest request) {
    if (courseAssignmentRepository.existsByTeacherIdAndCourseId(
        request.teacherId(), request.courseId())) {
      throw new DuplicateResourceException("Teacher is already assigned to this course");
    }

    CourseAssignment assignment = courseAssignmentMapper.toEntity(request);
    CourseAssignment savedAssignment = courseAssignmentRepository.save(assignment);
    return courseAssignmentMapper.toResponse(savedAssignment);
  }

  public CourseAssignmentResponse findById(UUID id) {
    CourseAssignment assignment =
        courseAssignmentRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Course assignment not found with id: " + id));

    return courseAssignmentMapper.toResponse(assignment);
  }

  public List<CourseAssignmentResponse> findAll() {
    return courseAssignmentRepository.findAll().stream()
        .map(courseAssignmentMapper::toResponse)
        .toList();
  }

  public List<CourseAssignmentResponse> findByTeacherId(UUID teacherId) {
    return courseAssignmentRepository.findByTeacherId(teacherId).stream()
        .map(courseAssignmentMapper::toResponse)
        .toList();
  }

  public List<CourseAssignmentResponse> findByCourseId(UUID courseId) {
    return courseAssignmentRepository.findByCourseId(courseId).stream()
        .map(courseAssignmentMapper::toResponse)
        .toList();
  }

  public List<CourseAssignmentResponse> findByGroupId(UUID groupId) {
    return courseAssignmentRepository.findByGroupId(groupId).stream()
        .map(courseAssignmentMapper::toResponse)
        .toList();
  }

  public CourseAssignmentResponse update(UUID id, CourseAssignmentRequest request) {
    CourseAssignment assignment =
        courseAssignmentRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Course assignment not found with id: " + id));

    courseAssignmentMapper.toEntity(assignment, request);

    CourseAssignment updatedAssignment = courseAssignmentRepository.save(assignment);
    return courseAssignmentMapper.toResponse(updatedAssignment);
  }

  public void delete(UUID id) {
    if (!courseAssignmentRepository.existsById(id)) {
      throw new ResourceNotFoundException("Course assignment not found with id: " + id);
    }

    courseAssignmentRepository.deleteById(id);
  }
}
