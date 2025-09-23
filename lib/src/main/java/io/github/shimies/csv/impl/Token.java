package io.github.shimies.csv.impl;

import java.io.IOException;

/**
 * Represents a token with a type and value, used in CSV parsing and formatting.
 *
 * @param <T> the enum type representing the kind of token
 */
public class Token<T extends Enum<T>> {

  private final T type;
  private final Object value;

  /**
   * Constructs with the specified type and value.
   *
   * @param type the type of the token
   * @param value the value of the token
   */
  public Token(T type, Object value) {
    this.type = type;
    this.value = value;
  }

  /**
   * Returns the type of this token.
   *
   * @return the token type
   */
  public T getKind() {
    return type;
  }

  /**
   * Returns the value of this token.
   *
   * @return the token value
   */
  public Object getValue() {
    return value;
  }

  /**
   * Tokenizer interface for iterating over tokens of a specific enum type.
   *
   * @param <T> the enum type representing the kind of token
   */
  public interface Tokenizer<T extends Enum<T>> {

    /**
     * Returns true if there are more tokens available.
     *
     * @return true if more tokens are available
     */
    boolean hasNext();

    /**
     * Returns the next token in the sequence.
     *
     * @return the next token
     * @throws IOException if an I/O error occurs
     */
    Token<T> nextToken() throws IOException;
  }
}
