package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.hei.app.service.CourseAssignmentService;
import com.hei.app.service.SecurityAsserts;
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

  @Mock private SecurityAsserts securityAsserts;

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

    CourseAssignmentResponse result = courseAssignmentService.create(request, admin());

    assertEquals(response, result);
  }

  @Test
  void create_shouldThrowWhenTeacherAlreadyAssignedToCourse() {
    CourseAssignmentRequest request = mock(CourseAssignmentRequest.class);

    when(request.teacherId()).thenReturn(UUID.randomUUID());
    when(request.courseId()).thenReturn(UUID.randomUUID());
    when(courseAssignmentRepository.existsByTeacherIdAndCourseId(
            request.teacherId(), request.courseId()))
        .thenReturn(true);

    assertThrows(
        DuplicateResourceException.class, () -> courseAssignmentService.create(request, admin()));
  }

  @Test
  void teacher_cannotCreateCourseAssignment() {
    assertThrows(
        UnauthorizedActionException.class,
        () ->
            courseAssignmentService.create(
                mock(CourseAssignmentRequest.class), teacher(UUID.randomUUID())));
  }

  @Test
  void findById_shouldReturnCourseAssignment() {
    UUID id = UUID.randomUUID();
    CourseAssignment assignment = new CourseAssignment();
    CourseAssignmentResponse response = mock(CourseAssignmentResponse.class);

    when(courseAssignmentRepository.findById(id)).thenReturn(Optional.of(assignment));
    when(courseAssignmentMapper.toResponse(assignment)).thenReturn(response);

    CourseAssignmentResponse result = courseAssignmentService.findById(id, admin());

    assertEquals(response, result);
  }

  @Test
  void findById_shouldThrowWhenCourseAssignmentDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(courseAssignmentRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> courseAssignmentService.findById(id, admin()));
  }

  @Test
  void teacher_canReadOwnAssignment() {
    UUID id = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    CurrentUser currentUser = teacher(teacherId);
    Teacher teacher = teacherWith(teacherId);
    CourseAssignment assignment = assignmentWith(teacher);
    CourseAssignmentResponse response = mock(CourseAssignmentResponse.class);

    when(securityAsserts.requireTeacherId(currentUser)).thenReturn(teacherId);
    when(courseAssignmentRepository.findById(id)).thenReturn(Optional.of(assignment));
    when(courseAssignmentMapper.toResponse(assignment)).thenReturn(response);

    CourseAssignmentResponse result = courseAssignmentService.findById(id, currentUser);

    assertEquals(response, result);
  }

  @Test
  void teacher_cannotReadAnotherTeacherAssignment() {
    UUID id = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    CurrentUser currentUser = teacher(teacherId);
    CourseAssignment assignment = assignmentWith(teacherWith(UUID.randomUUID()));

    when(securityAsserts.requireTeacherId(currentUser)).thenReturn(teacherId);
    when(courseAssignmentRepository.findById(id)).thenReturn(Optional.of(assignment));

    assertThrows(
        UnauthorizedActionException.class, () -> courseAssignmentService.findById(id, currentUser));
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

    List<CourseAssignmentResponse> result = courseAssignmentService.findAll(admin());

    assertEquals(List.of(response1, response2), result);
  }

  @Test
  void teacher_findAll_shouldReturnOnlyOwnAssignments() {
    UUID teacherId = UUID.randomUUID();
    CurrentUser currentUser = teacher(teacherId);
    CourseAssignment own = assignmentWith(teacherWith(teacherId));
    CourseAssignment other = assignmentWith(teacherWith(UUID.randomUUID()));
    CourseAssignmentResponse response = mock(CourseAssignmentResponse.class);

    when(securityAsserts.requireTeacherId(currentUser)).thenReturn(teacherId);
    when(courseAssignmentRepository.findAll()).thenReturn(List.of(own, other));
    when(courseAssignmentMapper.toResponse(own)).thenReturn(response);

    List<CourseAssignmentResponse> result = courseAssignmentService.findAll(currentUser);

    assertEquals(List.of(response), result);
    verify(courseAssignmentMapper, never()).toResponse(other);
  }

  @Test
  void student_cannotListAssignments() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> courseAssignmentService.findAll(student(UUID.randomUUID())));
  }

  @Test
  void findByTeacherId_shouldReturnCourseAssignments() {
    UUID teacherId = UUID.randomUUID();
    CourseAssignment assignment = new CourseAssignment();
    CourseAssignmentResponse response = mock(CourseAssignmentResponse.class);

    when(courseAssignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of(assignment));
    when(courseAssignmentMapper.toResponse(assignment)).thenReturn(response);

    List<CourseAssignmentResponse> result =
        courseAssignmentService.findByTeacherId(teacherId, admin());

    assertEquals(List.of(response), result);
  }

  @Test
  void teacher_findByTeacherId_shouldForceOwnId() {
    UUID teacherId = UUID.randomUUID();
    CurrentUser currentUser = teacher(teacherId);
    CourseAssignment assignment = new CourseAssignment();
    CourseAssignmentResponse response = mock(CourseAssignmentResponse.class);

    when(securityAsserts.requireTeacherId(currentUser)).thenReturn(teacherId);
    when(courseAssignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of(assignment));
    when(courseAssignmentMapper.toResponse(assignment)).thenReturn(response);

    List<CourseAssignmentResponse> result =
        courseAssignmentService.findByTeacherId(UUID.randomUUID(), currentUser);

    assertEquals(List.of(response), result);
    verify(courseAssignmentRepository).findByTeacherId(teacherId);
  }

  @Test
  void findByCourseId_shouldReturnCourseAssignments() {
    UUID courseId = UUID.randomUUID();
    CourseAssignment assignment = new CourseAssignment();
    CourseAssignmentResponse response = mock(CourseAssignmentResponse.class);

    when(courseAssignmentRepository.findByCourseId(courseId)).thenReturn(List.of(assignment));
    when(courseAssignmentMapper.toResponse(assignment)).thenReturn(response);

    List<CourseAssignmentResponse> result =
        courseAssignmentService.findByCourseId(courseId, admin());

    assertEquals(List.of(response), result);
  }

  @Test
  void teacher_findByCourseId_shouldReturnOnlyOwnAssignments() {
    UUID courseId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    CurrentUser currentUser = teacher(teacherId);
    CourseAssignment own = assignmentWith(teacherWith(teacherId));
    CourseAssignment other = assignmentWith(teacherWith(UUID.randomUUID()));
    CourseAssignmentResponse response = mock(CourseAssignmentResponse.class);

    when(securityAsserts.requireTeacherId(currentUser)).thenReturn(teacherId);
    when(courseAssignmentRepository.findByCourseId(courseId)).thenReturn(List.of(own, other));
    when(courseAssignmentMapper.toResponse(own)).thenReturn(response);

    List<CourseAssignmentResponse> result =
        courseAssignmentService.findByCourseId(courseId, currentUser);

    assertEquals(List.of(response), result);
    verify(courseAssignmentMapper, never()).toResponse(other);
  }

  @Test
  void findByGroupId_shouldReturnCourseAssignments() {
    UUID groupId = UUID.randomUUID();
    CourseAssignment assignment = new CourseAssignment();
    CourseAssignmentResponse response = mock(CourseAssignmentResponse.class);

    when(courseAssignmentRepository.findByGroupId(groupId)).thenReturn(List.of(assignment));
    when(courseAssignmentMapper.toResponse(assignment)).thenReturn(response);

    List<CourseAssignmentResponse> result = courseAssignmentService.findByGroupId(groupId, admin());

    assertEquals(List.of(response), result);
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

    CourseAssignmentResponse result = courseAssignmentService.update(id, request, admin());

    assertEquals(response, result);
  }

  @Test
  void update_shouldThrowWhenCourseAssignmentDoesNotExist() {
    UUID id = UUID.randomUUID();
    CourseAssignmentRequest request = mock(CourseAssignmentRequest.class);

    when(courseAssignmentRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> courseAssignmentService.update(id, request, admin()));
  }

  @Test
  void teacher_cannotUpdateCourseAssignment() {
    assertThrows(
        UnauthorizedActionException.class,
        () ->
            courseAssignmentService.update(
                UUID.randomUUID(),
                mock(CourseAssignmentRequest.class),
                teacher(UUID.randomUUID())));
  }

  @Test
  void delete_shouldDeleteCourseAssignment() {
    UUID id = UUID.randomUUID();

    when(courseAssignmentRepository.existsById(id)).thenReturn(true);

    courseAssignmentService.delete(id, admin());

    verify(courseAssignmentRepository).deleteById(id);
  }

  @Test
  void delete_shouldThrowWhenCourseAssignmentDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(courseAssignmentRepository.existsById(id)).thenReturn(false);

    assertThrows(
        ResourceNotFoundException.class, () -> courseAssignmentService.delete(id, admin()));
  }

  @Test
  void teacher_cannotDeleteCourseAssignment() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> courseAssignmentService.delete(UUID.randomUUID(), teacher(UUID.randomUUID())));
  }

  private CurrentUser admin() {
    return new CurrentUser(UUID.randomUUID(), Role.ADMIN, null, null);
  }

  private CurrentUser student(UUID studentId) {
    return new CurrentUser(UUID.randomUUID(), Role.STUDENT, studentId, null);
  }

  private CurrentUser teacher(UUID teacherId) {
    return new CurrentUser(UUID.randomUUID(), Role.TEACHER, null, teacherId);
  }

  private Teacher teacherWith(UUID id) {
    Teacher teacher = new Teacher();
    teacher.setId(id);
    return teacher;
  }

  private CourseAssignment assignmentWith(Teacher teacher) {
    CourseAssignment assignment = new CourseAssignment();
    assignment.setTeacher(teacher);
    return assignment;
  }
}
