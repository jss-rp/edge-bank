package com.jss.bank.edge.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonDTO {

  @JsonProperty("first_name")
  private String firstName;

  private String surname;

  @JsonProperty("birth_date")
  private LocalDate birthDate;

  private String document;
}
