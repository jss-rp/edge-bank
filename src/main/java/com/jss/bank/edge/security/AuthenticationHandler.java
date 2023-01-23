package com.jss.bank.edge.security;

import com.jss.bank.edge.security.exception.InvalidUsernamePasswordException;
import com.jss.bank.edge.security.exception.UserNotAllowedException;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.auth.sqlclient.SqlAuthenticationOptions;
import io.vertx.mutiny.core.Context;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.http.HttpServerRequest;
import io.vertx.mutiny.ext.auth.authentication.AuthenticationProvider;
import io.vertx.mutiny.ext.auth.authorization.AuthorizationProvider;
import io.vertx.mutiny.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.mutiny.ext.auth.sqlclient.SqlAuthentication;
import io.vertx.mutiny.ext.auth.sqlclient.SqlAuthorization;
import io.vertx.mutiny.ext.web.RoutingContext;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.SqlClient;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class AuthenticationHandler implements Consumer<RoutingContext> {

  public static final Logger logger = LoggerFactory.getLogger(AuthenticationHandler.class);

  private AuthenticationProvider authenticationProvider;

  private AuthorizationProvider authorizationProvider;

  private final Set<String> allowedRoles = new HashSet<>();

  public AuthenticationHandler(final Vertx vertx, final Set<String> allowedRoles) {
    final Context context = vertx.getOrCreateContext();
    final Optional<JsonObject> optionalConfig = Optional.ofNullable(context.config().getJsonObject("auth_db"));

    optionalConfig.ifPresentOrElse(
        config -> {
          final MySQLConnectOptions connectOptions = new MySQLConnectOptions();
          connectOptions.setHost(config.getString("host", "localhost"));
          connectOptions.setPort(config.getInteger("port", 3306));
          connectOptions.setDatabase(config.getString("schema"));
          connectOptions.setUser(config.getString("username"));
          connectOptions.setPassword(config.getString("password"));

          final PoolOptions poolOptions = new PoolOptions();
          poolOptions.setMaxSize(10);

          final SqlClient client = MySQLPool.pool(vertx, connectOptions, poolOptions);
          this.authenticationProvider = SqlAuthentication.create(client, new SqlAuthenticationOptions());
          this.authorizationProvider = SqlAuthorization.create(client);
          this.allowedRoles.addAll(allowedRoles);
        },
        () -> logger.error("No database configuration for AuthenticationProvider.")
    );
  }


  @Override
  public void accept(final RoutingContext context) {
    if (authenticationProvider == null) {
      logger.error("AuthenticationProvider is null.");
      context.response().setStatusCode(500).endAndForget();
      return;
    }

    extractCredentials(context.request())
        .onItem()
        .call(credential -> authenticationProvider.authenticate(credential)
            .onItem()
            .call(user -> authorizationProvider.getAuthorizations(user))
            .invoke(user -> this.allowedRoles.forEach(role -> {
              if (!RoleBasedAuthorization.create(role).match(user)) {
                throw new UserNotAllowedException("Current user is not allowed for [" + role + "] resources.");
              }
              context.setUser(user);
            }))
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
}
