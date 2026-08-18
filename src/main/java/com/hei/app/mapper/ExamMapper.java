package com.hei.app.mapper;

import com.hei.app.dto.exam.ExamRequest;
import com.hei.app.dto.exam.ExamResponse;
import com.hei.app.model.Exam;
import org.springframework.stereotype.Component;

@Component
public class ExamMapper {
  public Exam toEntity(ExamRequest request) {
    Exam exam = new Exam();
    exam.setDateExam(request.dateExam());
    exam.setCoefficient(request.coefficient());
    return exam;
  }

  public ExamResponse toResponse(Exam exam) {
    return new ExamResponse(
        exam.getId(), exam.getDateExam(), exam.getCoefficient(), exam.getCourse().getId());
  }
}