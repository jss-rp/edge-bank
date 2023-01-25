package com.jss.bank.edge.resource;

import com.jss.bank.edge.security.AuthenticationHandler;
import io.vertx.mutiny.ext.web.Router;
import org.hibernate.reactive.mutiny.Mutiny;

public abstract class AbstractResource {

  protected final Router router;

  protected final Mutiny.SessionFactory sessionFactory;

  protected final AuthenticationHandler authHandler;

  protected AbstractResource(final Router router, final Mutiny.SessionFactory sessionFactory, final AuthenticationHandler authHandler) {
    this.router = router;
    this.sessionFactory = sessionFactory;
    this.authHandler = authHandler;
  }

  public abstract void provide();
}
