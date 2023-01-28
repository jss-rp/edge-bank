package com.jss.bank.edge.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "transactions")
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  private String uuid;

  @Column
  private BigDecimal value;

  @Column
  private String type;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "finished_at")
  private LocalDateTime finishedAt;

  @ManyToOne
  @JoinColumn(name = "account_code")
  private Account account;
}
