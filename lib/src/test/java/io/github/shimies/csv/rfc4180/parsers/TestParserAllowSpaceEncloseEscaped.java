package io.github.shimies.csv.rfc4180.parsers;

public class TestParserAllowSpaceEncloseEscaped extends DefaultTest4ParserVariation {

  @Override
  protected boolean stripFields() {
    return false;
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
    return true;
  }
}
