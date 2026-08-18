package com.hei.app.service;

import com.hei.app.dto.student.StudentRequest;
import com.hei.app.dto.student.StudentResponse;
import com.hei.app.exceptions.DuplicateResourceException;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.mapper.StudentMapper;
import com.hei.app.model.Student;
import com.hei.app.repository.StudentRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {
  private final StudentRepository studentRepository;
  private final StudentMapper studentMapper;

  public StudentResponse create(StudentRequest request) {
    if (studentRepository.existsByStd(request.std())) {
      throw new DuplicateResourceException("Student already exists with std: " + request.std());
    }

    Student student = studentMapper.toEntity(request);
    Student savedStudent = studentRepository.save(student);
    return studentMapper.toResponse(savedStudent);
  }

  public StudentResponse findById(UUID id) {
    Student student =
        studentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    return studentMapper.toResponse(student);
  }

  public StudentResponse findByStd(String std) {
    Student student =
        studentRepository
            .findByStd(std)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with std: " + std));
    return studentMapper.toResponse(student);
  }

  public List<StudentResponse> findAll() {
    return studentRepository.findAll().stream().map(studentMapper::toResponse).toList();
  }

  public StudentResponse findByUserAccountId(UUID userAccountId) {
    Student student =
        studentRepository
            .findByUserAccountId(userAccountId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Student not found with user account id: " + userAccountId));

    return studentMapper.toResponse(student);
  }

  public StudentResponse update(UUID id, StudentRequest request) {
    Student student =
        studentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

    student.setStd(request.std());
    student.setFirstName(request.firstName());
    student.setLastName(request.lastName());

    Student updatedStudent = studentRepository.save(student);
    return studentMapper.toResponse(updatedStudent);
  }

  public void delete(UUID id) {
    if (!studentRepository.existsById(id)) {
      throw new ResourceNotFoundException("Student not found with id: " + id);
    }

    studentRepository.deleteById(id);
  }
}
