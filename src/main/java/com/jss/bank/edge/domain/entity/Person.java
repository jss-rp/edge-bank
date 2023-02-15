package com.jss.bank.edge.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "people")
public class Person {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "first_name", length = 45)
  private String firstName;

  @Column(length = 45)
  private String surname;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  @Column(length = 15)
  private String document;

  @Column(name = "created_at")
  private LocalDateTime createdAt;
}
