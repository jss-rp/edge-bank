package com.jss.bank.edge.util.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jss.bank.edge.domain.dto.AccountDTO;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.jackson.DatabindCodec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AccountDTOCodec implements MessageCodec<AccountDTO, AccountDTO> {

  private final ObjectMapper mapper = DatabindCodec.mapper();

  @Override
  public void encodeToWire(final Buffer buffer, final AccountDTO accountDTO) {
    try {
      final String raw = mapper.writeValueAsString(accountDTO);
      buffer.appendBytes(raw.getBytes(StandardCharsets.UTF_8));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public AccountDTO decodeFromWire(final int pos, final Buffer buffer) {
    final String raw = new String(buffer.getBytes());

    try {
      return mapper.reader().readValue(raw, AccountDTO.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public AccountDTO transform(final AccountDTO accountDTO) {
    return accountDTO;
  }

  @Override
  public String name() {
    return AccountDTO.class.getName();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
