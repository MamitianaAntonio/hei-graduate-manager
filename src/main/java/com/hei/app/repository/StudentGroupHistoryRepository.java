package com.hei.app.repository;

import com.hei.app.model.StudentGroupHistory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentGroupHistoryRepository extends JpaRepository<StudentGroupHistory, UUID> {
  List<StudentGroupHistory> findByStudentId(UUID studentId);

  Optional<StudentGroupHistory> findFirstByStudentIdAndEndDateIsNull(UUID studentId);
}
