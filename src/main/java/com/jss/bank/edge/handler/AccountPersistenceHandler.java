package com.jss.bank.edge.handler;

import com.jss.bank.edge.domain.dto.AccountDTO;
import com.jss.bank.edge.domain.dto.PersonDTO;
import com.jss.bank.edge.domain.entity.Account;
import com.jss.bank.edge.domain.entity.Document;
import com.jss.bank.edge.domain.entity.Person;
import com.jss.bank.edge.repository.AccountRepository;
import com.jss.bank.edge.repository.DocumentRepository;
import com.jss.bank.edge.repository.PersonRepository;
import com.jss.bank.edge.security.AccountAuthenticationHandler;
import com.jss.bank.edge.util.codec.AccountDTOCodec;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.Message;
import io.vertx.mutiny.ext.auth.VertxContextPRNG;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.function.Consumer;

import static com.jss.bank.edge.util.EventBusFailureCode.MESSAGE_BODY_CLASS_NOT_EXPECTED;
import static com.jss.bank.edge.util.EventBusFailureCode.MESSAGE_WITHOUT_BODY;

public class AccountPersistenceHandler implements Consumer<Message<Object>> {

  private static final Logger logger = LoggerFactory.getLogger(AccountPersistenceHandler.class);

  public static final AccountDTOCodec BODY_CODEC = new AccountDTOCodec();

  private final DocumentRepository documentRepository;

  private final PersonRepository personRepository;

  private final AccountRepository accountRepository;

  public AccountPersistenceHandler(
      final DocumentRepository documentRepository,
      final PersonRepository personRepository,
      final AccountRepository accountRepository
  ) {
    this.documentRepository = documentRepository;
    this.personRepository = personRepository;
    this.accountRepository = accountRepository;
  }

  @Override
  public void accept(final Message<Object> message) {
    final Object raw = message.body();

    if (raw != null) {
      if (raw instanceof AccountDTO dto && dto.getPerson() != null) {
        persistAccount(dto)
            .subscribe()
            .with(__ -> message.reply(dto));
      } else message.fail(MESSAGE_BODY_CLASS_NOT_EXPECTED.getCode(), "Message body class was not expected");
    } else message.fail(MESSAGE_WITHOUT_BODY.getCode(), "Message body was null");
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

  private Uni<Account> persistAccount(final AccountDTO dto) {
    final AccountAuthenticationHandler accountAuthenticationHandler = new AccountAuthenticationHandler();
    final String password = accountAuthenticationHandler.getSqlAuthentication().hash(
        "pbkdf2",
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
