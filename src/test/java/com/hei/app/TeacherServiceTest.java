package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.app.dto.teacher.TeacherRequest;
import com.hei.app.dto.teacher.TeacherResponse;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.mapper.TeacherMapper;
import com.hei.app.model.Teacher;
import com.hei.app.repository.TeacherRepository;
import com.hei.app.service.TeacherService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TeacherServiceTest {
  @Mock private TeacherRepository teacherRepository;

  @Mock private TeacherMapper teacherMapper;

  @InjectMocks private TeacherService teacherService;

  @Test
  void create_shouldReturnTeacher() {
    TeacherRequest request = mock(TeacherRequest.class);
    Teacher teacher = new Teacher();
    Teacher savedTeacher = new Teacher();
    TeacherResponse response = mock(TeacherResponse.class);

    when(teacherMapper.toEntity(request)).thenReturn(teacher);
    when(teacherRepository.save(teacher)).thenReturn(savedTeacher);
    when(teacherMapper.toResponse(savedTeacher)).thenReturn(response);

    TeacherResponse result = teacherService.create(request);

    assertEquals(response, result);

    verify(teacherMapper).toEntity(request);
    verify(teacherRepository).save(teacher);
    verify(teacherMapper).toResponse(savedTeacher);
  }

  @Test
  void findById_shouldReturnTeacher() {
    UUID id = UUID.randomUUID();
    Teacher teacher = new Teacher();
    TeacherResponse response = mock(TeacherResponse.class);

    when(teacherRepository.findById(id)).thenReturn(Optional.of(teacher));
    when(teacherMapper.toResponse(teacher)).thenReturn(response);

    TeacherResponse result = teacherService.findById(id);

    assertEquals(response, result);

    verify(teacherRepository).findById(id);
    verify(teacherMapper).toResponse(teacher);
  }

  @Test
  void findById_shouldThrowWhenTeacherDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(teacherRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> teacherService.findById(id));
  }

  @Test
  void findAll_shouldReturnTeachers() {
    Teacher teacher1 = new Teacher();
    teacher1.setFirstName("Teacher1");
    Teacher teacher2 = new Teacher();
    teacher2.setFirstName("Teacher2");
    TeacherResponse response1 = mock(TeacherResponse.class);
    TeacherResponse response2 = mock(TeacherResponse.class);

    when(teacherRepository.findAll()).thenReturn(List.of(teacher1, teacher2));
    when(teacherMapper.toResponse(teacher1)).thenReturn(response1);
    when(teacherMapper.toResponse(teacher2)).thenReturn(response2);

    List<TeacherResponse> result = teacherService.findAll();

    assertEquals(List.of(response1, response2), result);

    verify(teacherRepository).findAll();
    verify(teacherMapper).toResponse(teacher1);
    verify(teacherMapper).toResponse(teacher2);
  }

  @Test
  void findByUserAccountId_shouldReturnTeacher() {
    UUID userAccountId = UUID.randomUUID();
    Teacher teacher = new Teacher();
    TeacherResponse response = mock(TeacherResponse.class);

    when(teacherRepository.findByUserAccountId(userAccountId)).thenReturn(Optional.of(teacher));
    when(teacherMapper.toResponse(teacher)).thenReturn(response);

    TeacherResponse result = teacherService.findByUserAccountId(userAccountId);

    assertEquals(response, result);

    verify(teacherRepository).findByUserAccountId(userAccountId);
    verify(teacherMapper).toResponse(teacher);
  }

  @Test
  void findByUserAccountId_shouldThrowWhenTeacherDoesNotExist() {
    UUID userAccountId = UUID.randomUUID();

    when(teacherRepository.findByUserAccountId(userAccountId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> teacherService.findByUserAccountId(userAccountId));
  }

  @Test
  void update_shouldReturnUpdatedTeacher() {
    UUID id = UUID.randomUUID();
    TeacherRequest request = mock(TeacherRequest.class);
    Teacher teacher = new Teacher();
    Teacher updatedTeacher = new Teacher();
    TeacherResponse response = mock(TeacherResponse.class);

    when(request.firstName()).thenReturn("Jean");
    when(request.lastName()).thenReturn("Dupont");
    when(teacherRepository.findById(id)).thenReturn(Optional.of(teacher));
    when(teacherRepository.save(teacher)).thenReturn(updatedTeacher);
    when(teacherMapper.toResponse(updatedTeacher)).thenReturn(response);

    TeacherResponse result = teacherService.update(id, request);

    assertEquals(response, result);

    verify(teacherRepository).findById(id);
    verify(teacherRepository).save(teacher);
    verify(teacherMapper).toResponse(updatedTeacher);
  }

  @Test
  void update_shouldThrowWhenTeacherDoesNotExist() {
    UUID id = UUID.randomUUID();
    TeacherRequest request = mock(TeacherRequest.class);

    when(teacherRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> teacherService.update(id, request));
  }

  @Test
  void delete_shouldDeleteTeacher() {
    UUID id = UUID.randomUUID();

    when(teacherRepository.existsById(id)).thenReturn(true);

    teacherService.delete(id);

    verify(teacherRepository).existsById(id);
    verify(teacherRepository).deleteById(id);
  }

  @Test
  void delete_shouldThrowWhenTeacherDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(teacherRepository.existsById(id)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> teacherService.delete(id));

    verify(teacherRepository).existsById(id);
  }
}
