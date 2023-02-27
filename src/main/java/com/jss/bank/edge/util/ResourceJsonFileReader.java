package com.jss.bank.edge.util;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResourceJsonFileReader {

  public static final Logger logger = LoggerFactory.getLogger(ResourceJsonFileReader.class);

  public JsonObject read(final String filename) {
    try {
      final StringBuilder builder = new StringBuilder();
      final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      final InputStream resourceAsStream = classLoader.getResourceAsStream(filename);
      final BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));

      for (String line; (line = reader.readLine()) != null; ) {
        builder.append(line);
      }

      return new JsonObject(builder.toString());
    } catch (IOException e) {
      logger.info("Fail on reading file", e);
      throw new RuntimeException(e);
    }
  }
}
