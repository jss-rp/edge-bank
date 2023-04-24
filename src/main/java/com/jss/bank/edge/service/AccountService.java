package com.jss.bank.edge.service;

import com.jss.bank.edge.domain.dto.AccountDTO;
import com.jss.bank.edge.domain.dto.PersonDTO;
import com.jss.bank.edge.domain.entity.Account;
import com.jss.bank.edge.domain.entity.Document;
import com.jss.bank.edge.domain.entity.Person;
import com.jss.bank.edge.repository.AccountRepository;
import com.jss.bank.edge.repository.DocumentRepository;
import com.jss.bank.edge.repository.PersonRepository;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.ext.auth.HashingStrategy;
import io.vertx.mutiny.ext.auth.VertxContextPRNG;

import java.math.BigDecimal;

public class AccountService {

  private final DocumentRepository documentRepository;

  private final PersonRepository personRepository;

  private final AccountRepository accountRepository;

  public AccountService(
      final DocumentRepository documentRepository,
      final PersonRepository personRepository,
      final AccountRepository accountRepository) {
    this.documentRepository = documentRepository;
    this.personRepository = personRepository;
    this.accountRepository = accountRepository;
  }


  private Uni<Person> persistAccountAssociates(final AccountDTO dto) {
    final PersonDTO personDto = dto.getPerson();
    final String rawDocument = personDto.getDocument();

    final Person person = Person.builder()
        .firstName(personDto.getFirstName())
        .surname(personDto.getSurname())
        .birthdate(personDto.getBirthdate())
        .build();

    return persistDocument(rawDocument, "cpf")
        .chain(document -> {
          person.setDocument(document);
          return personRepository.persist(person);
        });
  }

  private Uni<Document> persistDocument(final String raw, final String type) {
    final Document document = Document.builder()
        .document(raw)
        .documentType(type)
        .build();

    return documentRepository.find(document)
        .onItem()
        .ifNull()
        .switchTo(documentRepository.persist(document));
  }

  public Uni<Account> persistAccount(final AccountDTO dto) {
    final HashingStrategy hashingStrategy = HashingStrategy.load();
    final String password = hashingStrategy.hash(
        "pbkdf2",
        null,
        VertxContextPRNG.current().nextString(32),
        dto.getPassword()
    );

    final Account account = Account.builder()
        .agency(dto.getAgency())
        .code(dto.getCode())
        .dtVerifier(dto.getDtVerifier())
        .password(password)
        .balance(BigDecimal.ZERO)
        .build();

    return persistAccountAssociates(dto)
        .chain(person -> {
          account.setPerson(person);
          return accountRepository.persist(account);
        });
  }
}
