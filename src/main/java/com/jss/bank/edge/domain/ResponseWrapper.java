package com.jss.bank.edge.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseWrapper {

  private boolean success;

  private String message;

  private Object content;

  private LocalDateTime timestamp;
}
