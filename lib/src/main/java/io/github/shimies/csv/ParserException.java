package io.github.shimies.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Exception thrown when a parsing error occurs, with location information. */
public class ParserException extends IOException implements TextLocator {

  private final int lineNo;
  private final int characterNo;

  /**
   * Constructs with a message, line number, and character number.
   *
   * @param message the error message
   * @param lineNo the line number where the error occurred
   * @param characterNo the character number where the error occurred
   */
  public ParserException(String message, int lineNo, int characterNo) {
    super(message);
    this.lineNo = lineNo;
    this.characterNo = characterNo;
  }

  /**
   * Constructs with a message and line number.
   *
   * @param message the error message
   * @param lineNo the line number where the error occurred
   */
  public ParserException(String message, int lineNo) {
    this(message, lineNo, -1);
  }

  /**
   * Constructs with a message and {@link TextLocator}.
   *
   * @param message the error message
   * @param locator the text locator providing line and character numbers
   */
  public ParserException(String message, TextLocator locator) {
    this(message, locator.getLineNumber(), locator.getCharacterNumber());
  }

  /**
   * Returns the line number where the error occurred.
   *
   * @return the line number
   */
  @Override
  public int getLineNumber() {
    return lineNo;
  }

  /**
   * Returns the character number where the error occurred.
   *
   * @return the character number
   */
  @Override
  public int getCharacterNumber() {
    return characterNo;
  }

  @Override
  public String getMessage() {
    return appendTextLocation(super.getMessage());
  }

  private String appendTextLocation(String base) {
    List<String> xs = new ArrayList<>();
    if (lineNo >= 0) {
      xs.add("L" + lineNo);
    }
    if (characterNo >= 0) {
      xs.add("C" + characterNo);
    }
    if (xs.isEmpty()) {
      return base;
    }
    return base + ' ' + '[' + String.join(",", xs) + ']';
  }
}
