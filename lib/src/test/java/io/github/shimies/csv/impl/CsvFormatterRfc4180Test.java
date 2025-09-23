package io.github.shimies.csv.impl;

import static org.assertj.core.api.Assertions.*;

import io.github.shimies.csv.ArgumentsHelper;
import io.github.shimies.csv.CsvTool;
import io.github.shimies.csv.Dimension;
import io.github.shimies.csv.Newline;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Test suite for {@link CsvFormatterRfc4180} covering formatting of CSV records and edge cases. */
public class CsvFormatterRfc4180Test {

  @ParameterizedTest
  @MethodSource
  void emptyRecordIsFormattedAsEmptyString(CsvTool csvTool, boolean a) throws IOException {
    var target = new CsvFormatterRfc4180(csvTool.delimiter(), csvTool.newline().toString(), a);

    var actualCsv = target.formatToString(List.of());
    assertThat(actualCsv).isEmpty();
  }

  static Stream<Arguments> emptyRecordIsFormattedAsEmptyString() {
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(csvToolsProvider(), booleans);
  }

  @ParameterizedTest
  @MethodSource
  void formattingRecordsFilledWithSameElementSucceeds(CsvTool csvTool, Dimension shape, boolean a)
      throws IOException {
    var target = new CsvFormatterRfc4180(csvTool.delimiter(), csvTool.newline().toString(), a);
    var field = "  this is a test!  ";
    var records = CsvTool.listOfListFilledWith(field, shape.recordCount(), shape.fieldCount());

    var actualCsv = target.formatToString(records);
    assertThat(actualCsv)
        .isEqualTo(
            csvTool.toCsvFromStrings(
                CsvTool.listOfListFilledWith(field, shape.recordCount(), shape.fieldCount())));
  }

  static Stream<Arguments> formattingRecordsFilledWithSameElementSucceeds() {
    var dimensionSet =
        List.of(
            new Dimension(1, 1), new Dimension(1, 2), new Dimension(1, 10), new Dimension(1, 500));
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(csvToolsProvider(), dimensionSet, booleans);
  }

  @ParameterizedTest
  @MethodSource
  void fieldContainingDelimiterIsFormattedWithEscaping(CsvTool csvTool, List<String> xs, boolean a)
      throws IOException {
    var target = new CsvFormatterRfc4180(csvTool.delimiter(), csvTool.newline().toString(), a);
    // use delimiter-separated record as field (so the field contains delimiter)
    var field = csvTool.toCsvRecordFromStrings(xs);
    var records = CsvTool.listOfListFilledWith(field, 2, 10);

    var actualCsv = target.formatToString(records);
    assertThat(actualCsv)
        .isEqualTo(
            csvTool.toCsvFromStrings(
                CsvTool.listOfListFilledWith(CsvTool.escapeRfc4180(field), 2, 10)));
  }

