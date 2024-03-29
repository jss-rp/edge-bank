package com.jss.bank.edge.verticle;

import com.jss.bank.edge.Application;
import com.jss.bank.edge.handler.AccountPersistenceHandler;
import com.jss.bank.edge.handler.TransactionPersistenceHandler;
import com.jss.bank.edge.repository.AccountRepository;
import com.jss.bank.edge.repository.DocumentRepository;
import com.jss.bank.edge.repository.PersonRepository;
import com.jss.bank.edge.repository.TransactionRepository;
import com.jss.bank.edge.service.AccountService;
import com.jss.bank.edge.service.TransactionService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(PersistenceVerticle.class);

  public static final String ACCOUNT_PERSISTENCE_ADDR = "edge.bank.persister.account.create";
  public static final String TRANSACTION_PERSISTENCE_ADDR = "edge.bank.persister.transaction.create";

  @Override
  public Uni<Void> asyncStart() {
    Infrastructure.setDroppedExceptionHandler(error -> logger.error("Mutiny dropped exception", error));

    final Mutiny.SessionFactory sessionFactory = Application.getSessionFactory();

    vertx.eventBus().registerCodec(AccountPersistenceHandler.BODY_CODEC);
    vertx.eventBus().registerCodec(TransactionPersistenceHandler.BODY_CODEC);

    final TransactionRepository transactionRepository = new TransactionRepository(sessionFactory);
    final DocumentRepository documentRepository = new DocumentRepository(sessionFactory);
    final PersonRepository personRepository = new PersonRepository(sessionFactory);
    final AccountRepository accountRepository = new AccountRepository(sessionFactory);
    final TransactionService transactionService = new TransactionService(transactionRepository);
    final AccountService accountService = new AccountService(documentRepository, personRepository, accountRepository);


    vertx.eventBus().consumer(ACCOUNT_PERSISTENCE_ADDR, new AccountPersistenceHandler(accountService));
    vertx.eventBus().consumer(TRANSACTION_PERSISTENCE_ADDR, new TransactionPersistenceHandler(transactionService));

    logger.info("PersistenceVerticle is ready");
    return super.asyncStart();
  }
}
