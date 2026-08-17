package com.hei.app.dto.exam;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ExamRequest(Instant dateExam, BigDecimal coefficient, UUID courseId) {}
