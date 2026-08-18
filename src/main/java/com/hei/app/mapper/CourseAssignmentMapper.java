package com.hei.app.mapper;

import com.hei.app.dto.assignment.CourseAssignmentRequest;
import com.hei.app.dto.assignment.CourseAssignmentResponse;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.model.Course;
import com.hei.app.model.CourseAssignment;
import com.hei.app.model.Group;
import com.hei.app.model.Teacher;
import com.hei.app.repository.CourseRepository;
import com.hei.app.repository.GroupRepository;
import com.hei.app.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseAssignmentMapper {
  private final CourseRepository courseRepository;
  private final TeacherRepository teacherRepository;
  private final GroupRepository groupRepository;

  public CourseAssignment toEntity(CourseAssignmentRequest request) {
    CourseAssignment assignment = new CourseAssignment();
    apply(assignment, request);
    return assignment;
  }

  public void toEntity(CourseAssignment assignment, CourseAssignmentRequest request) {
    apply(assignment, request);
  }

  public CourseAssignmentResponse toResponse(CourseAssignment assignment) {
    return new CourseAssignmentResponse(
        assignment.getId(),
        assignment.getCourse().getId(),
        assignment.getTeacher().getId(),
        assignment.getGroup().getId(),
        assignment.getSemester(),
        assignment.getAcademicYear());
  }

  private void apply(CourseAssignment assignment, CourseAssignmentRequest request) {
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
