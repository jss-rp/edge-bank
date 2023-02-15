package com.jss.bank.edge.domain.entity;

import com.jss.bank.edge.security.entity.User;
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

  @Column
  private String dtVerifier;

  @Column
  private BigDecimal balance;

  @OneToOne
  @JoinColumn(name = "username")
  private User user;

  @OneToOne
  @JoinColumn(name = "person_id")
  private Person person;
}
