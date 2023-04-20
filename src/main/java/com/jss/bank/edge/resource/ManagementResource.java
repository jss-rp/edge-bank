package com.jss.bank.edge.resource;

import com.jss.bank.edge.domain.ResponseWrapper;
import com.jss.bank.edge.domain.dto.AccountDTO;
import com.jss.bank.edge.security.ManagerAuthenticationHandler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.RequestBody;
import io.vertx.mutiny.ext.web.Router;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class ManagementResource extends AbstractResource{

  public static final Logger logger = LoggerFactory.getLogger(ManagementResource.class);

  private final Router router;

  private final ManagerAuthenticationHandler managerAuthenticationHandler;

  public ManagementResource(final Router router, final Vertx vertx, final Mutiny.SessionFactory sessionFactory) {
    super(vertx, sessionFactory);
    this.router = router;
    this.managerAuthenticationHandler = new ManagerAuthenticationHandler(vertx);
  }

  @Override
  public void provide() {

    router.route(HttpMethod.POST, "/manager/authenticate")
        .handler(managerAuthenticationHandler);

    router.route(HttpMethod.POST, "/manager/new/account")
        .handler(managerAuthenticationHandler)
        .respond(context -> {
          final RequestBody body = context.body();
          final AccountDTO dto = body.asPojo(AccountDTO.class);
          final DeliveryOptions deliveryOptions = new DeliveryOptions()
              .setCodecName("AccountDTO");

          return vertx.eventBus()
              .request("edge.bank.persister.account.create", dto, deliveryOptions)
              .onItem()
              .transform(result -> ResponseWrapper.builder()
                  .message("Account created successfully")
                  .content(result.body())
                  .success(true)
                  .timestamp(LocalDateTime.now())
                  .build()
              );
        });
  }
}
