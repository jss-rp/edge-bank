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
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.junit5.RunTestOnContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
public class AccountServiceTest {

  @RegisterExtension
  static RunTestOnContext rtoc = new RunTestOnContext();

  @Test
  public void shouldPersistAccountSuccessfully() {
    final VertxTestContext testContext = new VertxTestContext();
    final DocumentRepository documentRepository = mock(DocumentRepository.class);
    final PersonRepository personRepository = mock(PersonRepository.class);
    final AccountRepository accountRepository = mock(AccountRepository.class);

    final AccountDTO dto = new AccountDTO(
        "agency",
        "code",
        "dt_verifier",
        "password",
        BigDecimal.ZERO,
        new PersonDTO(
            "first_name",
            "surname",
            LocalDate.now(),
            ""
        ));

    final Document document = Document.builder()
        .document("")
        .documentType("cpf")
        .build();

    final Person person = Person.builder()
        .firstName(dto.getPerson().getFirstName())
        .surname(dto.getPerson().getSurname())
        .birthdate(dto.getPerson().getBirthdate())
        .document(document)
        .build();

    final Account account = Account.builder()
        .agency(dto.getAgency())
        .code(dto.getCode())
        .dtVerifier(dto.getDtVerifier())
        .password("")
        .balance(BigDecimal.ZERO)
        .person(person)
        .build();

    Mockito.when(documentRepository.find(Mockito.any())).thenReturn(Uni.createFrom().item(document));
    Mockito.when(personRepository.persist(Mockito.any())).thenReturn(Uni.createFrom().item(person));
    Mockito.when(accountRepository.persist(Mockito.any())).thenReturn(Uni.createFrom().item(account));

    final AccountService accountService = new AccountService(
        documentRepository,
        personRepository,
        accountRepository);

    final UniAssertSubscriber<Account> subscriber = accountService.persistAccount(dto)
        .subscribe().withSubscriber(UniAssertSubscriber.create());

    verify(accountRepository).persist(any(Account.class));

    subscriber
        .awaitItem()
        .assertItem(account);

    testContext.completeNow();
  }
}
