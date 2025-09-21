package io.github.shimies.csv.rfc4180.parsers;

import io.github.shimies.csv.Newline;
import io.github.shimies.csv.rfc4180.ParserTest;

public abstract class DefaultTest4ParserVariation extends ParserTest {

  @Override
  protected boolean newlineBeforeEof() {
    return true;
  }

  @Override
  protected int delimiter() {
    return ',';
  }

  @Override
  protected Newline newline() {
    return Newline.CRLF;
  }
}
