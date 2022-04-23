package io.github.shimies.csv;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvUtil {

    private static char CHAR_DQUOTE = '"';

    private final String delimiter;
    private final Newline newline;
    private final boolean addNewlineBeforeEof;

    public CsvUtil(int delimiter, Newline newline, boolean addNewlineBeforeEof) {
        this.delimiter = new String(Character.toChars(delimiter));
        this.newline = newline;
        this.addNewlineBeforeEof = addNewlineBeforeEof;
    }

    public String toCsvByJoiningRecords(Iterable<String> records) {
        return toCsvByJoiningRecords(records, addNewlineBeforeEof);
    }

    public String toCsvByJoiningRecords(Iterable<String> records, boolean newlineBeforeEof) {
        String nl = newline.toString();
        String csvStr = String.join(nl, records);
        if (newlineBeforeEof)
            csvStr = csvStr + nl;
        return csvStr;
    }

    public String toCsvByDuplicatingField(String field, int nrows, int ncols) {
        return toCsvByDuplicatingField(field, nrows, ncols, addNewlineBeforeEof);
    }

    public String toCsvByDuplicatingField(String field, int nrows, int ncols, boolean newlineBeforeEof) {
        String record = String.join(delimiter, fillListWith(field, ncols));
        return toCsvByDuplicatingStringRecord(record, nrows, newlineBeforeEof);
    }

    public String toCsvByDuplicatingFieldsRecord(Iterable<String> fields, int nrows) {
        return toCsvByDuplicatingFieldsRecord(fields, nrows, addNewlineBeforeEof);
    }

    public String toCsvByDuplicatingFieldsRecord(Iterable<String> fields, int nrows, boolean newlineBeforeEof) {
        String record = String.join(delimiter, fields);
        return toCsvByDuplicatingStringRecord(record, nrows, newlineBeforeEof);
    }

    public String toCsvByDuplicatingFieldsRecord(Collection<Field> fields, int nrows) {
        return toCsvByDuplicatingFieldsRecord(fields, nrows, addNewlineBeforeEof);
    }

    public String toCsvByDuplicatingFieldsRecord(Collection<Field> fields, int nrows, boolean newlineBeforeEof) {
        String record = fields.stream().map(f -> f.mustEscape ? escapeRFC4180(f.field) : f.field).collect(Collectors.joining(delimiter));
        return toCsvByDuplicatingStringRecord(record, nrows, newlineBeforeEof);
    }

    public String toCsvByDuplicatingStringRecord(String record, int nrows) {
        return toCsvByDuplicatingStringRecord(record, nrows, addNewlineBeforeEof);
    }

    public String toCsvByDuplicatingStringRecord(String record, int nrows, boolean newlineBeforeEof) {
        String nl = newline.toString();
        String csvStr = String.join(nl, fillListWith(record, nrows));
        if (newlineBeforeEof)
            csvStr = csvStr + nl;
        return csvStr;
    }

    public static <T> List<T> fillListWith(T element, int n) {
        return Stream.generate(() -> element).limit(n).collect(Collectors.toList());
    }

    public static <T> List<List<T>> fillListOfListWith(T element, int n, int m) {
        return fillListWith(fillListWith(element, m), n);
    }

    public static String escapeRFC4180(String field) {
        return surround(duplicateChar(field, CHAR_DQUOTE), CHAR_DQUOTE, CHAR_DQUOTE);
    }

    private static String surround(String str, char start, char end) {
        StringBuilder sb = new StringBuilder();
        sb.append(start);
        sb.append(str);
        sb.append(end);
        return sb.toString();
    }

    private static String duplicateChar(String str, char target) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            sb.append(c);
            if (c == target)
                sb.append(c);
        }
        return sb.toString();
    }

    public static class Field {

        private final String field;
        private final boolean mustEscape;

        public Field(String field, boolean mustEscape) {
            this.field = field;
            this.mustEscape = mustEscape;
        }

        public String field() {
            return field;
        }

        public boolean mustEscape() {
            return mustEscape;
        }

    }

}
