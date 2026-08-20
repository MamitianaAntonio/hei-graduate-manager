package com.hei.app.repository;

import com.hei.app.model.Student;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, UUID> {
  Optional<Student> findByStd(String std);

  Optional<Student> findByUserAccountId(UUID userAccountId);

  List<Student> findByPromotionId(UUID promotionId);

  boolean existsByStd(String std);
}
