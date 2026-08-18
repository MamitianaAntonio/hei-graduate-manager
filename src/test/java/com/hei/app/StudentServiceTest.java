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
import com.hei.app.mapper.StudentMapper;
import com.hei.app.model.Student;
import com.hei.app.repository.StudentRepository;
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

    StudentResponse result = studentService.create(request);

    assertEquals(response, result);

    verify(studentRepository).existsByStd("STD24191");
    verify(studentMapper).toEntity(request);
    verify(studentRepository).save(student);
    verify(studentMapper).toResponse(savedStudent);
  }

  @Test
  void create_shouldThrowWhenStdAlreadyExists() {
    StudentRequest request = mock(StudentRequest.class);

    when(request.std()).thenReturn("STD24191");
    when(studentRepository.existsByStd("STD24191")).thenReturn(true);

    assertThrows(DuplicateResourceException.class, () -> studentService.create(request));

    verify(studentRepository).existsByStd("STD24191");
  }

  @Test
  void findById_shouldReturnStudent() {
    UUID id = UUID.randomUUID();
    Student student = new Student();
    StudentResponse response = mock(StudentResponse.class);

    when(studentRepository.findById(id)).thenReturn(Optional.of(student));
    when(studentMapper.toResponse(student)).thenReturn(response);

    StudentResponse result = studentService.findById(id);

    assertEquals(response, result);

    verify(studentRepository).findById(id);
    verify(studentMapper).toResponse(student);
  }

  @Test
  void findById_shouldThrowWhenStudentDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(studentRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> studentService.findById(id));
  }

  @Test
  void findByStd_shouldReturnStudent() {
    String std = "STD24191";
    Student student = new Student();
    StudentResponse response = mock(StudentResponse.class);

    when(studentRepository.findByStd(std)).thenReturn(Optional.of(student));
    when(studentMapper.toResponse(student)).thenReturn(response);

    StudentResponse result = studentService.findByStd(std);

    assertEquals(response, result);

    verify(studentRepository).findByStd(std);
    verify(studentMapper).toResponse(student);
  }

  @Test
  void findByStd_shouldThrowWhenStudentDoesNotExist() {
    String std = "STD24191";

    when(studentRepository.findByStd(std)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> studentService.findByStd(std));
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

    List<StudentResponse> result = studentService.findAll();

    assertEquals(List.of(response1, response2), result);

    verify(studentRepository).findAll();
    verify(studentMapper).toResponse(student1);
    verify(studentMapper).toResponse(student2);
  }

  @Test
  void findByUserAccountId_shouldReturnStudent() {
    UUID userAccountId = UUID.randomUUID();
    Student student = new Student();
    StudentResponse response = mock(StudentResponse.class);

    when(studentRepository.findByUserAccountId(userAccountId)).thenReturn(Optional.of(student));
    when(studentMapper.toResponse(student)).thenReturn(response);

    StudentResponse result = studentService.findByUserAccountId(userAccountId);

    assertEquals(response, result);

    verify(studentRepository).findByUserAccountId(userAccountId);
    verify(studentMapper).toResponse(student);
  }

  @Test
  void findByUserAccountId_shouldThrowWhenStudentDoesNotExist() {
    UUID userAccountId = UUID.randomUUID();

    when(studentRepository.findByUserAccountId(userAccountId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> studentService.findByUserAccountId(userAccountId));
  }

  @Test
  void delete_shouldDeleteStudent() {
    UUID id = UUID.randomUUID();

    when(studentRepository.existsById(id)).thenReturn(true);

    studentService.delete(id);

    verify(studentRepository).existsById(id);
    verify(studentRepository).deleteById(id);
  }

  @Test
  void delete_shouldThrowWhenStudentDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(studentRepository.existsById(id)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> studentService.delete(id));

    verify(studentRepository).existsById(id);
  }
}
