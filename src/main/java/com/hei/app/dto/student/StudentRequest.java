package com.hei.app.dto.student;

import java.util.UUID;

public record StudentRequest(
    String std, String firstName, String lastName, UUID promotionId, UUID userAccountId) {}
