package com.hei.app.mapper;

import com.hei.app.dto.teacher.TeacherRequest;
import com.hei.app.dto.teacher.TeacherResponse;
import com.hei.app.model.Teacher;
import org.springframework.stereotype.Component;

@Component
public class TeacherMapper {
  public Teacher toEntity(TeacherRequest request) {
    Teacher teacher = new Teacher();

    teacher.setFirstName(request.firstName());
    teacher.setLastName(request.lastName());

    return teacher;
  }

  public TeacherResponse toResponse(Teacher teacher) {
    return new TeacherResponse(
        teacher.getId(),
        teacher.getFirstName(),
        teacher.getLastName(),
        teacher.getUserAccount().getId());
  }
}
