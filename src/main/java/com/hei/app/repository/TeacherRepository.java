package com.hei.app.repository;

import com.hei.app.model.Teacher;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, UUID> {
  Optional<Teacher> findByUserAccountId(UUID userAccountId);
}
