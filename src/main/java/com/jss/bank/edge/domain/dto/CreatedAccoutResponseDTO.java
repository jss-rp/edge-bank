package com.jss.bank.edge.domain.dto;

public record CreatedAccoutResponseDTO(
    String agency,
    String code,
    String dtVerifier,
    UserDTO user,
    PersonDTO person
) { }

