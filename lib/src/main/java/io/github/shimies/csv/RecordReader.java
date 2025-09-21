package io.github.shimies.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A CSV record reader that reads a record from a specific source sequentially. Classes implementing
 * this interface must be mutable, and thus may not be thread safe.
 */
public interface RecordReader {

  boolean hasMoreRecord();

  List<String> readRecord() throws IOException;

  default List<List<String>> readAllRecords() throws IOException {
    List<List<String>> records = new ArrayList<>();
    while (hasMoreRecord()) records.add(readRecord());
    return records;
  }
}
