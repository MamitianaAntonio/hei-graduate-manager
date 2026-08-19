package com.hei.app.service;

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
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {
  private final StudentRepository studentRepository;
  private final StudentMapper studentMapper;
  private final SecurityAsserts securityAsserts;

  @Transactional
  public StudentResponse create(StudentRequest request, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can create students");
    }

    if (studentRepository.existsByStd(request.std())) {
      throw new DuplicateResourceException("Student already exists with std: " + request.std());
    }

    Student student = studentMapper.toEntity(request);
    Student savedStudent = studentRepository.save(student);
    return studentMapper.toResponse(savedStudent);
  }

  public StudentResponse findById(UUID requestedId, CurrentUser currentUser) {
    UUID id = requestedId;

    if (currentUser.role() == Role.STUDENT) {
      id = securityAsserts.requireStudentId(currentUser);
    }

    UUID finalId = id;
    Student student =
        studentRepository
            .findById(finalId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Student not found with id: " + finalId));
    return studentMapper.toResponse(student);
  }

  public StudentResponse findByStd(String std, CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Student cannot look up students by std");
    }

    Student student =
        studentRepository
            .findByStd(std)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with std: " + std));
    return studentMapper.toResponse(student);
  }

  public List<StudentResponse> findAll(CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Student cannot list all students");
    }

    return studentRepository.findAll().stream().map(studentMapper::toResponse).toList();
  }

  public StudentResponse findByUserAccountId(UUID requestedUserAccountId, CurrentUser currentUser) {
    UUID userAccountId = requestedUserAccountId;

    if (currentUser.role() == Role.STUDENT) {
      userAccountId = currentUser.accountId();
    }

    UUID finalUserAccountId = userAccountId;
    Student student =
        studentRepository
            .findByUserAccountId(finalUserAccountId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Student not found with user account id: " + finalUserAccountId));

    return studentMapper.toResponse(student);
  }

  @Transactional
  public StudentResponse update(UUID id, StudentRequest request, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can update students");
    }

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

  @Transactional
  public void delete(UUID id, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can delete students");
    }

    if (!studentRepository.existsById(id)) {
      throw new ResourceNotFoundException("Student not found with id: " + id);
    }

    studentRepository.deleteById(id);
  }
}
