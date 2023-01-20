package com.jss.bank.edge;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.http.HttpServer;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Persistence;

public class MainVerticle extends AbstractVerticle {

  public static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public Uni<Void> asyncStart() {
    Uni<Void> hibernate = Uni.createFrom().deferred(() -> {
      Persistence
          .createEntityManagerFactory("mysql-edge-bank")
          .unwrap(Mutiny.SessionFactory.class);

      return Uni.createFrom().voidItem();
    });

    hibernate = vertx.executeBlocking(hibernate)
        .onItem().invoke(() -> logger.info("Hibernate Reactive is ready"));

    final Uni<HttpServer> server = vertx.createHttpServer()
        .requestHandler(handler -> {
          handler.response().end("base").subscribe();
        })
        .listen(8080)
        .onItem().invoke(() -> logger.info("HTTP server started on port 8080"));

    return Uni.combine().all().unis(server, hibernate).discardItems();
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
