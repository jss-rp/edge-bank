package com.jss.bank.edge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jss.bank.edge.security.AuthenticationHandler;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.mutiny.core.http.HttpServer;
import io.vertx.mutiny.ext.web.Router;
import io.vertx.mutiny.ext.web.handler.BodyHandler;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Persistence;

public class MainVerticle extends AbstractVerticle {

  public static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  private Mutiny.SessionFactory session;

  @Override
  public Uni<Void> asyncStart() {
    Infrastructure.setDroppedExceptionHandler(error -> logger.error("Mutiny dropped exception", error));

    final ObjectMapper mapper = DatabindCodec.mapper();
    mapper.findAndRegisterModules();
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    Uni<Void> hibernate = Uni.createFrom().deferred(() -> {
      session = Persistence
          .createEntityManagerFactory("mysql-edge-bank")
          .unwrap(Mutiny.SessionFactory.class);

      return Uni.createFrom().voidItem();
    });

    hibernate = vertx.executeBlocking(hibernate)
        .onItem().invoke(() -> logger.info("Hibernate Reactive is ready"));

    final Router root = Router.router(vertx);
    final AuthenticationHandler authHandler = new AuthenticationHandler(vertx);

    root.route("/*")
        .handler(authHandler)
        .handler(BodyHandler.create())
        .failureHandler(ctx -> {
          logger.error("An error occurred.", ctx.failure());
          ctx.response().setStatusCode(500).endAndForget();
        });

    final Uni<HttpServer> server = vertx.createHttpServer()
        .requestHandler(root)
        .listen(8080)
        .onItem().invoke(() -> logger.info("HTTP server started on port 8080"));

    return Uni.combine().all().unis(server, hibernate)
        .discardItems()
        .onTermination()
        .invoke(() -> root.route("/api/*").subRouter(ResourceRouter.route(vertx, session)));
  }
}
