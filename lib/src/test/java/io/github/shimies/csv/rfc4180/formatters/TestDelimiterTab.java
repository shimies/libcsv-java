package io.github.shimies.csv.rfc4180.formatters;

import io.github.shimies.csv.Newline;
import io.github.shimies.csv.rfc4180.FormatterTest;

public class TestDelimiterTab extends FormatterTest {

  @Override
  protected int delimiter() {
    return '\t';
  }

  @Override
  protected boolean allowRecordEndWithEmptyField() {
    return false;
  }

  @Override
  protected Newline newline() {
    return Newline.CRLF;
  }
}
