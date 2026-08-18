package com.hei.app.mapper;

import com.hei.app.dto.assignment.CourseAssignmentRequest;
import com.hei.app.dto.assignment.CourseAssignmentResponse;
import com.hei.app.model.CourseAssignment;
import org.springframework.stereotype.Component;

@Component
public class CourseAssignmentMapper {
  public CourseAssignment toEntity(CourseAssignmentRequest request) {
    CourseAssignment assignment = new CourseAssignment();
    assignment.setSemester(request.semester());
    assignment.setAcademicYear(request.academicYear());
    return assignment;
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
}