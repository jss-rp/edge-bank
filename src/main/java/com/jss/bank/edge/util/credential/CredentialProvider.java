package com.jss.bank.edge.util.credential;

import com.jss.bank.edge.util.IntegratedService;
import io.vertx.core.json.JsonObject;

public interface CredentialProvider {

  JsonObject getCrendentials(final IntegratedService service);
}
