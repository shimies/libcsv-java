package io.github.shimies.csv.rfc4180;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.github.shimies.csv.CsvUtil;
import io.github.shimies.csv.ICsvParser;
import io.github.shimies.csv.Newline;
import io.github.shimies.csv.ParserException;
import io.github.shimies.csv.CsvUtil.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public abstract class ParserTest {

    protected ICsvParser parser;
    protected CsvUtil util;

    protected abstract boolean newlineBeforeEof();
    protected abstract int delimiter();
    protected abstract Newline newline();
    protected abstract boolean stripFields();
    protected abstract boolean allowRecordEndWithEmptyField();
    protected abstract boolean allowVariadicFields();
    protected abstract boolean allowSpaceEncloseEscaped();

    @BeforeEach
    protected void setup() {
        parser = ICsvParser.ofVariantRFC4180(delimiter(), stripFields(), allowRecordEndWithEmptyField(), allowVariadicFields(), allowSpaceEncloseEscaped());
        util = new CsvUtil(delimiter(), newline(), newlineBeforeEof());
    }

    @ParameterizedTest(name = "[{index}] nFields={0}")
    @MethodSource("nDimensionsProvider")
    protected void testVariousNumberOfFields(int nFields) throws IOException {
        String field = "  ";
        testRepeatedFields(field, false, 1, nFields);
    }

    @ParameterizedTest(name = "[{index}] nRecords={0}")
    @MethodSource("nDimensionsProvider")
    protected void testVariousNumberOfRecords(int nRecords) throws IOException {
        String field = "  ";
        testRepeatedFields(field, false, nRecords, 1);
    }

    @ParameterizedTest(name = "[{index}] nRecords={0},nFields={0}")
    @MethodSource("nDimensionsProvider")
    protected void testNumberOfRecordsWithSameElements(int ndims) throws IOException {
        String field = " this is a test! ";
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
    @MethodSource("escapedFieldsProvider")
    protected void testEscapedFieldsContainingEverythingElse(String field) throws IOException {
        int nRecords = 1;
        int nFields = 3;
        testRepeatedFields(field, true, nRecords, nFields);
    }

    @ParameterizedTest
    @ValueSource(strings = { "!", "0", "a", "A", " ", "\u3042", "!!", "  ", " ! 0 a A \u3042 " })
    protected void testNonEscapedFieldBMP(String field) throws IOException {
        int nRecords = 1;
        int nFields = 3;
        testRepeatedFields(field, false, nRecords, nFields);
    }

    @ParameterizedTest
    @ValueSource(strings = { "\ud800\udc0f", "\ud800\udc0f\u3042\ud800\udc0f\ud800\udc0f\u3042" })
    protected void testNonEscapedFieldSIP(String field) throws IOException {
        int nRecords = 1;
        int nFields = 3;
        testRepeatedFields(field, false, nRecords, nFields);
    }

    @ParameterizedTest
    @MethodSource("mixedFieldsProvider")
    protected void testNonEscapedAndEscapedMixedFields(List<Field> fields) throws IOException {
        int nRecords = 1;
        String csv = util.toCsvByDuplicatingFieldsRecord(fields, nRecords);
        List<List<String>> actual = parser.parseString(csv);
        List<String> expected = fields.stream().map(f -> f.field()).collect(Collectors.toList());
        assertEquals(nRecords, actual.size());
        for (List<String> record : actual) {
            assertEquals(expected, record);
        }
    }

    @Test
    protected void testEmptyCsv() throws IOException {
        String csv = "";
        List<List<String>> records = parser.parseString(csv);
        assertEquals(records.size(), 0);
    }

    @Test
    protected void testNewlineOnlyCsv() throws IOException {
        String csv = newline().toString();
        if (allowRecordEndWithEmptyField()) {
            List<List<String>> actual = parser.parseString(csv);
            assertEquals(1, actual.size());
            assertEquals(1, actual.get(0).size());
            assertTrue(actual.get(0).get(0).isEmpty());
        } else {
            ParserException e = assertThrows(ParserException.class, () -> parser.parseString(csv));
            assertTrue(e.getMessage().contains(ERROR_EMPTY_FIELD_FOLLOWED_BY_EOR));
            assertEquals(1, e.getLineNumber());
        }
    }

    @Test
    protected void testNonEscapedEmptyFields() throws IOException {
        int nRecords = 2;
        int nFields = 3;
        String empty = "";
        String csv = util.toCsvByDuplicatingField(empty, nRecords, nFields);
        if (allowRecordEndWithEmptyField()) {
            List<List<String>> actual = parser.parseString(csv);
            assertEquals(nRecords, actual.size());
            for (List<String> record : actual) {
                assertEquals(nFields, record.size());
                assertTrue(record.stream().allMatch(empty::equals));
            }
        } else {
            ParserException e = assertThrows(ParserException.class, () -> parser.parseString(csv));
            assertTrue(e.getMessage().contains(ERROR_EMPTY_FIELD_FOLLOWED_BY_EOR));
            assertEquals(1, e.getLineNumber());
        }
    }

    @Test
    protected void testEscapedEmptyFields() throws IOException {
        int nRecords = 2;
        int nFields = 3;
        String empty = "";
        String record = util.toCsvByDuplicatingField(empty, 1, nFields, false) + CsvUtil.escapeRFC4180("");
        String csv = util.toCsvByDuplicatingStringRecord(record, nRecords);
        List<List<String>> actual = parser.parseString(csv);
        List<List<String>> expected = CsvUtil.fillListOfListWith(empty, nRecords, nFields);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("spaceAddersProvider")
    protected void testEscapedFieldsSurroundedBySpaces(UnaryOperator<String> spaceAdder) throws IOException {
        int nRecords = 1;
        String rawField = " this is escaped ";
        String escapedField = '"' + rawField + '"';
        List<String> fields = Arrays.asList("abc", spaceAdder.apply(escapedField), "def");
        String csv = util.toCsvByDuplicatingFieldsRecord(fields, nRecords);
        if (allowSpaceEncloseEscaped()) {
            List<List<String>> actual = parser.parseString(csv);
            assertEquals(nRecords, actual.size());
            for (List<String> record : actual) {
                assertEquals(rawField, record.get(1));
            }
        } else {
            ParserException e = assertThrows(ParserException.class, () -> parser.parseString(csv));
            boolean contains1 = e.getMessage().contains(ERROR_DQUOTE_IN_NONESCAPED_FIELD);
            boolean contains2 = e.getMessage().contains(ERROR_ILLEGAL_CHAR_AFTER_ESCAPED_FIELD);
            assertTrue(contains1 || contains2);
            assertEquals(1, e.getLineNumber());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "!", "0", "a", "A", "\u3042", "!!", " ! 0 a A \u3042 " })
    protected void testEscapedFieldsFollowedByIllegalCharacters(String chars) throws IOException {
        int nRecords = 1;
        String rawField = " this is escaped ";
        String escapedField = '"' + rawField + '"' + chars;
        String csv = util.toCsvByDuplicatingFieldsRecord(Arrays.asList("abc", escapedField), nRecords);
        ParserException e = assertThrows(ParserException.class, () -> parser.parseString(csv));
        assertTrue(e.getMessage().contains(ERROR_ILLEGAL_CHAR_AFTER_ESCAPED_FIELD));
        assertEquals(1, e.getLineNumber());
    }

    @Test
    protected void testDquoteInNonEscapedField() throws IOException {
        int nRecords = 1;
        String csv = util.toCsvByDuplicatingFieldsRecord(Arrays.asList("abc", "non\"\""), nRecords);
        ParserException e = assertThrows(ParserException.class, () -> parser.parseString(csv));
        assertTrue(e.getMessage().contains(ERROR_DQUOTE_IN_NONESCAPED_FIELD));
        assertEquals(1, e.getLineNumber());
    }

    @Test
    protected void testEscapedFieldWithNoClosingDquote() {
        int nRecords = 1;
        String csv = util.toCsvByDuplicatingFieldsRecord(Arrays.asList("abc", "\"an escaped field"), nRecords);
        ParserException e = assertThrows(ParserException.class, () -> parser.parseString(csv));
        assertTrue(e.getMessage().contains(ERROR_EOF_REACHED_IN_ESCAPED_FIELD));
        assertEquals(1, e.getLineNumber());
    }

    @Test
    protected void testValiadicFields() throws IOException {
        List<List<String>> csvData = Arrays.asList(
            Arrays.asList("0"),
            Arrays.asList("0", "1"),
            Arrays.asList("0", "1", "2"),
            Arrays.asList("0"),
            Arrays.asList("0", "1"),
            Arrays.asList("0", "1", "2")
        );
        List<String> records = csvData.stream().map(xs -> util.toCsvByDuplicatingFieldsRecord(xs, 1, false)).collect(Collectors.toList());
        String csv = util.toCsvByJoiningRecords(records);
        if (allowVariadicFields()) {
            List<List<String>> actual = parser.parseString(csv);
            assertEquals(csvData, actual);
        } else {
            ParserException e = assertThrows(ParserException.class, () -> parser.parseString(csv));
            assertTrue(e.getMessage().contains(ERROR_ILLEGAL_NUMBER_OF_FIELDS));
            assertEquals(2, e.getLineNumber()); // expects an error on the 2nd line
        }
    }

    private void testRepeatedFields(String field, boolean mustEscape, int nRecords, int nFields) throws IOException {
        String fieldEscaped = field;
        if (mustEscape)
            fieldEscaped = CsvUtil.escapeRFC4180(fieldEscaped);
        String csv = util.toCsvByDuplicatingField(fieldEscaped, nRecords, nFields);
        List<List<String>> records = parser.parseString(csv);
        if (stripFields() && !mustEscape)
            field = field.trim(); // use trim() provided no unicode whitespaces are used in tests
        assertNotNull(records);
        assertEquals(nRecords, records.size());
        for (List<String> record : records) {
            assertEquals(nFields, record.size());
            assertTrue(record.stream().allMatch(field::equals));
        }
    }

    private static IntStream nDimensionsProvider() {
        return IntStream.of(1, 2, 3, 10, 50, 100, 256);
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

    private static Stream<Arguments> escapedFieldsProvider() {
        return Stream.of(
            Arguments.of(""),
            Arguments.of("!"),
            Arguments.of("0"),
            Arguments.of("A"),
            Arguments.of("a"),
            Arguments.of(" "),
            Arguments.of("\""),
            Arguments.of("\u3042"),
            Arguments.of("\ud800\udc0f"),
            Arguments.of("\n"),
            Arguments.of("\r"),
            Arguments.of("!!"),
            Arguments.of("  "),
            Arguments.of("\"\""),
            Arguments.of("\n\n"),
            Arguments.of("\r\r"),
            Arguments.of("\r\n"),
            Arguments.of("! 0 A a \" \n \r \r\n\u3042\ud800\udc0f\u3042\n")
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

    private static Stream<Arguments> spaceAddersProvider() {
        return Stream.of(
            Arguments.of((UnaryOperator<String>) s -> " " + s),
            Arguments.of((UnaryOperator<String>) s -> s + " "),
            Arguments.of((UnaryOperator<String>) s -> " " + s + " "),
            Arguments.of((UnaryOperator<String>) s -> "\u2003" + s),
            Arguments.of((UnaryOperator<String>) s -> s + "\u2003"),
            Arguments.of((UnaryOperator<String>) s -> "\u2003" + s + "\u2003"),
            Arguments.of((UnaryOperator<String>) s -> " \u2003 " + s),
            Arguments.of((UnaryOperator<String>) s -> s + " \u2003 "),
            Arguments.of((UnaryOperator<String>) s -> " \u2003 " + s + " \u2003 ")
        );
    }

    // Copied from CsvParserRFC4180.java
    private static final String ERROR_ILLEGAL_NUMBER_OF_FIELDS = "Illegal number of fields found";
    private static final String ERROR_EMPTY_FIELD_FOLLOWED_BY_EOR = "Empty field followed by end of record";
    private static final String ERROR_DQUOTE_IN_NONESCAPED_FIELD = "Double quotation found in non-escaped field";
    private static final String ERROR_EOF_REACHED_IN_ESCAPED_FIELD = "EOF reached while parsing escaped field";
    private static final String ERROR_ILLEGAL_CHAR_AFTER_ESCAPED_FIELD = "Escaped field followed by illegal character";

}
