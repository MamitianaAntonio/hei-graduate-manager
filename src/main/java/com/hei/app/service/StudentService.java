package com.hei.app.service;

import com.hei.app.model.Student;
import com.hei.app.repository.StudentRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {
  private final StudentRepository studentRepository;

  public Student create(Student student) {
    if (studentRepository.existsById(student.getId())) {
      throw new IllegalArgumentException("STD already exists");
    }
    return studentRepository.save(student);
  }

  public Student findById(UUID id) {
    return studentRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Student not found"));
  }

  public Student findByStd(String std) {
    return studentRepository
        .findByStd(std)
        .orElseThrow(() -> new IllegalArgumentException("Student not found"));
  }
}
