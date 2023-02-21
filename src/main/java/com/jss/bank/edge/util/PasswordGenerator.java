package com.jss.bank.edge.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class PasswordGenerator {

  private static final Logger logger = LoggerFactory.getLogger(PasswordGenerator.class);
  final List<Character> specialCharacters = new ArrayList<>();
  final List<Character> alphabet = new ArrayList<>();

  public String generate(final int maxLength) {
    if (maxLength < 8) {
      logger.error("The max length need to be major than 8");
      return null;
    }

    final Random random = new Random();
    final int specialCharactersAmount = random.nextInt(1, 4);
    final char[] password = new char[maxLength];

    for (int i = 0; i < password.length; i++) {
      password[i] = alphabet.get(random.nextInt(0, alphabet.size()));
    }

    random
        .ints(specialCharactersAmount, 0, maxLength)
        .forEach(x -> password[x] = getRandomSpecialCharacter());

    return new String(password);
  }

  private char getRandomSpecialCharacter() {
    return specialCharacters.get(new Random().nextInt(0, specialCharacters.size()));
  }

  {
    IntStream.rangeClosed(33, 46).forEach(i -> specialCharacters.add((char) i));
    IntStream.rangeClosed(65, 90).forEach(i -> alphabet.add((char) i));
    IntStream.rangeClosed(97, 122).forEach(i -> alphabet.add((char) i));
    IntStream.rangeClosed(48, 57).forEach(i -> alphabet.add((char) i));
  }
}
