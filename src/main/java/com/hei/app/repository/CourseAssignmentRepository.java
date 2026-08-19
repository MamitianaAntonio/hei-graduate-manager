package com.hei.app.repository;

import com.hei.app.model.CourseAssignment;
import com.hei.app.model.Semester;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, UUID> {
  List<CourseAssignment> findByTeacherId(UUID teacherId);

  List<CourseAssignment> findByCourseId(UUID courseId);

  List<CourseAssignment> findByGroupId(UUID groupId);

  List<CourseAssignment> findBySemesterAndAcademicYear(Semester semester, Integer academicYear);

  boolean existsByTeacherIdAndCourseId(UUID teacherId, UUID courseId);
}
