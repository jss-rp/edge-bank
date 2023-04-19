package com.jss.bank.edge.resource;

import io.vertx.mutiny.core.Vertx;
import org.hibernate.reactive.mutiny.Mutiny;

public abstract class AbstractResource {

  protected final Mutiny.SessionFactory sessionFactory;

  protected final Vertx vertx;

  protected AbstractResource(final Vertx vertx, final Mutiny.SessionFactory sessionFactory) {
    this.vertx = vertx;
    this.sessionFactory = sessionFactory;
  }

  public abstract void provide();
}
