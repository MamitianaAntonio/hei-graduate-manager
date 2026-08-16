package com.hei.app.repository;

import com.hei.app.model.Promotion;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
  Optional<Promotion> findByYear(Integer year);

  boolean existsByYear(Integer year);
}
