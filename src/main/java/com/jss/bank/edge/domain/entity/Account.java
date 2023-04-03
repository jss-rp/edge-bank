package com.jss.bank.edge.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "accounts")
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  private String agency;

  @Column
  private String code;

  @Column(name = "dt_verifier")
  private String dtVerifier;

  @Column
  private String password;

  @Column
  private BigDecimal balance;

  @OneToOne
  @JoinColumn(name = "person_id")
  private Person person;
}
