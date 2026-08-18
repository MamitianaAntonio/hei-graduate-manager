package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.app.dto.course.CourseRequest;
import com.hei.app.dto.course.CourseResponse;
import com.hei.app.exceptions.DuplicateResourceException;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.mapper.CourseMapper;
import com.hei.app.model.Course;
import com.hei.app.repository.CourseRepository;
import com.hei.app.service.CourseService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {
  @Mock private CourseRepository courseRepository;

  @Mock private CourseMapper courseMapper;

  @InjectMocks private CourseService courseService;

  @Test
  void create_shouldReturnCourse() {
    CourseRequest request = mock(CourseRequest.class);
    Course course = new Course();
    Course savedCourse = new Course();
    CourseResponse response = mock(CourseResponse.class);

    when(request.ref()).thenReturn("PROG4");
    when(courseRepository.existsByRef("PROG4")).thenReturn(false);
    when(courseMapper.toEntity(request)).thenReturn(course);
    when(courseRepository.save(course)).thenReturn(savedCourse);
    when(courseMapper.toResponse(savedCourse)).thenReturn(response);

    CourseResponse result = courseService.create(request);

    assertEquals(response, result);

    verify(courseRepository).existsByRef("PROG4");
    verify(courseMapper).toEntity(request);
    verify(courseRepository).save(course);
    verify(courseMapper).toResponse(savedCourse);
  }

  @Test
  void create_shouldThrowWhenRefAlreadyExists() {
    CourseRequest request = mock(CourseRequest.class);

    when(request.ref()).thenReturn("PROG4");
    when(courseRepository.existsByRef("PROG4")).thenReturn(true);

    assertThrows(DuplicateResourceException.class, () -> courseService.create(request));

    verify(courseRepository).existsByRef("PROG4");
  }

  @Test
  void findById_shouldReturnCourse() {
    UUID id = UUID.randomUUID();
    Course course = new Course();
    CourseResponse response = mock(CourseResponse.class);

    when(courseRepository.findById(id)).thenReturn(Optional.of(course));
    when(courseMapper.toResponse(course)).thenReturn(response);

    CourseResponse result = courseService.findById(id);

    assertEquals(response, result);

    verify(courseRepository).findById(id);
    verify(courseMapper).toResponse(course);
  }

  @Test
  void findById_shouldThrowWhenCourseDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(courseRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> courseService.findById(id));
  }

  @Test
  void findByRef_shouldReturnCourse() {
    String ref = "PROG4";
    Course course = new Course();
    CourseResponse response = mock(CourseResponse.class);

    when(courseRepository.findByRef(ref)).thenReturn(Optional.of(course));
    when(courseMapper.toResponse(course)).thenReturn(response);

    CourseResponse result = courseService.findByRef(ref);

    assertEquals(response, result);

    verify(courseRepository).findByRef(ref);
    verify(courseMapper).toResponse(course);
  }

  @Test
  void findByRef_shouldThrowWhenCourseDoesNotExist() {
    String ref = "PROG4";
    when(courseRepository.findByRef(ref)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> courseService.findByRef(ref));
  }

  @Test
  void findAll_shouldReturnCourses() {
    Course course1 = new Course();
    course1.setRef("PROG1");
    Course course2 = new Course();
    course2.setRef("PROG2");
    CourseResponse response1 = mock(CourseResponse.class);
    CourseResponse response2 = mock(CourseResponse.class);

    when(courseRepository.findAll()).thenReturn(List.of(course1, course2));
    when(courseMapper.toResponse(course1)).thenReturn(response1);
    when(courseMapper.toResponse(course2)).thenReturn(response2);

    List<CourseResponse> result = courseService.findAll();

    assertEquals(List.of(response1, response2), result);

    verify(courseRepository).findAll();
    verify(courseMapper).toResponse(course1);
    verify(courseMapper).toResponse(course2);
  }

  @Test
  void update_shouldReturnUpdatedCourse() {
    UUID id = UUID.randomUUID();
    CourseRequest request = mock(CourseRequest.class);
    Course course = new Course();
    Course updatedCourse = new Course();
    CourseResponse response = mock(CourseResponse.class);

    when(request.ref()).thenReturn("PROG5");
    when(request.title()).thenReturn("Algorithmique");
    when(request.credits()).thenReturn(4);
    when(courseRepository.findById(id)).thenReturn(Optional.of(course));
    when(courseRepository.save(course)).thenReturn(updatedCourse);
    when(courseMapper.toResponse(updatedCourse)).thenReturn(response);

    CourseResponse result = courseService.update(id, request);

    assertEquals(response, result);

    verify(courseRepository).findById(id);
    verify(courseRepository).save(course);
    verify(courseMapper).toResponse(updatedCourse);
  }

  @Test
  void update_shouldThrowWhenCourseDoesNotExist() {
    UUID id = UUID.randomUUID();
    CourseRequest request = mock(CourseRequest.class);

    when(courseRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> courseService.update(id, request));
  }

  @Test
  void delete_shouldDeleteCourse() {
    UUID id = UUID.randomUUID();

    when(courseRepository.existsById(id)).thenReturn(true);
    courseService.delete(id);

    verify(courseRepository).existsById(id);
    verify(courseRepository).deleteById(id);
  }

  @Test
  void delete_shouldThrowWhenCourseDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(courseRepository.existsById(id)).thenReturn(false);
    assertThrows(ResourceNotFoundException.class, () -> courseService.delete(id));
    verify(courseRepository).existsById(id);
  }
}
