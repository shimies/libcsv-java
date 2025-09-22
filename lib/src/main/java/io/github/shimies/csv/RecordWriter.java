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

  void writeRecord(List<String> record) throws IOException;

  default void writeAllRecords(CsvParser parser, Reader reader) throws IOException {
    RecordReader r = parser.newRecordReader(reader);
    while (r.hasMoreRecord()) {
      writeRecord(r.readRecord());
    }
  }

  default void writeAllRecords(Iterable<List<String>> records) throws IOException {
    for (List<String> record : records) {
      writeRecord(record);
    }
  }

  default void writeAllRecords(Stream<List<String>> records) throws IOException {
    writeAllRecords(records::iterator);
  }
}
