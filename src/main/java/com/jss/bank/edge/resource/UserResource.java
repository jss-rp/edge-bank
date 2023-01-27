package com.jss.bank.edge.resource;

import com.jss.bank.edge.domain.ResponseWrapper;
import com.jss.bank.edge.domain.dto.UserDTO;
import com.jss.bank.edge.security.AuthenticationHandler;
import com.jss.bank.edge.security.entity.Role;
import com.jss.bank.edge.security.entity.User;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.auth.VertxContextPRNG;
import io.vertx.mutiny.ext.web.Router;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class UserResource extends AbstractResource {

  public static final Logger logger = LoggerFactory.getLogger(UserResource.class);

  public UserResource(final Router router, final Mutiny.SessionFactory sessionFactory, final AuthenticationHandler authHandler) {
    super(router, sessionFactory, authHandler);
  }

  @Override
  public void provide() {
    router.get("/me")
        .failureHandler(ctx -> {
          logger.error("error",ctx.failure());
          ctx.response()
              .setStatusCode(500)
              .endAndForget("Something is wrong");
        })
        .putMetadata("allowedRoles", Set.of("all"))
        .handler(authHandler)
        .respond(context -> sessionFactory.withSession(session -> session
            .find(User.class,
                (String) context.user().get("username"))
            .onItem()
            .transform(user -> ResponseWrapper.builder()
                .success(true)
                .message("User data found successfully")
                .timestamp(LocalDateTime.now())
                .content(UserDTO.builder()
                    .username(user.getUsername())
                    .role(user.getAuthorization().stream().map(Role::getRole).collect(Collectors.toSet()))
                    .build())
                .build())
        ));

    router.post("/user")
        .consumes("application/json")
        .produces("application/json")
        .putMetadata("allowedRoles", Set.of("user"))
        .handler(authHandler)
        .respond(context -> {
          final UserDTO dto = context.body().asPojo(UserDTO.class);
          return sessionFactory.withSession(session -> {
            final User user = User.builder()
                .username(dto.getUsername())
                .authorization(dto.getRole().stream()
                        .map(raw -> Role.builder()
                                .role(raw)
                                .build())
                        .collect(Collectors.toSet()))
                .build();

            final String password = authHandler.getSqlAuthentication()
                .hash(
                    "pbkdf2",
                    VertxContextPRNG.current().nextString(32),
                    dto.getPassword()
                );

            user.setPassword(password);

            return session.persist(user)
                .call(session::flush)
                .replaceWith(ResponseWrapper.builder()
                    .success(true)
                    .timestamp(LocalDateTime.now())
                    .message("User created successfully")
                    .content(new JsonObject()
                        .put("username", dto.getUsername())
                        .put("role", dto.getRole()))
                    .build());
          });
        });

    router.get("/user")
        .putMetadata("allowedRoles", Set.of("root"))
        .handler(authHandler)
        .respond(context -> sessionFactory.withSession(session -> {
          final String username = context.queryParams().get("username");
          if (username != null) {
            return session.find(User.class, username);
          }

          return null;
        }).onItem()
            .ifNull()
            .fail()
            .onFailure()
            .call(() -> context.response().setStatusCode(404).end())
            .onItem()
            .call(() -> context.response().setStatusCode(200).end())
        );
  }
}
