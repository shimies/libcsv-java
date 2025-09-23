package io.github.shimies.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.stream.Stream;

/**
 * A CSV record writer that writes a record into a specific destination sequentially. Classes
 * implementing this interface must be mutable, thus may not be thread-safe.
 */
public interface RecordWriter {

  /**
   * Writes a single CSV record to the destination.
   *
   * @param record a list of fields to write
   * @throws IOException if an I/O error occurs
   */
  void writeRecord(List<String> record) throws IOException;

  /**
   * Writes all records from {@link Reader} and {@link CsvParser} to the destination.
   *
   * @param reader the {@link Reader} providing CSV data
   * @param parser the {@link CsvParser} that parses and recognizes the fields of the CSV data
   * @throws IOException if an I/O error occurs
   */
  default void writeAllRecords(Reader reader, CsvParser parser) throws IOException {
    RecordReader r = parser.newRecordReader(reader);
    while (r.hasMoreRecord()) {
      writeRecord(r.readRecord());
    }
  }

  /**
   * Writes all records from an iterable collection of records to the destination.
   *
   * @param records the iterable collection of records
   * @throws IOException if an I/O error occurs
   */
  default void writeAllRecords(Iterable<List<String>> records) throws IOException {
    for (List<String> record : records) {
      writeRecord(record);
    }
  }

  /**
   * Writes all records from a stream of records to the destination.
   *
   * @param records the stream of records
   * @throws IOException if an I/O error occurs
   */
  default void writeAllRecords(Stream<List<String>> records) throws IOException {
    writeAllRecords(records::iterator);
  }
}
