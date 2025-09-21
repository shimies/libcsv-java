package io.github.shimies.csv.rfc4180.parsers;

public class TestParserStripFields extends DefaultTest4ParserVariation {

  @Override
  protected boolean stripFields() {
    return true;
  }

  @Override
  protected boolean allowRecordEndWithEmptyField() {
    return false;
  }

  @Override
  protected boolean allowVariadicFields() {
    return false;
  }

  @Override
  protected boolean allowSpaceEncloseEscaped() {
    return false;
  }
}
