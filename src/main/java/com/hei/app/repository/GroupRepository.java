package com.hei.app.repository;

import com.hei.app.model.Group;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, UUID> {
  Optional<Group> findByRef(String ref);

  boolean existsByRef(String ref);
}
