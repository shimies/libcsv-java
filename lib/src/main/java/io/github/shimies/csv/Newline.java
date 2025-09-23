package io.github.shimies.csv;

/** Represents newline types for CSV files. */
public enum Newline {
  LF("\n"),
  CRLF("\r\n"),
  CR("\r");

  private final String expression;

  Newline(String expression) {
    this.expression = expression;
  }

  @Override
  public String toString() {
    return expression;
  }
}
