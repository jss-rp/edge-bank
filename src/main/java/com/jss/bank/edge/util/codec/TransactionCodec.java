package com.jss.bank.edge.util.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jss.bank.edge.domain.entity.Transaction;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.jackson.DatabindCodec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TransactionCodec implements MessageCodec<Transaction, Transaction> {

  private final ObjectMapper mapper = DatabindCodec.mapper();

  @Override
  public void encodeToWire(final Buffer buffer, final Transaction transaction) {
    try {
      final String raw = mapper.writeValueAsString(transaction);
      buffer.appendBytes(raw.getBytes(StandardCharsets.UTF_8));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Transaction decodeFromWire(final int pos, final Buffer buffer) {
    final String raw = new String(buffer.getBytes());

    try {
      return mapper.reader().readValue(raw, Transaction.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Transaction transform(final Transaction transaction) {
    return transaction;
  }

  @Override
  public String name() {
    return Transaction.class.getName();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
