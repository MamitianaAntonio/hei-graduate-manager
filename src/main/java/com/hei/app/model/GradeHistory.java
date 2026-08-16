package com.hei.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "grade_histories")
@Getter
@Setter
@NoArgsConstructor
public class GradeHistory {
  @Id @GeneratedValue private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "grade_id", nullable = false)
  private Grade grade;

  @Column(nullable = false, precision = 5, scale = 2)
  private BigDecimal oldValue;

  @Column(nullable = false, precision = 5, scale = 2)
  private BigDecimal newValue;

  @Column(nullable = false, length = 500)
  private String reason;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "modified_by_id", nullable = false)
  private UserAccount modifiedBy;

  @Column(nullable = false)
  private Instant modifiedAt;
}
