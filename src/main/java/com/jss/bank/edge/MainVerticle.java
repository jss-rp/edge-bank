package com.jss.bank.edge;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.mutiny.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

  public static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public Uni<Void> asyncStart() {
    final Uni<HttpServer> server = vertx.createHttpServer()
        .listen(8080)
        .onItem().invoke(() -> logger.info("HTTP server started on port 8080"));

    return server.replaceWithVoid();
  }
}
