package io.github.shimies.csv.impl;

import static org.assertj.core.api.Assertions.*;

import io.github.shimies.csv.*;
import java.io.IOException;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

/** Test suite for {@link CsvParserRfc4180} covering RFC 4180 compliance and edge cases. */
public class CsvParserRfc4180Test {

  @ParameterizedTest
  @MethodSource
  void emptyInputIsParsedGeneratingNothing(boolean a, boolean b, boolean c, boolean d)
      throws IOException {
    var target = new CsvParserRfc4180(',', a, b, c, d);

    var actualRecords = target.parseString("");
    assertThat(actualRecords).isEmpty();
  }

  static Stream<Arguments> emptyInputIsParsedGeneratingNothing() {
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(booleans, booleans, booleans, booleans);
  }

  @ParameterizedTest
  @MethodSource
  void parsingCsvFilledWithSameElementSucceeds(
      CsvTool csvTool, Dimension shape, boolean a, boolean b, boolean c) throws IOException {
    var target = new CsvParserRfc4180(csvTool.delimiter(), false, a, b, c);
    var csvData = CsvTool.listOfListFilledWith(" ", shape.recordCount(), shape.fieldCount());
    var csv = csvTool.toCsvFromStrings(csvData);

    var actualRecords = target.parseString(csv);
    assertThat(actualRecords).containsExactlyElementsOf(csvData);
  }

  static Stream<Arguments> parsingCsvFilledWithSameElementSucceeds() {
    var dimensions =
        List.of(
            new Dimension(1, 1),
            new Dimension(1, 2),
            new Dimension(1, 10),
            new Dimension(1, 500),
            new Dimension(2, 1),
            new Dimension(10, 1),
            new Dimension(500, 1),
            new Dimension(2, 2),
            new Dimension(10, 10),
            new Dimension(500, 500));
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(
        csvToolsProvider(), dimensions, booleans, booleans, booleans);
  }

  @ParameterizedTest
  @EnumSource(Newline.class)
  void newlineOnlyCsvCannotBeParsedIfEmptyFieldIsNotAllowedAtEndOfRecord(Newline newline) {
    var allowEmptyEnding = false;
    var target = new CsvParserRfc4180(',', false, allowEmptyEnding, false, false);

    assertThatThrownBy(() -> target.parseString(newline.toString()))
        .isInstanceOf(ParserException.class)
        .hasMessageContaining(ERROR_EMPTY_FIELD_FOLLOWED_BY_EOR)
        .satisfies(
            e -> {
              var exception = (ParserException) e;
              assertThat(exception.getLineNumber()).isEqualTo(1);
            });
  }

  @ParameterizedTest
  @EnumSource(Newline.class)
  void newlineOnlyCsvIsParsedAsSingleRecordContainingEmptyStringIfEmptyFieldIsAllowedAtEndOfRecord(
      Newline newline) throws IOException {
    var allowEmptyEnding = true;
    var target = new CsvParserRfc4180(',', false, allowEmptyEnding, false, false);

    var actualRecords = target.parseString(newline.toString());
    assertThat(actualRecords).containsExactly(List.of(""));
  }

  @ParameterizedTest
  @MethodSource
  void spacesAroundFieldAreLeftUntouched(
      CsvTool csvTool, UnaryOperator<String> spaceAdder, boolean a) throws IOException {
    var target = new CsvParserRfc4180(csvTool.delimiter(), false, false, false, a);
    var record = List.of(spaceAdder.apply("a"), "bb", spaceAdder.apply("ccc"), "dddd");
    var csv = csvTool.toCsvFromStrings(List.of(record));

    var actualRecords = target.parseString(csv);
    assertThat(actualRecords).hasSize(1);
    assertThat(actualRecords.get(0)).containsExactlyElementsOf(record);
  }

  static Stream<Arguments> spacesAroundFieldAreLeftUntouched() {
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(csvToolsProvider(), spaceAdderProvider(), booleans);
  }

  @ParameterizedTest
  @MethodSource
  void spacesAroundFieldAreStrippedIfConfiguredToDoSo(
      CsvTool csvTool, UnaryOperator<String> spaceAdder, boolean a) throws IOException {
    var target = new CsvParserRfc4180(csvTool.delimiter(), true, false, false, a);
    var record = List.of(spaceAdder.apply("a"), "bb", spaceAdder.apply("ccc"), "dddd");
    var csv = csvTool.toCsvFromStrings(List.of(record));

    var actualRecords = target.parseString(csv);
    assertThat(actualRecords).hasSize(1);
    assertThat(actualRecords.get(0)).containsExactly("a", "bb", "ccc", "dddd");
  }

