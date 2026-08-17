package com.hei.app.mapper;

import com.hei.app.dto.student.StudentRequest;
import com.hei.app.dto.student.StudentResponse;
import com.hei.app.model.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {
  public Student toEntity(StudentRequest request) {
    Student student = new Student();

    student.setStd(request.std());
    student.setFirstName(request.firstName());
    student.setLastName(request.lastName());

    return student;
  }

  public StudentResponse toResponse(Student student) {
    return new StudentResponse(
        student.getId(),
        student.getStd(),
        student.getFirstName(),
        student.getLastName(),
        student.getPromotion().getId(),
        student.getUserAccount().getId());
  }
}
