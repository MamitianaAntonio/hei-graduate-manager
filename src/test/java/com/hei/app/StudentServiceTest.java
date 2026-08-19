package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.app.dto.student.StudentRequest;
import com.hei.app.dto.student.StudentResponse;
import com.hei.app.exceptions.DuplicateResourceException;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.mapper.StudentMapper;
import com.hei.app.model.Role;
import com.hei.app.model.Student;
import com.hei.app.repository.StudentRepository;
import com.hei.app.security.CurrentUser;
import com.hei.app.service.SecurityAsserts;
import com.hei.app.service.StudentService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {
  @Mock private StudentRepository studentRepository;

  @Mock private StudentMapper studentMapper;

  @Mock private SecurityAsserts securityAsserts;

  @InjectMocks private StudentService studentService;

  @Test
  void create_shouldReturnStudent() {
    StudentRequest request = mock(StudentRequest.class);
    Student student = new Student();
    Student savedStudent = new Student();
    StudentResponse response = mock(StudentResponse.class);

    when(request.std()).thenReturn("STD24191");
    when(studentRepository.existsByStd("STD24191")).thenReturn(false);
    when(studentMapper.toEntity(request)).thenReturn(student);
    when(studentRepository.save(student)).thenReturn(savedStudent);
    when(studentMapper.toResponse(savedStudent)).thenReturn(response);

    StudentResponse result = studentService.create(request, admin());

    assertEquals(response, result);
  }

  @Test
  void create_shouldThrowWhenStdAlreadyExists() {
    StudentRequest request = mock(StudentRequest.class);

    when(request.std()).thenReturn("STD24191");
    when(studentRepository.existsByStd("STD24191")).thenReturn(true);

    assertThrows(DuplicateResourceException.class, () -> studentService.create(request, admin()));
  }

  @Test
  void student_cannotCreateStudent() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> studentService.create(mock(StudentRequest.class), student(UUID.randomUUID())));
  }

  @Test
  void teacher_cannotCreateStudent() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> studentService.create(mock(StudentRequest.class), teacher(UUID.randomUUID())));
  }

  @Test
  void findById_shouldReturnStudent() {
    UUID id = UUID.randomUUID();
    Student student = new Student();
    StudentResponse response = mock(StudentResponse.class);

    when(studentRepository.findById(id)).thenReturn(Optional.of(student));
    when(studentMapper.toResponse(student)).thenReturn(response);

    StudentResponse result = studentService.findById(id, admin());

    assertEquals(response, result);
  }

  @Test
  void findById_shouldThrowWhenStudentDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(studentRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> studentService.findById(id, admin()));
  }

  @Test
  void student_canReadOwnProfile() {
    UUID studentId = UUID.randomUUID();
    CurrentUser currentUser = student(studentId);
    Student student = new Student();
    StudentResponse response = mock(StudentResponse.class);

    when(securityAsserts.requireStudentId(currentUser)).thenReturn(studentId);
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(studentMapper.toResponse(student)).thenReturn(response);

    StudentResponse result = studentService.findById(studentId, currentUser);

    assertEquals(response, result);
  }

  @Test
  void findByStd_shouldReturnStudent() {
    String std = "STD24191";
    Student student = new Student();
    StudentResponse response = mock(StudentResponse.class);

    when(studentRepository.findByStd(std)).thenReturn(Optional.of(student));
    when(studentMapper.toResponse(student)).thenReturn(response);

    StudentResponse result = studentService.findByStd(std, admin());

    assertEquals(response, result);
  }

  @Test
  void findByStd_shouldThrowWhenStudentDoesNotExist() {
    String std = "STD24191";

    when(studentRepository.findByStd(std)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> studentService.findByStd(std, admin()));
  }

  @Test
  void student_cannotLookUpByStd() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> studentService.findByStd("STD24191", student(UUID.randomUUID())));
  }

  @Test
  void findAll_shouldReturnStudents() {
    Student student1 = new Student();
    student1.setStd("STD1");
    Student student2 = new Student();
    student2.setStd("STD2");
    StudentResponse response1 = mock(StudentResponse.class);
    StudentResponse response2 = mock(StudentResponse.class);

    when(studentRepository.findAll()).thenReturn(List.of(student1, student2));
    when(studentMapper.toResponse(student1)).thenReturn(response1);
    when(studentMapper.toResponse(student2)).thenReturn(response2);

    List<StudentResponse> result = studentService.findAll(admin());

    assertEquals(List.of(response1, response2), result);
  }

  @Test
  void student_cannotListAllStudents() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> studentService.findAll(student(UUID.randomUUID())));
  }

  @Test
  void findByUserAccountId_shouldReturnStudent() {
    UUID userAccountId = UUID.randomUUID();
    Student student = new Student();
    StudentResponse response = mock(StudentResponse.class);

    when(studentRepository.findByUserAccountId(userAccountId)).thenReturn(Optional.of(student));
    when(studentMapper.toResponse(student)).thenReturn(response);

    StudentResponse result = studentService.findByUserAccountId(userAccountId, admin());

    assertEquals(response, result);
  }

  @Test
  void findByUserAccountId_shouldThrowWhenStudentDoesNotExist() {
    UUID userAccountId = UUID.randomUUID();

    when(studentRepository.findByUserAccountId(userAccountId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> studentService.findByUserAccountId(userAccountId, admin()));
  }

  @Test
  void student_canReadOwnProfileByUserAccount() {
    UUID accountId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    Student student = new Student();
    StudentResponse response = mock(StudentResponse.class);

    when(studentRepository.findByUserAccountId(accountId)).thenReturn(Optional.of(student));
    when(studentMapper.toResponse(student)).thenReturn(response);

    StudentResponse result =
        studentService.findByUserAccountId(
            accountId, new CurrentUser(accountId, Role.STUDENT, studentId, null));

    assertEquals(response, result);
  }

  @Test
  void update_shouldUpdateStudent() {
    UUID id = UUID.randomUUID();
    StudentRequest request = mock(StudentRequest.class);
    Student student = new Student();
    Student updatedStudent = new Student();
    StudentResponse response = mock(StudentResponse.class);

    when(request.std()).thenReturn("STD24191");
    when(request.firstName()).thenReturn("John");
    when(request.lastName()).thenReturn("Doe");
    when(studentRepository.findById(id)).thenReturn(Optional.of(student));
    when(studentRepository.save(student)).thenReturn(updatedStudent);
    when(studentMapper.toResponse(updatedStudent)).thenReturn(response);

    StudentResponse result = studentService.update(id, request, admin());

    assertEquals(response, result);
  }

  @Test
  void update_shouldThrowWhenStudentDoesNotExist() {
    UUID id = UUID.randomUUID();
    StudentRequest request = mock(StudentRequest.class);

    when(studentRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> studentService.update(id, request, admin()));
  }

  @Test
  void teacher_cannotUpdateStudent() {
    assertThrows(
        UnauthorizedActionException.class,
        () ->
            studentService.update(
                UUID.randomUUID(), mock(StudentRequest.class), teacher(UUID.randomUUID())));
  }

  @Test
  void delete_shouldDeleteStudent() {
    UUID id = UUID.randomUUID();

    when(studentRepository.existsById(id)).thenReturn(true);

    studentService.delete(id, admin());

    verify(studentRepository).deleteById(id);
  }

  @Test
  void delete_shouldThrowWhenStudentDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(studentRepository.existsById(id)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> studentService.delete(id, admin()));
  }

  @Test
  void student_cannotDeleteStudent() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> studentService.delete(UUID.randomUUID(), student(UUID.randomUUID())));
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
}
