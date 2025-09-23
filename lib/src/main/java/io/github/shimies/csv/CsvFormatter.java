package io.github.shimies.csv;

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
 * List<List<String>>}. {@link CsvFormatter} is immutable, and can be used to format multiple
 * table-like data.
 */
@FunctionalInterface
public interface CsvFormatter {

  /**
   * Creates a new {@link RecordWriter} for writing CSV records into the specified writer.
   *
   * @param writer the {@link Writer} to write records to
   * @return an instance of {@link RecordWriter}
   * @throws IOException if an {@link IOException} occurs
   */
  RecordWriter newRecordWriter(Writer writer) throws IOException;

  /**
   * Formats and writes all records to a file at the specified path and charset.
   *
   * @param records the list of records
   * @param csvPath the {@link Path} to the output CSV file
   * @param cs the {@link Charset} to use
   * @throws IOException if an I/O error occurs
   */
  default void formatToFile(List<List<String>> records, Path csvPath, Charset cs)
      throws IOException {
    try (Writer writer = Files.newBufferedWriter(csvPath, cs)) {
      newRecordWriter(writer).writeAllRecords(records);
    }
  }

  /**
   * Formats all records to a CSV string.
   *
   * @param records the list of records
   * @return the formatted CSV string
   * @throws IOException if an I/O error occurs
   */
  default String formatToString(List<List<String>> records) throws IOException {
    try (Writer writer = new StringWriter()) {
      newRecordWriter(writer).writeAllRecords(records);
      return writer.toString();
    }
  }
}
