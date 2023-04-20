package com.jss.bank.edge.verticle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jss.bank.edge.Application;
import com.jss.bank.edge.ResourceRouter;
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

public class ClientVerticle extends AbstractVerticle {

  public static final Logger logger = LoggerFactory.getLogger(ClientVerticle.class);

  private final Mutiny.SessionFactory sessionFactory = Application.getSessionFactory();

  @Override
  public Uni<Void> asyncStart() {
    Infrastructure.setDroppedExceptionHandler(error -> logger.error("Mutiny dropped exception", error));

    final ObjectMapper mapper = DatabindCodec.mapper();
    mapper.findAndRegisterModules();
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    final Router root = Router.router(vertx);

    root.route("/*")
        .handler(BodyHandler.create())
        .failureHandler(ctx -> {
          logger.error("An error occurred.", ctx.failure());
          ctx.response().setStatusCode(500).endAndForget();
        });

    final Uni<HttpServer> server = vertx.createHttpServer()
        .requestHandler(root)
        .listen(8080)
        .onItem().invoke(() -> logger.info("HTTP server started on port 8080"));

    return server
        .onItem().ignore()
        .andContinueWithNull()
        .onTermination()
        .invoke(() -> {
          root.route("/api/*").subRouter(ResourceRouter.route(vertx, sessionFactory));
          logger.info("ClientVerticle is ready");
        });
  }
}
