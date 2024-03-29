package com.jss.bank.edge.handler;

import com.jss.bank.edge.domain.dto.AccountDTO;
import com.jss.bank.edge.service.AccountService;
import com.jss.bank.edge.util.codec.AccountDTOCodec;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.mutiny.core.eventbus.Message;

import java.util.Optional;
import java.util.function.Consumer;

import static com.jss.bank.edge.util.EventBusFailureCode.MESSAGE_WITHOUT_BODY;
import static com.jss.bank.edge.util.EventBusFailureCode.UNKOWN_ERROR;

public class AccountPersistenceHandler implements Consumer<Message<Object>> {

  public static final DeliveryOptions DELIVERY_OPTIONS = new DeliveryOptions()
      .setCodecName(AccountDTOCodec.class.getName());

  public static final AccountDTOCodec BODY_CODEC = new AccountDTOCodec();

  private final AccountService accountService;

  public AccountPersistenceHandler(final AccountService accountService) {
    this.accountService = accountService;
  }

  @Override
  public void accept(final Message<Object> message) {
    Optional.ofNullable(message.body())
        .ifPresentOrElse(body -> {
          final AccountDTO dto = (AccountDTO) body;
          accountService.persistAccount(dto)
              .subscribe()
              .with(
                  __ -> message.reply(dto, DELIVERY_OPTIONS),
                  error -> message.fail(UNKOWN_ERROR.getCode(), error.getMessage())
              );
        }, () -> message.fail(MESSAGE_WITHOUT_BODY.getCode(), "That message is empty"));
  }
}
