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
public class AccountDTO {
  private String agency;
  private String code;
  private String dtVerifier;
  private BigDecimal balance;
  private PersonDTO person;
}
