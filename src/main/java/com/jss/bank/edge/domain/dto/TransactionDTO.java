package com.jss.bank.edge.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

  @JsonProperty("account_agency")
  private String accountAgency;

  @JsonProperty("account_code")
  private String accountCode;

  @JsonProperty("dt_verifier")
  private String dtVerifier;

  private TransactionType type;

  private BigDecimal value;
}
