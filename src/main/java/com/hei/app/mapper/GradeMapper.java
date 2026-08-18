package com.hei.app.mapper;

import com.hei.app.dto.grade.GradeRequest;
import com.hei.app.dto.grade.GradeResponse;
import com.hei.app.model.Grade;
import org.springframework.stereotype.Component;

@Component
public class GradeMapper {
  public Grade toEntity(GradeRequest request) {
    Grade grade = new Grade();
    grade.setValue(request.value());
    return grade;
  }

  public GradeResponse toResponse(Grade grade) {
    return new GradeResponse(
        grade.getId(), grade.getStudent().getId(), grade.getExam().getId(), grade.getValue());
  }
}
