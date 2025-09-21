package io.github.shimies.csv;

import io.github.shimies.csv.impl.CsvParserRFC4180;
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
 * List<List<String>>}. {@code CsvParser} is immutable, and can be used to parse multiple CSVs from
 * different sources.
 */
@FunctionalInterface
public interface CsvParser {

  static CsvParser ofStrictRFC4180(boolean stripFields) {
    return new CsvParserRFC4180(',', stripFields, false, false, false);
  }

  static CsvParser ofStrictRFC4180ForVariadicFields(boolean stripFields) {
    return new CsvParserRFC4180(',', stripFields, false, true, false);
  }

  static CsvParser ofVariantRFC4180(
      int delimiter,
      boolean stripFields,
      boolean allowRecordEndWithEmptyField,
      boolean allowVariadicFields,
      boolean allowSpaceEncloseEscaped) {
    return new CsvParserRFC4180(
        delimiter,
        stripFields,
        allowRecordEndWithEmptyField,
        allowVariadicFields,
        allowSpaceEncloseEscaped);
  }

  RecordReader newRecordReader(Reader reader) throws IOException;

  default List<List<String>> parseFile(Path path, Charset cs) throws IOException {
    try (Reader reader = Files.newBufferedReader(path, cs)) {
      return newRecordReader(reader).readAllRecords();
    }
  }

  default List<List<String>> parseString(String csv) throws IOException {
    try (Reader reader = new StringReader(csv)) {
      return newRecordReader(reader).readAllRecords();
    }
  }
}
