package com.hei.app.dto.assignment;

import com.hei.app.model.Semester;
import java.util.UUID;

public record CourseAssignmentResponse(
    UUID id,
    UUID courseId,
    UUID teacherId,
    UUID groupId,
    Semester semester,
    Integer academicYear) {}
