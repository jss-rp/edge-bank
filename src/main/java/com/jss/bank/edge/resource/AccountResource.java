package com.jss.bank.edge.resource;

import com.jss.bank.edge.domain.ResponseWrapper;
import com.jss.bank.edge.domain.dto.AccountDTO;
import com.jss.bank.edge.domain.dto.PersonDTO;
import com.jss.bank.edge.domain.dto.TransactionDTO;
import com.jss.bank.edge.domain.entity.Account;
import com.jss.bank.edge.domain.entity.Transaction;
import com.jss.bank.edge.security.AccountAuthenticationHandler;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.Router;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.jss.bank.edge.handler.TransactionPersistenceHandler.DELIVERY_OPTIONS;
import static com.jss.bank.edge.verticle.PersistenceVerticle.TRANSACTION_PERSISTENCE_ADDR;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;

public class AccountResource extends AbstractResource {

  public static final Logger logger = LoggerFactory.getLogger(AccountResource.class);

  private final Router router;

  private final AccountAuthenticationHandler accountAuthenticationHandler;

  public AccountResource(final Router router, final Vertx vertx, final Mutiny.SessionFactory sessionFactory) {
    super(vertx, sessionFactory);
    this.router = router;
    this.accountAuthenticationHandler = new AccountAuthenticationHandler();
  }

  @Override
  public void provide() {
    router.route(GET, "/account")
        .handler(this.accountAuthenticationHandler)
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
                        .birthdate(account.getPerson().getBirthdate())
                        .build()
                ))
                .build()
            )
        ));

    router.route(POST, "/account/transaction")
        .handler(accountAuthenticationHandler)
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

          return vertx.eventBus()
              .request(
                  TRANSACTION_PERSISTENCE_ADDR,
                  transaction,
                  DELIVERY_OPTIONS)
              .onItem()
              .invoke(() -> context.response().setStatusCode(201))
              .onFailure()
              .invoke(error -> {
                logger.error("Fail on Transaction persistence. Error: ", error);
                context.response().setStatusCode(500);
              })
              .replaceWithVoid();
        });
  }
}
