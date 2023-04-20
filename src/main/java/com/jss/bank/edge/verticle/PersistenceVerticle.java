package com.jss.bank.edge.verticle;

import com.jss.bank.edge.Application;
import com.jss.bank.edge.domain.dto.AccountDTO;
import com.jss.bank.edge.domain.dto.PersonDTO;
import com.jss.bank.edge.domain.entity.Account;
import com.jss.bank.edge.domain.entity.Document;
import com.jss.bank.edge.domain.entity.Person;
import com.jss.bank.edge.security.AccountAuthenticationHandler;
import com.jss.bank.edge.util.codec.AccountDTOCodec;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.mutiny.ext.auth.VertxContextPRNG;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

public class PersistenceVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(PersistenceVerticle.class);

  @Override
  public Uni<Void> asyncStart() {
    Infrastructure.setDroppedExceptionHandler(error -> logger.error("Mutiny dropped exception", error));

    final Mutiny.SessionFactory sessionFactory = Application.getSessionFactory();

    vertx.eventBus().registerCodec(new AccountDTOCodec());
    vertx.eventBus().consumer("edge.bank.persister.account.create")
        .handler(message -> Optional.ofNullable(message.body())
            .ifPresentOrElse(raw -> {
              if (raw instanceof AccountDTO dto && dto.getPerson() != null) {
                sessionFactory.withSession(session -> {
                      final String document = dto.getPerson().getDocument();

                      return session.createQuery("from documents where document = :document", Document.class)
                          .setParameter("document", document)
                          .getSingleResultOrNull();
                    })
                    .onItem()
                    .call(result -> {
                      if(result == null) {
                        final PersonDTO personDto = dto.getPerson();

                        final Document document = Document.builder()
                            .document(personDto.getDocument())
                            .documentType("cpf")
                            .build();

                        final Person person = Person.builder()
                            .firstName(personDto.getFirstName())
                            .surname(personDto.getSurname())
                            .document(document)
                            .birthdate(personDto.getBirthdate())
                            .build();

                        return sessionFactory.withSession(session -> session.persist(document)
                            .chain(__ -> session.persist(person))
                            .chain(__ -> persistAccount(dto, person, session)));
                      }
                      final PersonDTO personDto = dto.getPerson();

                      final Person person = Person.builder()
                          .firstName(personDto.getFirstName())
                          .surname(personDto.getSurname())
                          .document(result)
                          .birthdate(personDto.getBirthdate())
                          .build();

                      return sessionFactory.withSession(session -> session.persist(person)
                          .chain(__ -> persistAccount(dto, person, session)));
                    })
                    .subscribe()
                    .with(__ -> {
                      dto.setPassword(null);
                      dto.getPerson().setDocument(null);
                      message.reply(dto);
                    });

                return;
              }

              message.fail(1, "Message body class was not expected");
            }, () -> logger.info("Iam empty")));

    logger.info("PersistenceVerticle is ready");
    return super.asyncStart();
  }

  private static Uni<Void> persistAccount(final AccountDTO dto, final Person person, final Mutiny.Session session) {
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
        .person(person)
        .build();

    return session.persist(account);
  }
}
