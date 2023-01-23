package com.jss.bank.edge;

import com.jss.bank.edge.security.AuthenticationHandler;
import com.jss.bank.edge.security.entity.User;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.Router;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class RouterInitializer {

  public static final Logger logger = LoggerFactory.getLogger(RouterInitializer.class);

  public static Router initialize(final Vertx vertx, final Mutiny.SessionFactory sessionFactory) {
    final Router router = Router.router(vertx);

    router.get("/me")
        .failureHandler(ctx -> {
          logger.error("error",ctx.failure());
          ctx.response()
              .setStatusCode(500)
              .endAndForget("Something is wrong");
        })
        .handler(new AuthenticationHandler(vertx, Set.of("user")))
        .respond(context -> sessionFactory.withSession(session -> session
            .find(User.class,
                (String) context.user().get("username"))
        ));

    return router;
  }
}
