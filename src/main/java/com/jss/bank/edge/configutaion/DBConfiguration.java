package com.jss.bank.edge.configutaion;

import com.jss.bank.edge.util.Environment;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.SqlClient;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

import static com.jss.bank.edge.util.Environment.DEV;

public class DBConfiguration {

  public static final Logger logger = LoggerFactory.getLogger(DBConfiguration.class);

  private static SqlClient CLIENT;

  private final String host;

  private final Integer port;

  private final String schema;

  private final String username;

  private final String password;

  private final Integer poolSize;

  private static DBConfiguration CONFIGURATION;

  private DBConfiguration(
      final String host,
      final Integer port,
      final String schema,
      final String username,
      final String password,
      final Integer poolSize
  ) {
    this.host = host;
    this.port = port;
    this.schema = schema;
    this.username = username;
    this.password = password;
    this.poolSize = poolSize;
  }

  public static void initialize(final Vertx vertx) {
    final MySQLConnectOptions connectOptions = new MySQLConnectOptions();

    CONFIGURATION = readEnvironmentVars();
    connectOptions.setHost(CONFIGURATION.host);
    connectOptions.setPort(CONFIGURATION.port);
    connectOptions.setDatabase(CONFIGURATION.schema);
    connectOptions.setUser(CONFIGURATION.username);
    connectOptions.setPassword(CONFIGURATION.password);

    final PoolOptions poolOptions = new PoolOptions();
    poolOptions.setMaxSize(CONFIGURATION.poolSize);

    CLIENT = MySQLPool.pool(vertx, connectOptions, poolOptions);

    logger.debug("Database configurations loaded successful");
  }

  public static SqlClient getSQLCLient() {
    return CLIENT;
  }

  public static Properties getDefaultPersistenceUnitProperties() {
    var properties = new Properties();
    var url = String.format("jdbc:mysql://%s:%s/%s",
        CONFIGURATION.host,
        CONFIGURATION.port,
        CONFIGURATION.schema);

    properties.put("javax.persistence.jdbc.url", url);
    properties.put("javax.persistence.jdbc.user", CONFIGURATION.username);
    properties.put("javax.persistence.jdbc.password", CONFIGURATION.password);
    properties.put("hibernate.connection.pool_size", CONFIGURATION.poolSize);
    properties.put("javax.persistence.schema-generation.database.action", "none");
    properties.put("hibernate.show_sql", "false");

    return properties;
  }

  private static DBConfiguration readEnvironmentVars() {
    final Map<String, String> systemEnvVars = System.getenv();
    final String environment = systemEnvVars.getOrDefault("ENV", "dev");

    if(Environment.fromString(environment).equals(DEV)) {
      return new DBConfiguration("localhost", 3306, "edge_bank", "user", "secret", 10);
    }

    return new DBConfiguration(
        systemEnvVars.get("DB_HOST"),
        Integer.parseInt(systemEnvVars.get("DB_PORT")),
        systemEnvVars.get("DB_SCHEMA"),
        systemEnvVars.get("DB_USERNAME"),
        systemEnvVars.get("DB_PASSWORD"),
        Integer.parseInt(systemEnvVars.get("DB_POOL_SIZE"))
    );
  }
}
