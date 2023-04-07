package com.jss.bank.edge.resource;

import com.jss.bank.edge.domain.ResponseWrapper;
import com.jss.bank.edge.domain.dto.AccountDTO;
import com.jss.bank.edge.domain.dto.CreatedAccoutResponseDTO;
import com.jss.bank.edge.domain.dto.PersonDTO;
import com.jss.bank.edge.domain.dto.TransactionDTO;
import com.jss.bank.edge.domain.entity.Account;
import com.jss.bank.edge.domain.entity.Document;
import com.jss.bank.edge.domain.entity.Person;
import com.jss.bank.edge.domain.entity.Transaction;
import com.jss.bank.edge.security.AuthenticationHandler;
import com.jss.bank.edge.util.PasswordGenerator;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.ext.auth.VertxContextPRNG;
import io.vertx.mutiny.ext.web.Router;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class AccountResource extends AbstractResource {

  public static final Logger logger = LoggerFactory.getLogger(AccountResource.class);

  public AccountResource(final Router router, final Mutiny.SessionFactory sessionFactory, final AuthenticationHandler authHandler) {
    super(router, sessionFactory, authHandler);
  }

  @Override
  public void provide() {
    router.get("/account")
        .respond(context -> sessionFactory.withSession(session -> session
            .createQuery("SELECT a FROM accounts a WHERE a.code = :code", Account.class)
            .setParameter("code", context.user().get("username"))
            .getSingleResult()
            .onItem()
            .transform(account -> ResponseWrapper.builder()
                .success(true)
                .message("Account found successfully")
                .timestamp(LocalDateTime.now())
                .content(new AccountDTO(
                    account.getAgency(),
                    account.getCode(),
                    account.getDtVerifier(),
                    null,
                    account.getBalance(),
                    PersonDTO.builder()
                        .firstName(account.getPerson().getFirstName())
                        .surname(account.getPerson().getSurname())
                        .document(account.getPerson().getDocument().getDocument())
                        .birthDate(account.getPerson().getBirthDate())
                        .build()
                ))
                .build()
            )
        ));

    router.post("/account")
        .respond(context -> {
          final AccountDTO dto = context.body().asPojo(AccountDTO.class);

          return sessionFactory.withTransaction(session -> {
            final PasswordGenerator passwordGenerator = new PasswordGenerator();
            final String password = passwordGenerator.generate(15);
            final String hashedPassword = authHandler.getSqlAuthentication()
                .hash(
                    "pbkdf2",
                    VertxContextPRNG.current().nextString(32),
                    password
                );

            final Document document = Document.builder()
                .document(dto.getPerson().getDocument())
                .documentType("cpf")
                .build();

            final Person person = Person.builder()
                .firstName(dto.getPerson().getFirstName())
                .surname(dto.getPerson().getSurname())
                .birthDate(LocalDate.now())
                .document(document)
                .build();

            final Uni<Void> documentPersistence = session.persist(document)
                .onFailure()
                .invoke(error -> logger.error("Fail on Document persistence", error));

            final Uni<Void> personPersistence = session.persist(person)
                .onFailure()
                .invoke(error -> logger.error("Fail on Person persistence", error));


            return documentPersistence
                .chain(__ -> personPersistence)
                .flatMap(__ -> {
                  final Account account = Account.builder()
                      .code(dto.getCode())
                      .agency(dto.getAgency())
                      .dtVerifier(dto.getDtVerifier())
                      .balance(BigDecimal.ZERO)
                      .password(hashedPassword)
                      .person(person)
                      .build();

                  return session.persist(account)
                      .call(session::flush)
                      .chain(empty -> Uni.createFrom().item(
                          ResponseWrapper.builder()
                              .success(true)
                              .message("New account created successfully")
                              .content(new CreatedAccoutResponseDTO(
                                  account.getAgency(),
                                  account.getCode(),
                                  account.getDtVerifier(),
                                  null,
                                  new PersonDTO(
                                      person.getFirstName(),
                                      person.getSurname(),
                                      person.getBirthDate(),
                                      document.getDocument())
                              ))
                              .timestamp(LocalDateTime.now())
                              .build()));
                });
          });
        });

    router.post("/account/transaction")
        .respond(context -> {
          final TransactionDTO dto = context.body().asPojo(TransactionDTO.class);
          final Account account = Account.builder()
              .agency(dto.getAccountAgency())
              .code(dto.getAccountCode())
              .dtVerifier(dto.getDtVerifier())
              .build();

          final Transaction transaction = Transaction.builder()
              .uuid(UUID.randomUUID().toString())
              .value(dto.getValue())
              .type(dto.getType().toString())
              .account(account)
              .build();

          return sessionFactory.withSession(session -> session
              .createQuery("from accounts where code = :code and dtVerifier = :dtVerifier", Account.class)
              .setParameter("code", account.getCode())
              .setParameter("dtVerifier", account.getDtVerifier())
              .getSingleResult()
              .chain(result -> {
                account.setId(result.getId());

                return session.persist(transaction)
                    .chain(() -> {
                      transaction.setFinishedAt(LocalDateTime.now());
                      return session.persist(transaction).call(session::flush);
                    });
              })).onItem().transform(result -> ResponseWrapper.builder()
              .success(true)
              .message("OK")
              .timestamp(LocalDateTime.now())
              .build());
        });
  }
}
