package com.hei.app.service;

import com.hei.app.dto.assignment.CourseAssignmentRequest;
import com.hei.app.dto.assignment.CourseAssignmentResponse;
import com.hei.app.exceptions.DuplicateResourceException;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.mapper.CourseAssignmentMapper;
import com.hei.app.model.Course;
import com.hei.app.model.CourseAssignment;
import com.hei.app.model.Group;
import com.hei.app.model.Role;
import com.hei.app.model.Teacher;
import com.hei.app.repository.CourseAssignmentRepository;
import com.hei.app.repository.CourseRepository;
import com.hei.app.repository.GroupRepository;
import com.hei.app.repository.TeacherRepository;
import com.hei.app.security.CurrentUser;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseAssignmentService {
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final CourseAssignmentMapper courseAssignmentMapper;
  private final CourseRepository courseRepository;
  private final TeacherRepository teacherRepository;
  private final GroupRepository groupRepository;
  private final SecurityAsserts securityAsserts;

  @Transactional
  public CourseAssignmentResponse create(CourseAssignmentRequest request, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can create course assignments");
    }

    if (courseAssignmentRepository.existsByTeacherIdAndCourseId(
        request.teacherId(), request.courseId())) {

      throw new DuplicateResourceException("Teacher is already assigned to this course");
    }

    CourseAssignment assignment = courseAssignmentMapper.toEntity(request);
    applyRelations(assignment, request);

    CourseAssignment savedAssignment = courseAssignmentRepository.save(assignment);

    return courseAssignmentMapper.toResponse(savedAssignment);
  }

  public CourseAssignmentResponse findById(UUID id, CurrentUser currentUser) {
    CourseAssignment assignment =
        courseAssignmentRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Course assignment not found with id: " + id));

    if (currentUser.role() == Role.TEACHER) {
      assertOwnAssignment(securityAsserts.requireTeacherId(currentUser), assignment);
    }

    return courseAssignmentMapper.toResponse(assignment);
  }

  public List<CourseAssignmentResponse> findAll(CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Student cannot access course assignments");
    }

    List<CourseAssignment> assignments = courseAssignmentRepository.findAll();

    if (currentUser.role() == Role.TEACHER) {
      UUID teacherId = securityAsserts.requireTeacherId(currentUser);
      assignments = assignments.stream().filter(a -> isOwnAssignment(teacherId, a)).toList();
    }

    return assignments.stream().map(courseAssignmentMapper::toResponse).toList();
  }

  public List<CourseAssignmentResponse> findByTeacherId(UUID teacherId, CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Student cannot access course assignments");
    }
    if (currentUser.role() == Role.TEACHER) {
      teacherId = securityAsserts.requireTeacherId(currentUser);
    }

    return courseAssignmentRepository.findByTeacherId(teacherId).stream()
        .map(courseAssignmentMapper::toResponse)
        .toList();
  }

  public List<CourseAssignmentResponse> findByCourseId(UUID courseId, CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Student cannot access course assignments");
    }

    List<CourseAssignment> assignments = courseAssignmentRepository.findByCourseId(courseId);

    if (currentUser.role() == Role.TEACHER) {
      UUID teacherId = securityAsserts.requireTeacherId(currentUser);
      assignments = assignments.stream().filter(a -> isOwnAssignment(teacherId, a)).toList();
    }

    return assignments.stream().map(courseAssignmentMapper::toResponse).toList();
  }

  public List<CourseAssignmentResponse> findByGroupId(UUID groupId, CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Student cannot access course assignments");
    }

    List<CourseAssignment> assignments = courseAssignmentRepository.findByGroupId(groupId);

    if (currentUser.role() == Role.TEACHER) {
      UUID teacherId = securityAsserts.requireTeacherId(currentUser);
      assignments = assignments.stream().filter(a -> isOwnAssignment(teacherId, a)).toList();
    }

    return assignments.stream().map(courseAssignmentMapper::toResponse).toList();
  }

  @Transactional
  public CourseAssignmentResponse update(
      UUID id, CourseAssignmentRequest request, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can update course assignments");
    }

    CourseAssignment assignment =
        courseAssignmentRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Course assignment not found with id: " + id));

    applyRelations(assignment, request);

    CourseAssignment updatedAssignment = courseAssignmentRepository.save(assignment);
    return courseAssignmentMapper.toResponse(updatedAssignment);
  }

  @Transactional
  public void delete(UUID id, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can delete course assignments");
    }

    if (!courseAssignmentRepository.existsById(id)) {
      throw new ResourceNotFoundException("Course assignment not found with id: " + id);
    }

    courseAssignmentRepository.deleteById(id);
  }

  private boolean isOwnAssignment(UUID teacherId, CourseAssignment assignment) {
    return assignment.getTeacher().getId().equals(teacherId);
  }

  private void assertOwnAssignment(UUID teacherId, CourseAssignment assignment) {
    if (!isOwnAssignment(teacherId, assignment)) {
      throw new UnauthorizedActionException("Teacher cannot access another teacher's assignment");
    }
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
                    new ResourceNotFoundException("Group not found with id: " + request.groupId()));

    assignment.setCourse(course);
    assignment.setTeacher(teacher);
    assignment.setGroup(group);
    assignment.setSemester(request.semester());
    assignment.setAcademicYear(request.academicYear());
  }
}
