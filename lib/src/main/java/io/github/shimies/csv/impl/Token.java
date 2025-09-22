package io.github.shimies.csv.impl;

import java.io.IOException;

public class Token<T extends Enum<T>> {

  private final T type;
  private final Object value;

  public Token(T type, Object value) {
    this.type = type;
    this.value = value;
  }

  public T getKind() {
    return type;
  }

  public Object getValue() {
    return value;
  }

  public interface Tokenizer<T extends Enum<T>> {

    boolean hasNext();

    Token<T> nextToken() throws IOException;
  }
}
