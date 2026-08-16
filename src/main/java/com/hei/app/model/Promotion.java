package com.hei.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "promotion",
    uniqueConstraints = {@UniqueConstraint(name = "uk_promotion_year", columnNames = "year")})
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class Promotion {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false, unique = true)
  private Integer year;
}
