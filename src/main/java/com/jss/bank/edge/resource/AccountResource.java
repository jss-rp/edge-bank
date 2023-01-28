package com.jss.bank.edge.resource;

import com.jss.bank.edge.domain.ResponseWrapper;
import com.jss.bank.edge.domain.dto.AccountDTO;
import com.jss.bank.edge.domain.dto.TransactionDTO;
import com.jss.bank.edge.domain.entity.Account;
import com.jss.bank.edge.domain.entity.Transaction;
import com.jss.bank.edge.security.AuthenticationHandler;
import com.jss.bank.edge.security.entity.User;
import io.vertx.mutiny.ext.web.Router;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static com.jss.bank.edge.domain.dto.TransactionType.INCOME;
import static com.jss.bank.edge.domain.dto.TransactionType.OUTCOME;

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
        }).putMetadata("allowedRoles", Set.of("root"))
        .handler(authHandler)
        .respond(context -> {
          final AccountDTO dto = context.body().asPojo(AccountDTO.class);
          return sessionFactory.withSession(session -> {
            final Account account = Account.builder()
                .code(dto.getCode())
                .agency(dto.getAgency())
                .dtVerifier(dto.getDtVerifier())
                .balance(0.0)
                .user(User.builder()
                    .username(dto.getUsername())
                    .build())
                .build();

            return session.persist(account)
                .call(session::flush)
                .replaceWith(ResponseWrapper.builder()
                    .success(true)
                    .timestamp(LocalDateTime.now())
                    .message("Account created successfully")
                    .content(dto)
                    .build());
          });
        });

    router.post("/account/transaction")
        .handler(authHandler)
        .putMetadata("allowedRoles", Set.of("all"))
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
                      if(INCOME.toString().equals(transaction.getType())) {
                        result.setBalance(result.getBalance() + transaction.getValue().doubleValue());
                      } else if (OUTCOME.toString().equals(transaction.getType())) {
                        result.setBalance(result.getBalance() - transaction.getValue().doubleValue());
                      }

                      return session.persist(result);
                    })
                    .call(() -> {
                      transaction.setFinishedAt(LocalDateTime.now());

                      return session.persist(transaction)
                          .call(session::flush);
                    });
              })).onItem().transform(result -> ResponseWrapper.builder()
              .success(true)
              .message("OK")
              .timestamp(LocalDateTime.now())
              .build());
        });

  }
}
