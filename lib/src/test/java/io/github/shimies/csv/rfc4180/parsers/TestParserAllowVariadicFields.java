package io.github.shimies.csv.rfc4180.parsers;

public class TestParserAllowVariadicFields extends DefaultTest4ParserVariation {

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
        return true;
    }

    @Override
    protected boolean allowSpaceEncloseEscaped() {
        return false;
    }

}
