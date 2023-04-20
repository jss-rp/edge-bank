package com.jss.bank.edge.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO implements Serializable {

  private String agency;

  private String code;

  @JsonProperty("dt_verifier")
  private String dtVerifier;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String password;

  private BigDecimal balance;

  private PersonDTO person;
}
