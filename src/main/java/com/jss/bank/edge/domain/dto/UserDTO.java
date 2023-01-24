package com.jss.bank.edge.domain.dto;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
  private String username;
  private String password;
  private String role;
}
