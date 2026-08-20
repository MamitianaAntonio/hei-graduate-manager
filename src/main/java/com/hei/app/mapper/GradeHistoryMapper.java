package com.hei.app.mapper;

import com.hei.app.dto.gradeHistory.GradeHistoryResponse;
import com.hei.app.model.GradeHistory;
import org.springframework.stereotype.Component;

@Component
public class GradeHistoryMapper {
  public GradeHistoryResponse toResponse(GradeHistory history) {
    return new GradeHistoryResponse(
        history.getId(),
        history.getGrade().getId(),
        history.getOldValue(),
        history.getNewValue(),
        history.getReason(),
        history.getModifiedBy().getId(),
        history.getModifiedAt());
  }
}
