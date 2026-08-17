package com.hei.app.dto.student;

import java.util.UUID;

public record StudentResponse(
    UUID id, String firstName, String lastName, UUID promotionId, UUID userAccountId) {}
