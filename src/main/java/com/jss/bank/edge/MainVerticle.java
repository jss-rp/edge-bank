package com.jss.bank.edge;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

  public static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public Uni<Void> asyncStart() {
    final Uni<HttpServer> server = vertx.createHttpServer()
        .requestHandler(handler -> {
          handler.response().end("base").subscribe();
        })
        .listen(8080)
        .onItem().invoke(() -> logger.info("HTTP server started on port 8080"));

    return Uni.combine().all().unis(server).discardItems();
  }

  public static void main(String[] args) {
    final Vertx vertx = Vertx.vertx();

    vertx.deployVerticle(MainVerticle::new, new DeploymentOptions())
        .subscribe()
        .with(
            ok -> logger.info("Main Verticle has been deployed successfully"),
            err -> logger.error("Fail on deployment", err));
  }
}
