package io.github.shimies.csv.rfc4180.parsers;

public class TestParserAllowRecordEndWithEmptyField extends DefaultTest4ParserVariation {

  @Override
  protected boolean stripFields() {
    return false;
  }

  @Override
  protected boolean allowRecordEndWithEmptyField() {
    return true;
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
