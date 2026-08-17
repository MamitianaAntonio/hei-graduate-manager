package com.hei.app.mapper;

import com.hei.app.dto.course.CourseRequest;
import com.hei.app.dto.course.CourseResponse;
import com.hei.app.model.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {
  public Course toEntity(CourseRequest request) {
    Course course = new Course();

    course.setRef(request.ref());
    course.setTitle(request.title());
    course.setCredits(request.credits());

    return course;
  }

  public CourseResponse toResponse(Course course) {
    return new CourseResponse(
        course.getId(), course.getRef(), course.getTitle(), course.getCredits());
  }
}
