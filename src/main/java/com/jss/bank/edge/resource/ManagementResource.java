package com.jss.bank.edge.resource;

import com.jss.bank.edge.security.ManagerAuthenticationHandler;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.Router;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagementResource extends AbstractResource{

  public static final Logger logger = LoggerFactory.getLogger(ManagementResource.class);

  private final Router router;

  private final ManagerAuthenticationHandler managerAuthenticationHandler;

  public ManagementResource(final Router router, final Vertx vertx, final Mutiny.SessionFactory sessionFactory) {
    super(vertx, sessionFactory);
    this.router = router;
    this.managerAuthenticationHandler = new ManagerAuthenticationHandler(vertx);
  }

  @Override
  public void provide() {
    router.route(HttpMethod.POST, "/manager/authenticate")
        .handler(managerAuthenticationHandler);
  }
}
