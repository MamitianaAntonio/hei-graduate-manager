package com.hei.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "teachers")
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class Teacher {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = false)
  private String lastName;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_account_id", nullable = false, unique = true)
  private UserAccount userAccount;
}
