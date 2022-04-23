package io.github.shimies.csv.rfc4180.parsers;

import io.github.shimies.csv.rfc4180.ParserTest;

public abstract class DefaultTest4CsvVariation extends ParserTest {

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
        return false;
    }

}
