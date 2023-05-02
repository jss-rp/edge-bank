package com.jss.bank.edge;

import com.jss.bank.edge.configutaion.DBConfiguration;
import com.jss.bank.edge.verticle.ClientVerticle;
import com.jss.bank.edge.verticle.PersistenceVerticle;
import io.smallrye.mutiny.Uni;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.mutiny.core.Vertx;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Persistence;
import java.time.Duration;

public class Application {

  public static final Logger logger = LoggerFactory.getLogger(Application.class);

  private static Mutiny.SessionFactory SESSION_FACTORY;

  public static void main(String[] args) {
    final Vertx vertx = Vertx.vertx(new VertxOptions());

    DBConfiguration.initialize(vertx);

    Uni<Void> hibernateStartingUni = Uni.createFrom().deferred(() -> {
      SESSION_FACTORY = Persistence
          .createEntityManagerFactory(
              "default",
              DBConfiguration.getDefaultPersistenceUnitProperties())
          .unwrap(Mutiny.SessionFactory.class);

      return Uni.createFrom().voidItem();
    });

    hibernateStartingUni = vertx.executeBlocking(hibernateStartingUni)
        .onItem().invoke(() -> logger.info("Hibernate Reactive is ready"));


    final Uni<String> persistenceVerticleDeploymentUni = vertx.deployVerticle(PersistenceVerticle::new, new DeploymentOptions());
    final Uni<String> clientVerticleDeploymentUni = vertx.deployVerticle(ClientVerticle::new, new DeploymentOptions());

    hibernateStartingUni.await()
        .atMost(Duration.ofSeconds(30));

    Uni.combine().all().unis(
            persistenceVerticleDeploymentUni,
            clientVerticleDeploymentUni)
        .asTuple()
        .subscribe()
        .with(
            __ -> logger.info("Edge Bank was successful deployed"),
            error -> logger.error("Fail on deploying. Error:  ", error));
  }

  public static Mutiny.SessionFactory getSessionFactory() {
    return SESSION_FACTORY;
  }
}
