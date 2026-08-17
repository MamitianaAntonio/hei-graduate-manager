package com.hei.app.dto.teacher;

import java.util.UUID;

public record TeacherRequest(String firstName, String lastName, UUID userAccountId) {}
