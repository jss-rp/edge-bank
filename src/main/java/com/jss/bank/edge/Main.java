package com.jss.bank.edge;

import com.jss.bank.edge.util.ResourceJsonFileReader;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class Main {

  public static void main(String[] args){
    final ResourceJsonFileReader fileReader = new ResourceJsonFileReader();
    final JsonObject configurations = fileReader.read("config.json");
    final Vertx vertx = Vertx.vertx();

    vertx.deployVerticle(MainVerticle::new, new DeploymentOptions().setConfig(configurations));
  }
}
