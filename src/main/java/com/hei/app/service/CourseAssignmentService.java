package com.hei.app.service;

import com.hei.app.dto.assignment.CourseAssignmentRequest;
import com.hei.app.dto.assignment.CourseAssignmentResponse;
import com.hei.app.exceptions.DuplicateResourceException;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.mapper.CourseAssignmentMapper;
import com.hei.app.model.Course;
import com.hei.app.model.CourseAssignment;
import com.hei.app.model.Group;
import com.hei.app.model.Teacher;
import com.hei.app.repository.CourseAssignmentRepository;
import com.hei.app.repository.CourseRepository;
import com.hei.app.repository.GroupRepository;
import com.hei.app.repository.TeacherRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseAssignmentService {
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final CourseAssignmentMapper courseAssignmentMapper;
  private final CourseRepository courseRepository;
  private final TeacherRepository teacherRepository;
  private final GroupRepository groupRepository;

  public CourseAssignmentResponse create(CourseAssignmentRequest request) {
    if (courseAssignmentRepository.existsByTeacherIdAndCourseId(
        request.teacherId(), request.courseId())) {

      throw new DuplicateResourceException(
          "Teacher is already assigned to this course");
    }

    CourseAssignment assignment = courseAssignmentMapper.toEntity(request);
    applyRelations(assignment, request);

    CourseAssignment savedAssignment = courseAssignmentRepository.save(assignment);

    return courseAssignmentMapper.toResponse(savedAssignment);
  }

  public CourseAssignmentResponse findById(UUID id) {
    CourseAssignment assignment =
        courseAssignmentRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Course assignment not found with id: " + id));

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
                () ->
                    new ResourceNotFoundException(
                        "Course assignment not found with id: " + id));

    applyRelations(assignment, request);

    CourseAssignment updatedAssignment = courseAssignmentRepository.save(assignment);
    return courseAssignmentMapper.toResponse(updatedAssignment);
  }

  public void delete(UUID id) {
    if (!courseAssignmentRepository.existsById(id)) {
      throw new ResourceNotFoundException("Course assignment not found with id: " + id);
    }

    courseAssignmentRepository.deleteById(id);
  }

  private void applyRelations(CourseAssignment assignment, CourseAssignmentRequest request) {
    Course course =
        courseRepository
            .findById(request.courseId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Course not found with id: " + request.courseId()));
    Teacher teacher =
        teacherRepository
            .findById(request.teacherId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Teacher not found with id: " + request.teacherId()));
    Group group =
        groupRepository
            .findById(request.groupId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Group not found with id: " + request.groupId()));

    assignment.setCourse(course);
    assignment.setTeacher(teacher);
    assignment.setGroup(group);
    assignment.setSemester(request.semester());
    assignment.setAcademicYear(request.academicYear());
  }
}