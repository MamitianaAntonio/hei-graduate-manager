package com.hei.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "courses",
    uniqueConstraints = {@UniqueConstraint(name = "uk_course_ref", columnNames = "ref")})
@Getter
@Setter
@NoArgsConstructor
public class Course {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false, unique = true)
  private String ref;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private Integer credits;
}
