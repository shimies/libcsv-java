package io.github.shimies.csv;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Test suite for {@link CsvParsers}. */
public class CsvParserTest {

  @Test
  void allRecordsInFileCanBeParsedAtOnce() throws IOException {
    var parser = CsvParsers.ofStrictRfc4180(false);
    var path = ResourceHelper.makePathOutOfResource("fixed_n_fields_0.csv");

    var records = parser.parseFile(path, StandardCharsets.UTF_8);

    assertThat(records).containsExactly(List.of("1    ", " 2"), List.of("Hello", " World"));
  }

  @Test
  void allRecordsInStringCanBeParsedAtOnce() throws IOException {
    var parser = CsvParsers.ofStrictRfc4180(true);

    var records =
        parser.parseString(
            """
                a,b
                c,d
                """);

    assertThat(records).containsExactly(List.of("a", "b"), List.of("c", "d"));
  }

  @Test
  void eachRecordsIsParsedSequentiallyUsingRecordReader() throws IOException {
    var parser = CsvParsers.ofStrictRfc4180(true);
    var path = ResourceHelper.makePathOutOfResource("fixed_n_fields_0.csv");

    var records = new ArrayList<List<String>>();
    try (var br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      var reader = parser.newRecordReader(br);
      while (reader.hasMoreRecord()) {
        records.add(reader.readRecord());
      }
    }

    assertThat(records).containsExactly(List.of("1", "2"), List.of("Hello", "World"));
  }
}
