package com.hei.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "groups")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Group {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false)
  private String ref;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Track track;
}
