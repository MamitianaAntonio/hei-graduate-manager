package com.hei.app.mapper;

import com.hei.app.dto.grade.GradeResponse;
import com.hei.app.model.Grade;
import org.springframework.stereotype.Component;

@Component
public class GradeMapper {
  public GradeResponse toResponse(Grade grade) {
    return new GradeResponse(
        grade.getId(), grade.getStudent().getId(), grade.getExam().getId(), grade.getValue());
  }
}
