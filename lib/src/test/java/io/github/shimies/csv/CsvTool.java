package io.github.shimies.csv;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility for CSV formatting and parsing in tests.
 *
 * @param delimiter the delimiter character
 * @param newline the newline type
 * @param addNewlineBeforeEof whether to add a newline before EOF
 */
public record CsvTool(int delimiter, Newline newline, boolean addNewlineBeforeEof) {

  /**
   * Represents a CSV field and whether it must be escaped.
   *
   * @param field the field value
   * @param mustEscape whether escaping is required
   */
  public record Field(String field, boolean mustEscape) {}

  public String toCsvFromStrings(List<List<String>> records) {
    var stringRecords = records.stream().map(this::toCsvRecordFromStrings).toList();
    return toCsvByJoiningRecords(stringRecords);
  }

  public String toCsvFromFields(List<List<Field>> records) {
    var stringRecords = records.stream().map(this::toCsvRecordFromFields).toList();
    return toCsvByJoiningRecords(stringRecords);
  }

  public String toCsvRecordFromStrings(List<String> fields) {
    return String.join(Character.toString(delimiter), fields);
  }

  public String toCsvRecordFromFields(List<Field> fields) {
    return fields.stream()
        .map(f -> f.mustEscape ? escapeRfc4180(f.field) : f.field)
        .collect(Collectors.joining(Character.toString(delimiter)));
  }

  private String toCsvByJoiningRecords(Iterable<String> records) {
    var nl = newline.toString();
    var csvExpression = String.join(nl, records);
    if (addNewlineBeforeEof) {
      csvExpression = csvExpression + nl;
    }
    return csvExpression;
  }

  public static <T> List<T> listFilledWith(T element, int n) {
    return Stream.generate(() -> element).limit(n).collect(Collectors.toList());
  }

  public static <T> List<List<T>> listOfListFilledWith(T element, int n, int m) {
    return listFilledWith(listFilledWith(element, m), n);
  }

  public static String escapeRfc4180(String field) {
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
      if (c == target) {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private static final char CHAR_DQUOTE = '"';
}