  static Stream<Arguments> spacesAroundFieldAreStrippedIfConfiguredToDoSo() {
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(csvToolsProvider(), spaceAdderProvider(), booleans);
  }

  @ParameterizedTest
  @MethodSource
  void escapedFieldIsParsedUnescapedRegardlessOfNecessityOfEscape(
      CsvTool csvTool, String field, Dimension shape, boolean a) throws IOException {
    var target = new CsvParserRfc4180(csvTool.delimiter(), false, false, false, a);
    var escapedCsvData =
        CsvTool.listOfListFilledWith(
            CsvTool.escapeRfc4180(field),
            shape.recordCount(),
            shape.fieldCount()); // e.g. field := "A"
    var csv = csvTool.toCsvFromStrings(escapedCsvData);
    var csvData =
        CsvTool.listOfListFilledWith(
            field, shape.recordCount(), shape.fieldCount()); // e.g. field := A

    var actualRecords = target.parseString(csv);
    assertThat(actualRecords).containsExactlyElementsOf(csvData);
  }

  static Stream<Arguments> escapedFieldIsParsedUnescapedRegardlessOfNecessityOfEscape() {
    var fields =
        List.of(
            "",
            "!",
            "A",
            " ",
            "\u3042", // BMP
            "\ud800\udc0f", // SIP
            "\n",
            "\r",
            "\n\n",
            "\r\r",
            "\r\n", // newline(s)
            "\"",
            "\"\"", // double-quote(s)
            "! 0 A a \" \n \r \r\n\u3042\ud800\udc0f\u3042\n");
    var dimensions = List.of(new Dimension(1, 2), new Dimension(1, 1), new Dimension(2, 1));
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(csvToolsProvider(), fields, dimensions, booleans);
  }

  @ParameterizedTest
  @MethodSource
  void escapedFieldIsParsedUnescapedEvenWhenContainingDelimiters(
      CsvTool csvTool, List<String> xs, boolean a) throws IOException {
    var target = new CsvParserRfc4180(csvTool.delimiter(), false, false, false, a);
    // use delimiter-separated record as field (so the field contains delimiter)
    var field = csvTool.toCsvRecordFromStrings(xs);
    var escapedCsvData =
        CsvTool.listOfListFilledWith(CsvTool.escapeRfc4180(field), 1, 3); // e.g. field := "a,b,c"
    var csv = csvTool.toCsvFromStrings(escapedCsvData);
    var csvData = CsvTool.listOfListFilledWith(field, 1, 3); // e.g. field := a,b,c

    var actualRecords = target.parseString(csv);
    assertThat(actualRecords).containsExactlyElementsOf(csvData);
  }

  static Stream<Arguments> escapedFieldIsParsedUnescapedEvenWhenContainingDelimiters() {
    var xsSet =
        List.of(
            List.of("", ""), // ","
            List.of("", "", ""), // ",,"
            List.of("a", "b", "c"), // "a,b,c"
            List.of("", "a", "b", "c"), // ",a,b,c"
            List.of("a", "b", "c", ""), // "a,b,c,"
            List.of("", "a", "b", "c", ""), // ",a,b,c,d,"
            List.of("\n", "\n"),
            List.of("", "\n", "\n"),
            List.of("\n", "\n", ""),
            List.of("\"", "\""),
            List.of("", "\"", "\""),
            List.of("\"", "\"", ""));
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(csvToolsProvider(), xsSet, booleans);
  }

  @ParameterizedTest
  @MethodSource
  void escapedFieldMixedWithNonEscapedFieldAreAllParsed(
      CsvTool csvTool, List<CsvTool.Field> record, boolean a) throws IOException {
    var target = new CsvParserRfc4180(csvTool.delimiter(), false, false, false, a);
    var recordCount = 1;
    var csv = csvTool.toCsvFromFields(CsvTool.listFilledWith(record, recordCount));

    var actualRecords = target.parseString(csv);
    var expected = record.stream().map(CsvTool.Field::field).toList();
    assertThat(actualRecords).containsOnly(expected);
  }

