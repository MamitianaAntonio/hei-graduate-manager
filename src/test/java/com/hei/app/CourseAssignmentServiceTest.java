package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.hei.app.service.CourseAssignmentService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CourseAssignmentServiceTest {
  @Mock private CourseAssignmentRepository courseAssignmentRepository;

  @Mock private CourseAssignmentMapper courseAssignmentMapper;

  @Mock private CourseRepository courseRepository;

  @Mock private TeacherRepository teacherRepository;

  @Mock private GroupRepository groupRepository;

  @InjectMocks private CourseAssignmentService courseAssignmentService;

  @Test
  void create_shouldReturnCourseAssignment() {
    CourseAssignmentRequest request = mock(CourseAssignmentRequest.class);
    Course course = new Course();
    Teacher teacher = new Teacher();
    Group group = new Group();
    CourseAssignment assignment = new CourseAssignment();
    CourseAssignment savedAssignment = new CourseAssignment();
    CourseAssignmentResponse response = mock(CourseAssignmentResponse.class);

    when(request.teacherId()).thenReturn(UUID.randomUUID());
    when(request.courseId()).thenReturn(UUID.randomUUID());
    when(request.groupId()).thenReturn(UUID.randomUUID());
    when(courseAssignmentRepository.existsByTeacherIdAndCourseId(
            request.teacherId(), request.courseId()))
        .thenReturn(false);
    when(courseRepository.findById(request.courseId())).thenReturn(Optional.of(course));
    when(teacherRepository.findById(request.teacherId())).thenReturn(Optional.of(teacher));
    when(groupRepository.findById(request.groupId())).thenReturn(Optional.of(group));
    when(courseAssignmentMapper.toEntity(request)).thenReturn(assignment);
    when(courseAssignmentRepository.save(assignment)).thenReturn(savedAssignment);
    when(courseAssignmentMapper.toResponse(savedAssignment)).thenReturn(response);

    CourseAssignmentResponse result = courseAssignmentService.create(request);

    assertEquals(response, result);

    verify(courseAssignmentRepository)
        .existsByTeacherIdAndCourseId(request.teacherId(), request.courseId());
    verify(courseRepository).findById(request.courseId());
    verify(teacherRepository).findById(request.teacherId());
    verify(groupRepository).findById(request.groupId());
    verify(courseAssignmentMapper).toEntity(request);
    verify(courseAssignmentRepository).save(assignment);
    verify(courseAssignmentMapper).toResponse(savedAssignment);
  }

  @Test
  void create_shouldThrowWhenTeacherAlreadyAssignedToCourse() {
    CourseAssignmentRequest request = mock(CourseAssignmentRequest.class);

    when(request.teacherId()).thenReturn(UUID.randomUUID());
    when(request.courseId()).thenReturn(UUID.randomUUID());
    when(courseAssignmentRepository.existsByTeacherIdAndCourseId(
            request.teacherId(), request.courseId()))
        .thenReturn(true);

    assertThrows(DuplicateResourceException.class, () -> courseAssignmentService.create(request));

    verify(courseAssignmentRepository)
        .existsByTeacherIdAndCourseId(request.teacherId(), request.courseId());
  }

  @Test
  void findById_shouldReturnCourseAssignment() {
    UUID id = UUID.randomUUID();
    CourseAssignment assignment = new CourseAssignment();
    CourseAssignmentResponse response = mock(CourseAssignmentResponse.class);

    when(courseAssignmentRepository.findById(id)).thenReturn(Optional.of(assignment));
    when(courseAssignmentMapper.toResponse(assignment)).thenReturn(response);

    CourseAssignmentResponse result = courseAssignmentService.findById(id);

    assertEquals(response, result);

    verify(courseAssignmentRepository).findById(id);
    verify(courseAssignmentMapper).toResponse(assignment);
  }

  @Test
  void findById_shouldThrowWhenCourseAssignmentDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(courseAssignmentRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> courseAssignmentService.findById(id));
  }

  @Test
  void findAll_shouldReturnCourseAssignments() {
    CourseAssignment assignment1 = new CourseAssignment();
    assignment1.setAcademicYear(2025);
    CourseAssignment assignment2 = new CourseAssignment();
    assignment2.setAcademicYear(2026);
    CourseAssignmentResponse response1 = mock(CourseAssignmentResponse.class);
    CourseAssignmentResponse response2 = mock(CourseAssignmentResponse.class);

    when(courseAssignmentRepository.findAll()).thenReturn(List.of(assignment1, assignment2));
    when(courseAssignmentMapper.toResponse(assignment1)).thenReturn(response1);
    when(courseAssignmentMapper.toResponse(assignment2)).thenReturn(response2);

    List<CourseAssignmentResponse> result = courseAssignmentService.findAll();

    assertEquals(List.of(response1, response2), result);

    verify(courseAssignmentRepository).findAll();
    verify(courseAssignmentMapper).toResponse(assignment1);
    verify(courseAssignmentMapper).toResponse(assignment2);
  }

  @Test
  void findByTeacherId_shouldReturnCourseAssignments() {
    UUID teacherId = UUID.randomUUID();
    CourseAssignment assignment = new CourseAssignment();
    CourseAssignmentResponse response = mock(CourseAssignmentResponse.class);

    when(courseAssignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of(assignment));
    when(courseAssignmentMapper.toResponse(assignment)).thenReturn(response);

    List<CourseAssignmentResponse> result = courseAssignmentService.findByTeacherId(teacherId);

    assertEquals(List.of(response), result);

    verify(courseAssignmentRepository).findByTeacherId(teacherId);
    verify(courseAssignmentMapper).toResponse(assignment);
  }

  @Test
  void findByCourseId_shouldReturnCourseAssignments() {
    UUID courseId = UUID.randomUUID();
    CourseAssignment assignment = new CourseAssignment();
    CourseAssignmentResponse response = mock(CourseAssignmentResponse.class);

    when(courseAssignmentRepository.findByCourseId(courseId)).thenReturn(List.of(assignment));
    when(courseAssignmentMapper.toResponse(assignment)).thenReturn(response);

    List<CourseAssignmentResponse> result = courseAssignmentService.findByCourseId(courseId);

    assertEquals(List.of(response), result);

    verify(courseAssignmentRepository).findByCourseId(courseId);
    verify(courseAssignmentMapper).toResponse(assignment);
  }

  @Test
  void findByGroupId_shouldReturnCourseAssignments() {
    UUID groupId = UUID.randomUUID();
    CourseAssignment assignment = new CourseAssignment();
    CourseAssignmentResponse response = mock(CourseAssignmentResponse.class);

    when(courseAssignmentRepository.findByGroupId(groupId)).thenReturn(List.of(assignment));
    when(courseAssignmentMapper.toResponse(assignment)).thenReturn(response);

    List<CourseAssignmentResponse> result = courseAssignmentService.findByGroupId(groupId);

    assertEquals(List.of(response), result);

    verify(courseAssignmentRepository).findByGroupId(groupId);
    verify(courseAssignmentMapper).toResponse(assignment);
  }

  @Test
  void update_shouldReturnUpdatedCourseAssignment() {
    UUID id = UUID.randomUUID();
    CourseAssignmentRequest request = mock(CourseAssignmentRequest.class);
    Course course = new Course();
    Teacher teacher = new Teacher();
    Group group = new Group();
    CourseAssignment assignment = new CourseAssignment();
    CourseAssignment updatedAssignment = new CourseAssignment();
    CourseAssignmentResponse response = mock(CourseAssignmentResponse.class);

    when(request.teacherId()).thenReturn(UUID.randomUUID());
    when(request.courseId()).thenReturn(UUID.randomUUID());
    when(request.groupId()).thenReturn(UUID.randomUUID());
    when(courseAssignmentRepository.findById(id)).thenReturn(Optional.of(assignment));
    when(courseRepository.findById(request.courseId())).thenReturn(Optional.of(course));
    when(teacherRepository.findById(request.teacherId())).thenReturn(Optional.of(teacher));
    when(groupRepository.findById(request.groupId())).thenReturn(Optional.of(group));
    when(courseAssignmentRepository.save(assignment)).thenReturn(updatedAssignment);
    when(courseAssignmentMapper.toResponse(updatedAssignment)).thenReturn(response);

    CourseAssignmentResponse result = courseAssignmentService.update(id, request);

    assertEquals(response, result);

    verify(courseAssignmentRepository).findById(id);
    verify(courseRepository).findById(request.courseId());
    verify(teacherRepository).findById(request.teacherId());
    verify(groupRepository).findById(request.groupId());
    verify(courseAssignmentRepository).save(assignment);
    verify(courseAssignmentMapper).toResponse(updatedAssignment);
  }

  @Test
  void update_shouldThrowWhenCourseAssignmentDoesNotExist() {
    UUID id = UUID.randomUUID();
    CourseAssignmentRequest request = mock(CourseAssignmentRequest.class);

    when(courseAssignmentRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> courseAssignmentService.update(id, request));
  }

  @Test
  void delete_shouldDeleteCourseAssignment() {
    UUID id = UUID.randomUUID();

    when(courseAssignmentRepository.existsById(id)).thenReturn(true);

    courseAssignmentService.delete(id);

    verify(courseAssignmentRepository).existsById(id);
    verify(courseAssignmentRepository).deleteById(id);
  }

  @Test
  void delete_shouldThrowWhenCourseAssignmentDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(courseAssignmentRepository.existsById(id)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> courseAssignmentService.delete(id));
    verify(courseAssignmentRepository).existsById(id);
  }
}
