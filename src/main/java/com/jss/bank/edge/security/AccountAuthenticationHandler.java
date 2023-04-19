package com.jss.bank.edge.security;

import com.jss.bank.edge.configutaion.AuthDBConfiguration;
import com.jss.bank.edge.security.exception.InvalidUsernamePasswordException;
import com.jss.bank.edge.security.exception.UserNotAllowedException;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.auth.sqlclient.SqlAuthenticationOptions;
import io.vertx.mutiny.core.http.HttpServerRequest;
import io.vertx.mutiny.ext.auth.sqlclient.SqlAuthentication;
import io.vertx.mutiny.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Optional;
import java.util.function.Consumer;

public class AccountAuthenticationHandler implements Consumer<RoutingContext> {

  public static final Logger logger = LoggerFactory.getLogger(AccountAuthenticationHandler.class);

  private static final String AUTH_QUERY = "SELECT password FROM accounts WHERE code = ?";

  private SqlAuthentication sqlAuthentication;

  public AccountAuthenticationHandler() {
    Optional.ofNullable(AuthDBConfiguration.getSQLCLient())
        .ifPresentOrElse(
            client -> this.sqlAuthentication = SqlAuthentication.create(client, new SqlAuthenticationOptions(
                new JsonObject().put("authenticationQuery", AUTH_QUERY)
            )),
            () -> logger.error("No database configuration for AuthenticationProvider.")
        );
  }

  @Override
  public void accept(final RoutingContext context) {
    if (sqlAuthentication == null) {
      logger.error("AuthenticationProvider is null.");
      context.response().setStatusCode(500).endAndForget();
      return;
    }

    extractCredentials(context.request())
        .onItem()
        .call(credential -> sqlAuthentication.authenticate(credential)
            .onItem()
            .invoke(context::setUser)
            .onFailure()
            .transform(error -> {
              if (error instanceof UserNotAllowedException) {
                return error;
              }

              return new InvalidUsernamePasswordException(error);
            }))
        .subscribe().with(
            credential -> {
              logger.debug("Successfully authenticated!");
              context.next();
            },
            error -> {
              logger.error("Authentication failed.", error);
              context.response().setStatusCode(401).endAndForget();
            });
  }

  private Uni<UsernamePasswordCredentials> extractCredentials(final HttpServerRequest request) {
    return Uni.createFrom().item(request.getHeader("Authorization"))
        .onItem()
        .transform(rawEncodedCredentials -> {
          final byte[] decode = Base64.getDecoder().decode(rawEncodedCredentials.getBytes());
          final String rawDecodedCredentials = new String(decode);
          final String[] splitted = rawDecodedCredentials.split(":");

          return new UsernamePasswordCredentials(splitted[0], splitted[1]);

        })
        .onFailure()
        .invoke(error -> {
          request.response().setStatusCode(401).endAndForget();
          logger.error("Fail on authentication.", error);
        });
  }

  public SqlAuthentication getSqlAuthentication() {
    return sqlAuthentication;
  }
}
