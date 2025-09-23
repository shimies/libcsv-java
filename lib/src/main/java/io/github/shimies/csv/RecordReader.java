package io.github.shimies.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A CSV record reader that reads a record from a specific source sequentially. Classes implementing
 * this interface must be mutable, thus may not be thread-safe.
 */
public interface RecordReader {

  /**
   * Returns true if there are more records to read.
   *
   * @return true if more records are available
   */
  boolean hasMoreRecord();

  /**
   * Reads the next CSV record from the source.
   *
   * @return the next record as a {@link List} of strings
   * @throws IOException if an I/O error occurs
   */
  List<String> readRecord() throws IOException;

  /**
   * Reads all remaining records from the source.
   *
   * @return a {@link List} of all records
   * @throws IOException if an I/O error occurs
   */
  default List<List<String>> readAllRecords() throws IOException {
    List<List<String>> records = new ArrayList<>();
    while (hasMoreRecord()) {
      records.add(readRecord());
    }
    return Collections.unmodifiableList(records);
  }
}
