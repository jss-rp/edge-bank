package com.jss.bank.edge.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

public class PasswordGeneratorTest {

  @Test
  public void createPasswordSuccessfully() {
    PasswordGenerator generator = new PasswordGenerator();
    String password = generator.generate(15);
    Pattern pattern = Pattern.compile("[a-zA-Z0-9$&+,:;=?@#|'<>.^*()%!-\\\"\\/\\(\\)]", Pattern.MULTILINE);
    Assertions.assertTrue(pattern.matcher(password).find());
    Assertions.assertEquals(15, password.length());
  }
}
