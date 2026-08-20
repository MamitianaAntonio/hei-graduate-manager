package com.hei.app.service;

import com.hei.app.dto.gradeHistory.GradeHistoryResponse;
import com.hei.app.mapper.GradeHistoryMapper;
import com.hei.app.repository.GradeHistoryRepository;
import com.hei.app.security.CurrentUser;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeHistoryService {
  private final GradeHistoryRepository gradeHistoryRepository;
  private final GradeHistoryMapper gradeHistoryMapper;

  public List<GradeHistoryResponse> findByGradeId(UUID gradeId, CurrentUser currentUser) {
    return gradeHistoryRepository.findByGradeIdOrderByModifiedAtDesc(gradeId).stream()
        .map(gradeHistoryMapper::toResponse)
        .toList();
  }
}
