package com.hei.app.repository;

import com.hei.app.model.Course;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, UUID> {
  Optional<Course> findByRef(String ref);

  boolean existsByRef(String ref);
}
