package com.jss.bank.edge.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
  private String accountAgency;
  private String accountCode;
  private String dtVerifier;

  private TransactionType type;

  private BigDecimal value;
}
