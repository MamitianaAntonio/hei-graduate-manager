package com.hei.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "grades",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_grade_student_exam",
          columnNames = {"student_id", "exam_id"})
    })
@Getter
@Setter
@NoArgsConstructor
public class Grade {
  @Id @GeneratedValue private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "exam_id", nullable = false)
  private Exam exam;

  @Column(nullable = false, precision = 5, scale = 2)
  private BigDecimal value;
}
