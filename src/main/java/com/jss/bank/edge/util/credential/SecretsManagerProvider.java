package com.jss.bank.edge.util.credential;

import com.jss.bank.edge.Application;
import com.jss.bank.edge.util.AvailableEnvironment;
import com.jss.bank.edge.util.IntegratedService;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

public class SecretsManagerProvider implements CredentialProvider {

  public static final Logger logger = LoggerFactory.getLogger(SecretsManagerProvider.class);

  private final AvailableEnvironment environment;
  private final SecretsManagerClient client;

  private final JsonObject configurations;

  public SecretsManagerProvider() {
    final String env = System.getenv("ENV");
    final String region = System.getenv("AWS_REGION");
    this.environment = AvailableEnvironment.fromString(env);
    this.configurations = Application.getConfigurations();
    this.client = SecretsManagerClient.builder()
        .region(region == null ? Region.US_WEST_2 : Region.of(region))
        .build();
    logger.info("The application started at {} environment", env.toUpperCase());
  }

  @Override
  public JsonObject getCrendentials(final IntegratedService service) {
    return switch (service) {
      case DATABASE -> requestCredentials(service);
    };
  }

  private JsonObject requestCredentials(final IntegratedService service) {
    return switch (environment) {
      case DEV -> configurations.getJsonObject(service.getName(), new JsonObject());
      case STG, HOM, PROD -> new JsonObject(client.getSecretValue(GetSecretValueRequest.builder()
              .secretId(service.name())
              .build())
          .secretString());
    };
  }
}
