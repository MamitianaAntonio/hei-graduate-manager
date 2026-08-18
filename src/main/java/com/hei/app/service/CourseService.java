package com.hei.app.service;

import com.hei.app.dto.course.CourseRequest;
import com.hei.app.dto.course.CourseResponse;
import com.hei.app.exceptions.DuplicateResourceException;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.mapper.CourseMapper;
import com.hei.app.model.Course;
import com.hei.app.repository.CourseRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseService {
  private final CourseRepository courseRepository;
  private final CourseMapper courseMapper;

  public CourseResponse create(CourseRequest request) {
    if (courseRepository.existsByRef(request.ref())) {
      throw new DuplicateResourceException("Course already exists with ref: " + request.ref());
    }

    Course course = courseMapper.toEntity(request);
    Course savedCourse = courseRepository.save(course);

    return courseMapper.toResponse(savedCourse);
  }

  public CourseResponse findById(UUID id) {
    Course course =
        courseRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

    return courseMapper.toResponse(course);
  }

  public CourseResponse findByRef(String ref) {
    Course course =
        courseRepository
            .findByRef(ref)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with ref: " + ref));

    return courseMapper.toResponse(course);
  }

  public List<CourseResponse> findAll() {
    return courseRepository.findAll().stream().map(courseMapper::toResponse).toList();
  }

  public CourseResponse update(UUID id, CourseRequest request) {
    Course course =
        courseRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

    course.setRef(request.ref());
    course.setTitle(request.title());
    course.setCredits(request.credits());

    Course updatedCourse = courseRepository.save(course);
    return courseMapper.toResponse(updatedCourse);
  }

  public void delete(UUID id) {
    if (!courseRepository.existsById(id)) {
      throw new ResourceNotFoundException("Course not found with id: " + id);
    }

    courseRepository.deleteById(id);
  }
}
