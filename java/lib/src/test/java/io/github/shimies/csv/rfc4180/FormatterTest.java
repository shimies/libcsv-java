package io.github.shimies.csv.rfc4180;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.shimies.csv.CsvUtil;
import io.github.shimies.csv.ICsvFormatter;
import io.github.shimies.csv.Newline;
import io.github.shimies.csv.CsvUtil.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public abstract class FormatterTest {

    private ICsvFormatter formatter;
    private CsvUtil util;

    protected abstract int delimiter();
    protected abstract Newline newline();
    protected abstract boolean allowRecordEndWithEmptyField();

    @BeforeEach
    protected void setup() {
        formatter = ICsvFormatter.ofRFC4180(delimiter(), newline(), allowRecordEndWithEmptyField());
        util = new CsvUtil(delimiter(), newline(), true);
    }

    @ParameterizedTest(name = "[{index}] dimenstion={0}x{0}")
    @ValueSource(ints = {1, 2, 3, 10, 50, 100, 256})
    protected void testVariousNumberOfFieldsAndRecords(int ndims) throws IOException {
        String field = "  this is a test!  ";
        testRepeatedFields(field, false, ndims, ndims);
    }

    @ParameterizedTest
    @MethodSource("elementsJoinedByDelimiterProvider")
    protected void testEscapedFieldsContainingDelimiter(List<String> xs, boolean prepend, boolean append) throws IOException {
        int nRecords = 1;
        int nFields = 3;
        String field = util.toCsvByDuplicatingFieldsRecord(xs, 1, false);
        String d = new String(Character.toChars(delimiter()));
        if (prepend)
            field = d + field;
        if (append)
            field = field + d;
        testRepeatedFields(field, true, nRecords, nFields);
    }

    @ParameterizedTest
    @ValueSource(strings = { "\r\n", "\n\r\n\r", "\na\n", "a\na", "\"", "\"\"", "\"a\"", "a\"a", "\n\"\n", "\"\n\"" })
    protected void testEscapedFieldsContainingNewlineAndDquote(String field) throws IOException {
        int nRecords = 2;
        int nFields = 3;
        testRepeatedFields(field, true, nRecords, nFields);
    }

    @ParameterizedTest
    @ValueSource(strings = { "!", "0", "A", "a", " ", "\u2003", "\u200b", "\u3042", "\u3044", "\u3042\u3044" })
    protected void testNonEscapedFieldsBMP(String field) throws IOException {
        // U+2003 ... Em Space
        // U+200b ... Zero With Space
        int nRecords = 1;
        int nFields = 3;
        testRepeatedFields(field, false, nRecords, nFields);
    }

    @ParameterizedTest
    @ValueSource(strings = { "\ud800\udc0f", "\ud800\udc0f\u3042\ud800\udc0f\ud800\udc0f\u3042" })
    protected void testNonEscapedFieldsSIP(String field) throws IOException {
        int nRecords = 1;
        int nFields = 3;
        testRepeatedFields(field, false, nRecords, nFields);
    }

    @ParameterizedTest
    @MethodSource("mixedFieldsProvider")
    protected void testNonEscapedAndEscapedMixedFields(List<Field> fields) throws IOException {
        int nRecords = 1;
        List<String> actualRecord = fields.stream().map(f -> f.field()).collect(Collectors.toList());
        String actualCsv = formatter.formatToString(CsvUtil.fillListWith(actualRecord, nRecords));
        String expectedCsv = util.toCsvByDuplicatingFieldsRecord(fields, nRecords);
        assertEquals(expectedCsv, actualCsv);
    }

    @Test
    protected void testEmptyCsv() throws IOException {
        String actual = formatter.formatToString(Arrays.asList());
        assertTrue(actual.isEmpty());
    }

    @Test
    protected void testEmptyFields() throws IOException {
        int nRecords = 3;
        int nFields = 3;
        String empty = "";
        String actualCsv = formatter.formatToString(CsvUtil.fillListOfListWith(empty, nRecords, nFields));
        String singleRecord = util.toCsvByDuplicatingFieldsRecord(CsvUtil.fillListWith(empty, nFields), 1, false);
        if (!allowRecordEndWithEmptyField())
            singleRecord = singleRecord + "\"\"";
        String expectedCsv = util.toCsvByDuplicatingStringRecord(singleRecord, nRecords);
        assertEquals(expectedCsv, actualCsv);
    }

    private void testRepeatedFields(String field, boolean mustEscape, int nRecords, int nFields) throws IOException {
        String actualCsv = formatter.formatToString(CsvUtil.fillListOfListWith(field, nRecords, nFields));
        String fieldEscaped = field;
        if (mustEscape)
            fieldEscaped = CsvUtil.escapeRFC4180(field);
        String expectedCsv = util.toCsvByDuplicatingField(fieldEscaped, nRecords, nFields);
        assertEquals(expectedCsv, actualCsv);
    }

    private static Stream<Arguments> elementsJoinedByDelimiterProvider() {
        return Stream.of(
            Arguments.of(Arrays.asList("", ""), false, false),
            Arguments.of(Arrays.asList("", "", ""), false, false),
            Arguments.of(Arrays.asList("a", "b", "c"), false, false),
            Arguments.of(Arrays.asList("a", "b", "c"), true, false),
            Arguments.of(Arrays.asList("a", "b", "c"), false, true),
            Arguments.of(Arrays.asList("a", "b", "c"), true, true),
            Arguments.of(Arrays.asList("\n", "\n"), false, false),
            Arguments.of(Arrays.asList("\"", "\""), false, false)
        );
    }

    private static Stream<Arguments> mixedFieldsProvider() {
        return Stream.of(
            Arguments.of(Arrays.asList(new Field("non", false), new Field("\"esc\"", true), new Field("non", false))),
            Arguments.of(Arrays.asList(new Field("\"esc\"", true), new Field("non", false), new Field("\"esc\"", true))),
            Arguments.of(Arrays.asList(new Field("non", false), new Field("\r\n", true), new Field("non", false))),
            Arguments.of(Arrays.asList(new Field("\r", true), new Field("non", false), new Field("\n", true)))
        );
    }

}
