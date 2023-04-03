package com.jss.bank.edge.domain.dto;

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
  private String firstName;
  private String surname;
  private LocalDate birthDate;
  private String documen;
}
