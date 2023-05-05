package com.jss.bank.edge.handler;

import com.jss.bank.edge.domain.entity.Transaction;
import com.jss.bank.edge.service.TransactionService;
import com.jss.bank.edge.util.codec.TransactionCodec;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.mutiny.core.eventbus.Message;

import java.util.Optional;
import java.util.function.Consumer;

import static com.jss.bank.edge.util.EventBusFailureCode.MESSAGE_WITHOUT_BODY;
import static com.jss.bank.edge.util.EventBusFailureCode.UNKOWN_ERROR;

public class TransactionPersistenceHandler implements Consumer<Message<Object>> {

  public static final DeliveryOptions DELIVERY_OPTIONS = new DeliveryOptions()
      .setCodecName(Transaction.class.getName());

  public static final TransactionCodec BODY_CODEC = new TransactionCodec();


  private final TransactionService transactionService;

  public TransactionPersistenceHandler(final TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @Override
  public void accept(final Message<Object> message) {
    Optional.ofNullable(message.body())
        .ifPresentOrElse(body -> {
          final Transaction transaction = (Transaction) body;

          transactionService.persistTransaction(transaction)
              .subscribe()
              .with(
                  __ -> message.reply(transaction, DELIVERY_OPTIONS),
                  error -> message.fail(UNKOWN_ERROR.getCode(), error.getMessage()));
        }, () -> message.fail(MESSAGE_WITHOUT_BODY.getCode(), "That message is empty"));
  }
}
