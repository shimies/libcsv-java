package io.github.shimies.csv;

public enum Newline {
  LF("\n"),
  CRLF("\r\n"),
  CR("\r");

  private final String expression;

  private Newline(String expression) {
    this.expression = expression;
  }

  @Override
  public String toString() {
    return expression;
  }
}
