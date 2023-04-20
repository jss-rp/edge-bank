package com.jss.bank.edge.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonDTO implements Serializable {

  @JsonProperty("first_name")
  private String firstName;

  private String surname;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private LocalDate birthdate;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String document;
}
