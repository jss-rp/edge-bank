package com.jss.bank.edge.security;

import com.jss.bank.edge.configutaion.AuthDBConfiguration;
import com.jss.bank.edge.security.exception.InvalidBearerTokenException;
import com.jss.bank.edge.security.exception.UserNotAllowedException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.mutiny.core.Context;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.http.Cookie;
import io.vertx.mutiny.ext.auth.jwt.JWTAuth;
import io.vertx.mutiny.ext.auth.sqlclient.SqlAuthentication;
import io.vertx.mutiny.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Consumer;

public class ManagerAuthenticationHandler implements Consumer<RoutingContext> {

  public static final Logger logger = LoggerFactory.getLogger(ManagerAuthenticationHandler.class);

  private final JWTAuth jwtAuth;

  private SqlAuthentication sqlAuth;

  public ManagerAuthenticationHandler(final Vertx vertx) {
    final Context context = vertx.getOrCreateContext();

    final JWTAuthOptions jwtAuthOptions = new JWTAuthOptions()
        .addPubSecKey(new PubSecKeyOptions()
            .setAlgorithm("HS256")
            .setBuffer("edge_bank"));

    this.jwtAuth = JWTAuth.create(vertx, jwtAuthOptions);

    Optional.ofNullable(AuthDBConfiguration.getSQLCLient())
        .ifPresentOrElse(
            client -> this.sqlAuth = SqlAuthentication.create(client),
            () -> logger.error("No database configuration for SQLAuthentication.")
        );
  }

  @Override
  public void accept(final RoutingContext context) {
    final String raw = context.request().getHeader("Authorization");
    final String[] spplitedRaw = raw.split(" ");

    switch (spplitedRaw[0]) {
      case "Bearer" -> {
        final TokenCredentials credentials = new TokenCredentials(spplitedRaw[1]);
        jwtAuth.authenticate(credentials)
            .onItem()
            .invoke(context::next)
            .onFailure()
            .transform(error -> {
              if (error instanceof UserNotAllowedException) {
                return error;
              }

              return new InvalidBearerTokenException(error);
            })
            .subscribe().with(
                result -> {
                  logger.debug("Successfully authenticated!");
                  context.next();
                },
                error -> {
                  logger.error("Authentication failed.", error);
                  context.response().setStatusCode(401).endAndForget();
                }
            );
      }

      case "Basic" -> {
        final UsernamePasswordCredentials credentials = extractCredentials(spplitedRaw[1]);
        sqlAuth.authenticate(credentials)
            .onItem()
            .invoke(user -> {
              final String token = jwtAuth.generateToken(
                  new JsonObject()
                      .put("sub", "edge-bank")
                      .put("username", user.get("username"))
                      .put("iat", LocalDateTime.now().getLong(ChronoField.INSTANT_SECONDS))
              );

              context.response()
                  .addCookie(Cookie.cookie("JWT", token))
                  .setStatusCode(200)
                  .endAndForget();
            })
            .subscribe()
            .with(
                credential -> {
                  logger.debug("Successfully authenticated!");
                  context.next();
                },
                error -> {
                  logger.error("Authentication failed.", error);
                  context.response().setStatusCode(401).endAndForget();
                });
      }
    }
  }
  private UsernamePasswordCredentials extractCredentials(final String coded) {
    final byte[] decode = Base64.getDecoder().decode(coded.getBytes());
    final String rawDecodedCredentials = new String(decode);
    final String[] splitted = rawDecodedCredentials.split(":");

    return new UsernamePasswordCredentials(splitted[0], splitted[1]);
  }
}
