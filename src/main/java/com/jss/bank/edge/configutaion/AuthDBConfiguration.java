package com.jss.bank.edge.configutaion;

import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.SqlClient;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.PoolOptions;

public class AuthDBConfiguration {

  private static SqlClient CLIENT;

  public static void initialize(final Vertx vertx, final JsonObject config) {
    final MySQLConnectOptions connectOptions = new MySQLConnectOptions();
    connectOptions.setHost(config.getString("host", "localhost"));
    connectOptions.setPort(config.getInteger("port", 3306));
    connectOptions.setDatabase(config.getString("schema"));
    connectOptions.setUser(config.getString("username"));
    connectOptions.setPassword(config.getString("password"));

    final PoolOptions poolOptions = new PoolOptions();
    poolOptions.setMaxSize(config.getInteger("pool_size", 10));

    CLIENT = MySQLPool.pool(vertx, connectOptions, poolOptions);
  }

  public static SqlClient getSQLCLient() {
    return CLIENT;
  }
}
