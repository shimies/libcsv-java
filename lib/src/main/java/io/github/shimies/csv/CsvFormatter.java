package io.github.shimies.csv;

import io.github.shimies.csv.impl.CsvFormatterRfc4180;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * A CSV formatter that formats table-like data represented by Java objects to textually-represented
 * CSVs.
 *
 * <p>A record is expressed by {@code List<String>}, and an entire CSV is expressed by {@code
 * List<List<String>>}. {@code CsvFormatter} is immutable, and can be used to format multiple
 * table-like data.
 */
@FunctionalInterface
public interface CsvFormatter {

  static CsvFormatter ofRfc4180() {
    return CsvFormatter.ofRfc4180(',', Newline.CRLF, false);
  }

  static CsvFormatter ofRfc4180(
      int delimiter, Newline newline, boolean allowRecordEndWithEmptyField) {
    return new CsvFormatterRfc4180(delimiter, newline.toString(), allowRecordEndWithEmptyField);
  }

  RecordWriter newRecordWriter(Writer writer) throws IOException;

  default void formatToFile(List<List<String>> records, Path csvPath, Charset cs)
      throws IOException {
    try (Writer writer = Files.newBufferedWriter(csvPath, cs)) {
      newRecordWriter(writer).writeAllRecords(records);
    }
  }

  default String formatToString(List<List<String>> records) throws IOException {
    try (Writer writer = new StringWriter()) {
      newRecordWriter(writer).writeAllRecords(records);
      return writer.toString();
    }
  }
}
