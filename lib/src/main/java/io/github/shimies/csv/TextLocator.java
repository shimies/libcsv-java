package io.github.shimies.csv;

/** Provides location information (line and character number) for text parsing. */
public interface TextLocator {

  /**
   * Returns the line number in the text.
   *
   * @return the line number
   */
  int getLineNumber();

  /**
   * Returns the character number in the text.
   *
   * @return the character number
   */
  int getCharacterNumber();
}
