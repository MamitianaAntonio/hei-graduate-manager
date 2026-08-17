package com.hei.app.dto.grade;

import java.math.BigDecimal;
import java.util.UUID;

public record GradeResponse(UUID id, UUID studentId, UUID examId, BigDecimal value) {}
