package com.jss.bank.edge.resource;

import com.jss.bank.edge.domain.ResponseWrapper;
import com.jss.bank.edge.domain.dto.*;
import com.jss.bank.edge.domain.entity.Account;
import com.jss.bank.edge.domain.entity.Person;
import com.jss.bank.edge.domain.entity.Transaction;
import com.jss.bank.edge.security.AuthenticationHandler;
import com.jss.bank.edge.security.RolesProvider;
import com.jss.bank.edge.security.entity.Role;
import com.jss.bank.edge.security.entity.User;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.ext.auth.VertxContextPRNG;
import io.vertx.mutiny.ext.web.Router;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class AccountResource extends AbstractResource {

  public static final Logger logger = LoggerFactory.getLogger(AccountResource.class);

  public AccountResource(final Router router, final Mutiny.SessionFactory sessionFactory, final AuthenticationHandler authHandler) {
    super(router, sessionFactory, authHandler);
  }

  @Override
  public void provide() {
    router.post("/account")
        .failureHandler(ctx -> {
          logger.error("error", ctx.failure());
          ctx.response()
              .setStatusCode(500)
              .endAndForget("Something is wrong");
        }).putMetadata("allowedRoles", RolesProvider.builder().build())
        .handler(authHandler)
        .respond(context -> {
          final AccountDTO dto = context.body().asPojo(AccountDTO.class);

          return sessionFactory.withTransaction(session -> {
            // TODO: Make a random password generator
            final String password = authHandler.getSqlAuthentication()
                .hash(
                    "pbkdf2",
                    VertxContextPRNG.current().nextString(32),
                    "password"
                );
            final User user = User.builder()
                .username(dto.username())
                .password(password)
                .authorization(Set.of(Role.builder()
                    .role("user")
                    .build()
                )).build();

            final Person person = Person.builder()
                .firstName(dto.person().firstName())
                .surname(dto.person().surname())
                .birthDate(LocalDate.now())
                .document(dto.person().document())
                .build();

            final Uni<Void> userPersistence = session.persist(user)
                .onFailure()
                .invoke(error -> logger.error("Fail on User persistence", error));

            final Uni<Void> personPersistence = session.persist(person)
                .onFailure()
                .invoke(error -> logger.error("Fail on Person persistence", error));

            return Uni.combine().all().unis(userPersistence, personPersistence)
                .asTuple()
                .flatMap(__ -> {
                  final Account account = Account.builder()
                      .code(dto.code())
                      .agency(dto.agency())
                      .dtVerifier(dto.dtVerifier())
                      .balance(BigDecimal.ZERO)
                      .user(user)
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
                                  new UserDTO(
                                      user.getUsername(),
                                      user.getPassword(),
                                      user.getAuthorization().stream()
                                          .map(Role::getRole)
                                          .collect(Collectors.toSet())),
                                  new PersonDTO(
                                      person.getFirstName(),
                                      person.getSurname(),
                                      person.getBirthDate(),
                                      person.getDocument())
                              ))
                              .timestamp(LocalDateTime.now())
                              .build()));
                });
          });
        });

    router.post("/account/transaction")
        .handler(authHandler)
        .putMetadata("allowedRoles", RolesProvider.builder()
            .role("all")
            .build())
        .respond(context -> {
          final TransactionDTO dto = context.body().asPojo(TransactionDTO.class);
          final Account account = Account.builder()
              .agency(dto.getAccountAgency())
              .code(dto.getAccountCode())
              .dtVerifier(dto.getDtVerifier())
              .user(User.builder()
                  .username(context.user().get("username"))
                  .build())
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
