package com.hei.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "students",
    uniqueConstraints = {@UniqueConstraint(name = "uk_student_std", columnNames = "std")})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Student {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false, unique = true)
  private String std;

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = false)
  private String lastName;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "promotion_id", nullable = false)
  private Promotion promotion;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_account_id", nullable = false, unique = true)
  private UserAccount userAccount;
}
