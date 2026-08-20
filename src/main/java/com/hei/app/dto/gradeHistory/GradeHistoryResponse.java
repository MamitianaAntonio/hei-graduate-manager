package com.hei.app.dto.gradeHistory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GradeHistoryResponse(
    UUID id,
    UUID gradeId,
    BigDecimal oldValue,
    BigDecimal newValue,
    String reason,
    UUID modifiedById,
    Instant modifiedAt) {}
