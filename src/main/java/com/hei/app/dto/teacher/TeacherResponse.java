package com.hei.app.dto.teacher;

import java.util.UUID;

public record TeacherResponse(UUID id, String firstName, String lastName, UUID userAccountId) {}
