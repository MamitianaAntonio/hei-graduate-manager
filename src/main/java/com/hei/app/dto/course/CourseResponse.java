package com.hei.app.dto.course;

import java.util.UUID;

public record CourseResponse(UUID id, String ref, String title, Integer credits) {}
