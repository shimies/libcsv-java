package io.github.shimies.csv.rfc4180.formatters;

import io.github.shimies.csv.Newline;
import io.github.shimies.csv.rfc4180.FormatterTest;

public class TestAllowEmptyField extends FormatterTest {

    @Override
    protected int delimiter() {
        return ',';
    }

    @Override
    protected boolean allowRecordEndWithEmptyField() {
        return true;
    }

    @Override
    protected Newline newline() {
        return Newline.CRLF;
    }

}
