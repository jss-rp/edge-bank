package com.jss.bank.edge.configutaion;

import com.jss.bank.edge.util.Environment;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.SqlClient;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.jss.bank.edge.util.Environment.DEV;

public class DBConfiguration {

  public static final Logger logger = LoggerFactory.getLogger(DBConfiguration.class);

  private static final Map<String, String> DB_CONFIGURATION = new HashMap<>();

  private static SqlClient CLIENT;


  public static void initialize(final Vertx vertx) {
    final MySQLConnectOptions connectOptions = new MySQLConnectOptions();

    readEnvironmentVars();
    connectOptions.setHost(DB_CONFIGURATION.get("DB_HOST"));
    connectOptions.setPort(Integer.parseInt(DB_CONFIGURATION.get("DB_PORT")));
    connectOptions.setDatabase(DB_CONFIGURATION.get("DB_SCHEMA"));
    connectOptions.setUser(DB_CONFIGURATION.get("DB_USERNAME"));
    connectOptions.setPassword(DB_CONFIGURATION.get("DB_PASSWORD"));

    final PoolOptions poolOptions = new PoolOptions();
    poolOptions.setMaxSize(Integer.parseInt(DB_CONFIGURATION.get("DB_POOL_SIZE")));

    CLIENT = MySQLPool.pool(vertx, connectOptions, poolOptions);

    logger.debug("Database configurations loaded successful");
  }

  public static SqlClient getSQLCLient() {
    return CLIENT;
  }

  public static Properties getDefaultPersistenceUnitProperties() {
    var properties = new Properties();
    var url = String.format("jdbc:mysql://%s:%s/%s",
        DB_CONFIGURATION.get("DB_HOST"),
        DB_CONFIGURATION.get("DB_PORT"),
        DB_CONFIGURATION.get("DB_SCHEMA"));

    properties.put("javax.persistence.jdbc.url", url);
    properties.put("javax.persistence.jdbc.user", DB_CONFIGURATION.get("DB_USERNAME"));
    properties.put("javax.persistence.jdbc.password", DB_CONFIGURATION.get("DB_PASSWORD"));
    properties.put("hibernate.connection.pool_size", DB_CONFIGURATION.get("DB_POOL_SIZE"));
    properties.put("javax.persistence.schema-generation.database.action", "none");
    properties.put("hibernate.show_sql", "false");

    return properties;
  }

  private static void readEnvironmentVars() {
    final Map<String, String> systemEnvVars = System.getenv();
    final String environment = systemEnvVars.getOrDefault("ENV", "dev");

    if(Environment.fromString(environment).equals(DEV)) {
      DB_CONFIGURATION.put("DB_HOST", "localhost");
      DB_CONFIGURATION.put("DB_PORT", "3306");
      DB_CONFIGURATION.put("DB_SCHEMA", "edge_bank");
      DB_CONFIGURATION.put("DB_USERNAME", "user");
      DB_CONFIGURATION.put("DB_PASSWORD", "secret");
      DB_CONFIGURATION.put("DB_POOL_SIZE", "10");
    } else {
      DB_CONFIGURATION.put("DB_HOST", systemEnvVars.get("DB_HOST"));
      DB_CONFIGURATION.put("DB_PORT", systemEnvVars.get("DB_PORT"));
      DB_CONFIGURATION.put("DB_SCHEMA", systemEnvVars.get("DB_SCHEMA"));
      DB_CONFIGURATION.put("DB_USERNAME", systemEnvVars.get("DB_USERNAME"));
      DB_CONFIGURATION.put("DB_PASSWORD", systemEnvVars.get("DB_PASSWORD"));
      DB_CONFIGURATION.put("DB_POOL_SIZE", systemEnvVars.get("DB_POOL_SIZE"));
    }
  }
}
