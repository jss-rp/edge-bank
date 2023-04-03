package com.jss.bank.edge;

import com.jss.bank.edge.resource.AbstractResource;
import com.jss.bank.edge.resource.AccountResource;
import com.jss.bank.edge.security.AuthenticationHandler;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.Router;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.HashSet;
import java.util.Set;

public class ResourceRouter {

  public static Router route(final Vertx vertx, final Mutiny.SessionFactory sessionFactory) {
    final Router router = Router.router(vertx);
    final AuthenticationHandler authHandler = new AuthenticationHandler(vertx);
    final Set<AbstractResource> resources = new HashSet<>();

    resources.add(new AccountResource(router, sessionFactory, authHandler));
    resources.forEach(AbstractResource::provide);

    return router;
  }
}
