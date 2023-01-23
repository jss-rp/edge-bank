package com.jss.bank.edge;

import com.jss.bank.edge.security.AuthenticationHandler;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class RouterInitializer {

  public static final Logger logger = LoggerFactory.getLogger(RouterInitializer.class);

  public static Router initialize(final Vertx vertx) {
    final Router router = Router.router(vertx);

    router.get("/me")
        .failureHandler(ctx -> {
          logger.error("error",ctx.failure());
          ctx.response()
              .setStatusCode(500)
              .endAndForget("Something is wrong");
        })
        .handler(new AuthenticationHandler(vertx, Set.of("user")))
        .respond(context -> Uni.createFrom().item(context.user().authorizations().get("sql-client")));

    return router;
  }
}
