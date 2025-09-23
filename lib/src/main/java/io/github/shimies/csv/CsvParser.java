package io.github.shimies.csv;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * A CSV parser that parses textually-represented CSVs to corresponding data represented by Java
 * objects.
 *
 * <p>A record is expressed by {@code List<String>}, and an entire CSV is expressed by {@code
 * List<List<String>>}. {@link CsvParser} is immutable, and can be used to parse multiple CSVs from
 * different sources.
 */
@FunctionalInterface
public interface CsvParser {

  /**
   * Creates a new {@link RecordReader} for reading CSV records from the specified reader.
   *
   * @param reader the reader to read records from
   * @return an instance of {@link RecordReader}
   * @throws IOException if an I/O error occurs
   */
  RecordReader newRecordReader(Reader reader) throws IOException;

  /**
   * Parses a CSV file at the specified path and charset.
   *
   * @param path the {@link Path} to the input CSV file
   * @param cs the {@link Charset} to use
   * @return a list of records parsed from the file
   * @throws IOException if an I/O error occurs
   */
  default List<List<String>> parseFile(Path path, Charset cs) throws IOException {
    try (Reader reader = Files.newBufferedReader(path, cs)) {
      return newRecordReader(reader).readAllRecords();
    }
  }

  /**
   * Parses a CSV string.
   *
   * @param csv the CSV string to parse
   * @return a list of records parsed from the string
   * @throws IOException if an I/O error occurs
   */
  default List<List<String>> parseString(String csv) throws IOException {
    try (Reader reader = new StringReader(csv)) {
      return newRecordReader(reader).readAllRecords();
    }
  }
}