  static Stream<Arguments> escapedFieldMixedWithNonEscapedFieldAreAllParsed() {
    var fieldsSet =
        List.of(
            List.of(
                new CsvTool.Field("non", false),
                new CsvTool.Field("\"esc\"", true),
                new CsvTool.Field("non", false)),
            List.of(
                new CsvTool.Field("\"esc\"", true),
                new CsvTool.Field("non", false),
                new CsvTool.Field("\"esc\"", true)),
            List.of(
                new CsvTool.Field("non", false),
                new CsvTool.Field("\r\n", true),
                new CsvTool.Field("non", false)),
            List.of(
                new CsvTool.Field("\r", true),
                new CsvTool.Field("non", false),
                new CsvTool.Field("\n", true)));
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(csvToolsProvider(), fieldsSet, booleans);
  }

  @ParameterizedTest
  @MethodSource
  void parsingVariadicFieldsRaisesErrorIfNotAllowed(CsvTool csvTool, boolean a, boolean b) {
    var allowVariadicFields = false;
    var target = new CsvParserRfc4180(csvTool.delimiter(), false, a, allowVariadicFields, b);
    var csvData = List.of(List.of("0"), List.of("0", "1"), List.of("0", "1", "2"));
    var csv = csvTool.toCsvFromStrings(csvData);

    assertThatThrownBy(() -> target.parseString(csv))
        .isInstanceOf(ParserException.class)
        .hasMessageContaining(ERROR_ILLEGAL_NUMBER_OF_FIELDS)
        .satisfies(
            e -> {
              var exception = (ParserException) e;
              assertThat(exception.getLineNumber()).isEqualTo(2);
            });
  }

  static Stream<Arguments> parsingVariadicFieldsRaisesErrorIfNotAllowed() {
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(csvToolsProvider(), booleans, booleans);
  }

  @ParameterizedTest
  @MethodSource
  void parsingVariadicFieldsSucceedsIfAllowed(CsvTool csvTool, boolean a, boolean b)
      throws IOException {
    var allowVariadicFields = true;
    var target = new CsvParserRfc4180(csvTool.delimiter(), false, a, allowVariadicFields, b);
    var csvData = List.of(List.of("0"), List.of("0", "1"), List.of("0", "1", "2"));
    var csv = csvTool.toCsvFromStrings(csvData);

    var actualRecords = target.parseString(csv);
    assertThat(actualRecords).containsExactlyElementsOf(csvData);
  }

  static Stream<Arguments> parsingVariadicFieldsSucceedsIfAllowed() {
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(csvToolsProvider(), booleans, booleans);
  }

  @ParameterizedTest
  @MethodSource
  void emptyFieldAtEndOfRecordRaisesParseErrorIfNotAllowed(CsvTool csvTool, boolean a, boolean b) {
    var target = new CsvParserRfc4180(csvTool.delimiter(), a, false, false, b);
    var csv = csvTool.toCsvFromStrings(CsvTool.listOfListFilledWith("", 2, 3));

    assertThatThrownBy(() -> target.parseString(csv))
        .isInstanceOf(ParserException.class)
        .hasMessageContaining(ERROR_EMPTY_FIELD_FOLLOWED_BY_EOR)
        .satisfies(
            e -> {
              var exception = (ParserException) e;
              assertThat(exception.getLineNumber()).isEqualTo(1);
              assertThat(exception.getCharacterNumber()).isBetween(3, 4);
            });
  }

  static Stream<Arguments> emptyFieldAtEndOfRecordRaisesParseErrorIfNotAllowed() {
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(csvToolsProvider(), booleans, booleans);
  }

  @ParameterizedTest
  @MethodSource
  void emptyFieldAtEndOfRecordIsParsedAsEmptyStringIfAllowed(CsvTool csvTool, boolean a, boolean b)
      throws IOException {
    var target = new CsvParserRfc4180(csvTool.delimiter(), a, true, false, b);
    var csvData = CsvTool.listOfListFilledWith("", 2, 3);
    var csv = csvTool.toCsvFromStrings(csvData);

    var actualRecords = target.parseString(csv);
    assertThat(actualRecords).containsExactlyElementsOf(csvData);
  }

  static Stream<Arguments> emptyFieldAtEndOfRecordIsParsedAsEmptyStringIfAllowed() {
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(csvToolsProvider(), booleans, booleans);
  }

  @ParameterizedTest
  @MethodSource
  void emptyFieldAtEndOfRecordCanBeParsedIfEscaped(
      CsvTool csvTool, boolean a, boolean b, boolean c, boolean d) throws IOException {
    var target = new CsvParserRfc4180(csvTool.delimiter(), a, b, c, d);
    var recordCount = 2;
    var fields =
        List.of(
            new CsvTool.Field("", false),
            new CsvTool.Field("", false),
            new CsvTool.Field("", true));
    var csv = csvTool.toCsvFromFields(CsvTool.listFilledWith(fields, recordCount));

    var actualRecord = target.parseString(csv);
    assertThat(actualRecord)
        .containsExactlyElementsOf(CsvTool.listFilledWith(List.of("", "", ""), recordCount));
  }

