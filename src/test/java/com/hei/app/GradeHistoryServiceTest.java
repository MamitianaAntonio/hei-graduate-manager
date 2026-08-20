package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.hei.app.dto.gradeHistory.GradeHistoryResponse;
import com.hei.app.mapper.GradeHistoryMapper;
import com.hei.app.model.Grade;
import com.hei.app.model.GradeHistory;
import com.hei.app.model.UserAccount;
import com.hei.app.repository.GradeHistoryRepository;
import com.hei.app.security.CurrentUser;
import com.hei.app.service.GradeHistoryService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GradeHistoryServiceTest {
  @Mock private GradeHistoryRepository gradeHistoryRepository;

  @Mock private GradeHistoryMapper gradeHistoryMapper;

  @InjectMocks private GradeHistoryService gradeHistoryService;

  @Test
  void findByGradeId_shouldReturnHistory() {
    UUID gradeId = UUID.randomUUID();
    Grade grade = new Grade();
    grade.setId(gradeId);
    UserAccount userAccount = new UserAccount();

    GradeHistory history = new GradeHistory();
    history.setGrade(grade);
    history.setOldValue(BigDecimal.TEN);
    history.setNewValue(BigDecimal.valueOf(15));
    history.setReason("Correction");
    history.setModifiedBy(userAccount);
    history.setModifiedAt(Instant.now());

    GradeHistoryResponse response =
        new GradeHistoryResponse(
            UUID.randomUUID(),
            gradeId,
            BigDecimal.TEN,
            BigDecimal.valueOf(15),
            "Correction",
            userAccount.getId(),
            history.getModifiedAt());

    when(gradeHistoryRepository.findByGradeIdOrderByModifiedAtDesc(gradeId))
        .thenReturn(List.of(history));
    when(gradeHistoryMapper.toResponse(history)).thenReturn(response);

    CurrentUser admin =
        new CurrentUser(UUID.randomUUID(), com.hei.app.model.Role.ADMIN, null, null);
    List<GradeHistoryResponse> result = gradeHistoryService.findByGradeId(gradeId, admin);

    assertEquals(1, result.size());
    assertEquals(response, result.get(0));
  }

  @Test
  void findByGradeId_shouldReturnEmptyListWhenNoHistory() {
    UUID gradeId = UUID.randomUUID();

    when(gradeHistoryRepository.findByGradeIdOrderByModifiedAtDesc(gradeId)).thenReturn(List.of());

    CurrentUser admin =
        new CurrentUser(UUID.randomUUID(), com.hei.app.model.Role.ADMIN, null, null);
    List<GradeHistoryResponse> result = gradeHistoryService.findByGradeId(gradeId, admin);

    assertEquals(0, result.size());
  }
}