  static Stream<Arguments> fieldContainingDelimiterIsFormattedWithEscaping() {
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
  void fieldContainingDoubleQuoteOrNewlineIsFormattedWithEscaping(
      CsvTool csvTool, String field, boolean a) throws IOException {
    var target = new CsvFormatterRfc4180(csvTool.delimiter(), csvTool.newline().toString(), a);
    var records = CsvTool.listOfListFilledWith(field, 2, 10);

    var actualCsv = target.formatToString(records);
    assertThat(actualCsv)
        .isEqualTo(
            csvTool.toCsvFromStrings(
                CsvTool.listOfListFilledWith(CsvTool.escapeRfc4180(field), 2, 10)));
  }

  static Stream<Arguments> fieldContainingDoubleQuoteOrNewlineIsFormattedWithEscaping() {
    var fieldSet =
        List.of(
            "\r\n", "\n\r\n\r", "\na\n", "a\na", "\"", "\"\"", "\"a\"", "a\"a", "\n\"\n", "\"\n\"");
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(csvToolsProvider(), fieldSet, booleans);
  }

  @ParameterizedTest
  @MethodSource
  void fieldNoNeedToBeEscapedIsLeftUntouchedWhileOthersAreFormattedWithEscaping(
      CsvTool csvTool, List<CsvTool.Field> fields, boolean a) throws IOException {
    var target = new CsvFormatterRfc4180(csvTool.delimiter(), csvTool.newline().toString(), a);
    var recordCount = 2;
    var actualFields = fields.stream().map(CsvTool.Field::field).toList();
    var records = CsvTool.listFilledWith(actualFields, recordCount);

    var actualCsv = target.formatToString(records);
    assertThat(actualCsv)
        .isEqualTo(csvTool.toCsvFromFields(CsvTool.listFilledWith(fields, recordCount)));
  }

  private static Stream<Arguments>
      fieldNoNeedToBeEscapedIsLeftUntouchedWhileOthersAreFormattedWithEscaping() {
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
  void emptyFieldAtEndOfRecordIsEscapedIfSuchFieldIsNotAllowed(CsvTool csvTool) throws IOException {
    var target = new CsvFormatterRfc4180(csvTool.delimiter(), csvTool.newline().toString(), false);
    var records = CsvTool.listOfListFilledWith("", 3, 2);

    var actualCsv = target.formatToString(records);
    assertThat(actualCsv)
        .isEqualTo(
            csvTool.toCsvFromFields(
                CsvTool.listFilledWith(
                    List.of(new CsvTool.Field("", false), new CsvTool.Field("", true)), 3)));
  }

  static Stream<Arguments> emptyFieldAtEndOfRecordIsEscapedIfSuchFieldIsNotAllowed() {
    return ArgumentsHelper.cartesianProduct(csvToolsProvider());
  }

  @ParameterizedTest
  @MethodSource
  void emptyFieldAtEndOfRecordIsFormattedAsIsIfSuchFieldIsAllowed(CsvTool csvTool)
      throws IOException {
    var target = new CsvFormatterRfc4180(csvTool.delimiter(), csvTool.newline().toString(), true);
    var records = CsvTool.listOfListFilledWith("", 3, 2);

    var actualCsv = target.formatToString(records);
    assertThat(actualCsv)
        .isEqualTo(
            csvTool.toCsvFromFields(
                CsvTool.listFilledWith(
                    List.of(new CsvTool.Field("", false), new CsvTool.Field("", false)), 3)));
  }

  static Stream<Arguments> emptyFieldAtEndOfRecordIsFormattedAsIsIfSuchFieldIsAllowed() {
    return ArgumentsHelper.cartesianProduct(csvToolsProvider());
  }

  @ParameterizedTest
  @MethodSource
  void unicodeLettersAreFormattedAsIs(CsvTool csvTool, String field, boolean a) throws IOException {
    var target = new CsvFormatterRfc4180(csvTool.delimiter(), csvTool.newline().toString(), a);
    var records = CsvTool.listOfListFilledWith(field, 1, 3);

    var actualCsv = target.formatToString(records);
    assertThat(actualCsv)
        .isEqualTo(csvTool.toCsvFromStrings(CsvTool.listOfListFilledWith(field, 1, 3)));
  }

  static Stream<Arguments> unicodeLettersAreFormattedAsIs() {
    // BMP (i < 4) and SIP (non-BMP) (i >= 4)
    var fields =
        List.of(
            "!",
            "A",
            "\u3042",
            " ! 0 a A \u3042 ",
            "\ud800\udc0f",
            "\ud800\udc0f\u3042\ud800\udc0f\ud800\udc0f\u3042");
    var booleans = List.of(false, true);
    return ArgumentsHelper.cartesianProduct(csvToolsProvider(), fields, booleans);
  }

  private static List<CsvTool> csvToolsProvider() {
    // Do not set addNewlineBeforeEof to false because the formatter always add newline at EOF
    return List.of(
        new CsvTool(',', Newline.CRLF, true),
        new CsvTool(',', Newline.LF, true),
        new CsvTool(',', Newline.CR, true), // Comma
        new CsvTool('\t', Newline.CRLF, true), // Tab
        new CsvTool(0x20bb7, Newline.LF, true)) // SIP (non-BMP)
    ;
  }
}