  static Stream<Arguments> emptyFieldAtEndOfRecordCanBeParsedIfEscaped() {
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(
        csvToolsProvider(), booleans, booleans, booleans, booleans);
  }

  @ParameterizedTest
  @MethodSource
  void escapedFieldSurroundedBySpacesRaisesParseErrorIfNotAllowed(
      CsvTool csvTool, UnaryOperator<String> spaceAdder, boolean a, boolean b) {
    var target = new CsvParserRfc4180(csvTool.delimiter(), a, b, false, false);
    var recordCount = 1;
    var escapedField = CsvTool.escapeRfc4180(" this is escaped ");
    var fields = List.of("abc", spaceAdder.apply(escapedField), "def");
    var csv = csvTool.toCsvFromStrings(CsvTool.listFilledWith(fields, recordCount));

    assertThatThrownBy(() -> target.parseString(csv))
        .isInstanceOf(ParserException.class)
        .hasMessageMatching(
            String.format(
                "(%s|%s).*",
                ERROR_ILLEGAL_CHAR_AFTER_ESCAPED_FIELD, ERROR_DQUOTE_IN_NONESCAPED_FIELD))
        .satisfies(
            e -> {
              var exception = (ParserException) e;
              assertThat(exception.getLineNumber()).isEqualTo(1);
            });
  }

  static Stream<Arguments> escapedFieldSurroundedBySpacesRaisesParseErrorIfNotAllowed() {
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(
        csvToolsProvider(), spaceAdderProvider(), booleans, booleans);
  }

  @ParameterizedTest
  @MethodSource
  void escapedFieldSurroundedBySpacesIsParsedAsIfThereAreNoSpacesIfAllowed(
      CsvTool csvTool, UnaryOperator<String> spaceAdder, boolean a, boolean b) throws IOException {
    var target = new CsvParserRfc4180(csvTool.delimiter(), a, b, false, true);
    var recordCount = 1;
    var rawField = " this is escaped ";
    var escapedField = CsvTool.escapeRfc4180(rawField);
    var fields = List.of("abc", spaceAdder.apply(escapedField), "def");
    var csv = csvTool.toCsvFromStrings(CsvTool.listFilledWith(fields, recordCount));

    var actualRecords = target.parseString(csv);
    assertThat(actualRecords).hasSize(1);
    assertThat(actualRecords.get(0)).containsExactly("abc", rawField, "def");
  }

  static Stream<Arguments> escapedFieldSurroundedBySpacesIsParsedAsIfThereAreNoSpacesIfAllowed() {
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(
        csvToolsProvider(), spaceAdderProvider(), booleans, booleans);
  }

  @ParameterizedTest
  @MethodSource
  void escapedFieldFollowedByIllegalCharactersRaisesParseError(
      CsvTool csvTool, String chars, boolean a, boolean b, boolean c, boolean d) {
    var target = new CsvParserRfc4180(csvTool.delimiter(), a, b, c, d);
    var recordCount = 1;
    var escapedField = CsvTool.escapeRfc4180("this is escaped") + chars;
    var csv =
        csvTool.toCsvFromStrings(CsvTool.listFilledWith(List.of("abc", escapedField), recordCount));

    assertThatThrownBy(() -> target.parseString(csv))
        .isInstanceOf(ParserException.class)
        .hasMessageContaining(ERROR_ILLEGAL_CHAR_AFTER_ESCAPED_FIELD)
        .satisfies(
            e -> {
              var exception = (ParserException) e;
              assertThat(exception.getLineNumber()).isEqualTo(1);
              assertThat(exception.getCharacterNumber()).isBetween(22, 23);
            });
  }

  static Stream<Arguments> escapedFieldFollowedByIllegalCharactersRaisesParseError() {
    var appendedStringSet = List.of("!", "A", "\u3042", " ! 0 a A \u3042 ");
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(
        csvToolsProvider(), appendedStringSet, booleans, booleans, booleans, booleans);
  }

