package com.hei.app.repository;

import com.hei.app.model.Exam;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, UUID> {
  List<Exam> findByCourseId(UUID courseId);
}
