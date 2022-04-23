package io.github.shimies.csv.rfc4180.parsers;

import io.github.shimies.csv.Newline;

public class TestCsvDelimiterSIP extends DefaultTest4CsvVariation {

    @Override
    protected boolean newlineBeforeEof() {
        return false;
    }

    @Override
    protected int delimiter() {
        return 0x20bb7;
    }

    @Override
    protected Newline newline() {
        return Newline.CRLF;
    }

}