  @ParameterizedTest
  @MethodSource
  void doubleQuoteInNonEscapedFieldRaisesParseError(
      CsvTool csvTool, boolean a, boolean b, boolean c, boolean d) {
    var target = new CsvParserRfc4180(csvTool.delimiter(), a, b, c, d);
    var recordCount = 1;
    var csv =
        csvTool.toCsvFromStrings(CsvTool.listFilledWith(List.of("abc", "non\"\""), recordCount));

    assertThatThrownBy(() -> target.parseString(csv))
        .isInstanceOf(ParserException.class)
        .hasMessageContaining(ERROR_DQUOTE_IN_NONESCAPED_FIELD)
        .satisfies(
            e -> {
              var exception = (ParserException) e;
              assertThat(exception.getLineNumber()).isEqualTo(1);
              assertThat(exception.getCharacterNumber()).isEqualTo(8);
            });
  }

  static Stream<Arguments> doubleQuoteInNonEscapedFieldRaisesParseError() {
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(
        csvToolsProvider(), booleans, booleans, booleans, booleans);
  }

  @ParameterizedTest
  @MethodSource
  void escapedFieldWithNoClosingDoubleQuoteRaisesParseError(
      CsvTool csvTool, boolean a, boolean b, boolean c, boolean d) {
    var target = new CsvParserRfc4180(csvTool.delimiter(), a, b, c, d);
    var recordCount = 1;
    var csv =
        csvTool.toCsvFromStrings(
            CsvTool.listFilledWith(List.of("abc", "\"an escaped field"), recordCount));

    assertThatThrownBy(() -> target.parseString(csv))
        .isInstanceOf(ParserException.class)
        .hasMessageContaining(ERROR_EOF_REACHED_IN_ESCAPED_FIELD)
        .satisfies(
            e -> {
              var exception = (ParserException) e;
              assertThat(exception.getLineNumber()).isEqualTo(1);
              assertThat(exception.getCharacterNumber())
                  .isBetween(21, 23); // varies depending on newline
            });
  }

  static Stream<Arguments> escapedFieldWithNoClosingDoubleQuoteRaisesParseError() {
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(
        csvToolsProvider(), booleans, booleans, booleans, booleans);
  }

  @ParameterizedTest
  @MethodSource
  void unicodeLettersAreParsedAsIs(CsvTool csvTool, String field) throws IOException {
    var target = new CsvParserRfc4180(csvTool.delimiter(), false, false, false, false);
    var csvData = CsvTool.listOfListFilledWith(field, 1, 3);
    var csv = csvTool.toCsvFromStrings(csvData);

    var actualRecords = target.parseString(csv);
    assertThat(actualRecords).containsExactlyElementsOf(csvData);
  }

  static Stream<Arguments> unicodeLettersAreParsedAsIs() {
    // BMP (i < 4) and SIP (non-BMP) (i >= 4)
    var fields =
        List.of(
            "!",
            "A",
            "\u3042",
            " ! 0 a A \u3042 ",
            "\ud800\udc0f",
            "\ud800\udc0f\u3042\ud800\udc0f\ud800\udc0f\u3042");
    return ArgumentsHelper.cartesianProduct(csvToolsProvider(), fields);
  }

  private static List<CsvTool> csvToolsProvider() {
    return List.of(
        new CsvTool(',', Newline.CRLF, true),
        new CsvTool(',', Newline.CRLF, false),
        new CsvTool(',', Newline.LF, true),
        new CsvTool(',', Newline.CR, true), // Comma
        new CsvTool('\t', Newline.CRLF, true),
        new CsvTool('\t', Newline.CRLF, false), // Tab
        new CsvTool(0x20bb7, Newline.LF, true)) // SIP (non-BMP)
    ;
  }

  private static List<UnaryOperator<String>> spaceAdderProvider() {
    return List.of(
        s -> " " + s,
        s -> s + " ",
        s -> " " + s + " ",
        s -> "\u2003" + s,
        s -> s + "\u2003",
        s -> "\u2003" + s + "\u2003",
        s -> " \u2003 " + s,
        s -> s + " \u2003 ",
        s -> " \u2003 " + s + " \u2003 ");
  }

  // Copied from CsvParserRfc4180.java
  private static final String ERROR_ILLEGAL_NUMBER_OF_FIELDS = "Illegal number of fields found";
  private static final String ERROR_EMPTY_FIELD_FOLLOWED_BY_EOR =
      "Empty field followed by end of record";
  private static final String ERROR_DQUOTE_IN_NONESCAPED_FIELD =
      "Double quotation found in non-escaped field";
  private static final String ERROR_EOF_REACHED_IN_ESCAPED_FIELD =
      "EOF reached while parsing escaped field";
  private static final String ERROR_ILLEGAL_CHAR_AFTER_ESCAPED_FIELD =
      "Escaped field followed by illegal character";
}
