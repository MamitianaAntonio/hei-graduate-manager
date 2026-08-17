package com.hei.app.dto.grade;

import java.math.BigDecimal;

public record GradeUpdateRequest(BigDecimal value, String reason) {}
