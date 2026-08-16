package com.hei.app.repository;

import com.hei.app.model.Grade;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<Grade, UUID> {
  List<Grade> findByStudentId(UUID studentId);

  List<Grade> findByExamId(UUID examId);

  Optional<Grade> findByStudentIdAndExamId(UUID studentId, UUID examId);

  boolean existsByStudentIdAndExamId(UUID studentId, UUID examId);
}
