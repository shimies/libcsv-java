package io.github.shimies.csv.rfc4180.parsers;

import io.github.shimies.csv.Newline;

public class TestCsvNewlineLF extends DefaultTest4CsvVariation {

    @Override
    protected boolean newlineBeforeEof() {
        return false;
    }

    @Override
    protected int delimiter() {
        return ',';
    }

    @Override
    protected Newline newline() {
        return Newline.LF;
    }

}
