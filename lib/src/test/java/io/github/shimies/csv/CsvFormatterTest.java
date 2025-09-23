package io.github.shimies.csv;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Test suite for {@link CsvFormatter}. */
public class CsvFormatterTest {

  @Test
  void allRecordsCanBeFormattedIntoFileAtOnce() throws IOException {
    var formatter = CsvFormatters.ofRfc4180();
    var records = List.of(List.of("1", "2"), List.of("Hello", "World"));
    var sink = Files.createTempFile("test", ".csv");

    formatter.formatToFile(records, sink, StandardCharsets.UTF_8);

    assertThat(sink).hasContent("1,2\r\nHello,World\r\n");
    Files.delete(sink);
  }

  @Test
  void allRecordsCanBeFormattedIntoStringAtOnce() throws IOException {
    var formatter = CsvFormatters.ofRfc4180();
    var records = List.of(List.of("a", "b"), List.of(",", "\""));

    var actual = formatter.formatToString(records);

    assertThat(actual).isEqualTo("a,b\r\n\",\",\"\"\"\"\r\n");
  }

  @Test
  void eachRecordsIsFormattedSequentiallyUsingRecordWriter() throws IOException {
    var formatter = CsvFormatters.ofRfc4180();
    var records = List.of(List.of("1", "2"), List.of("Hello", "World"));
    var sink = Files.createTempFile("test", ".csv");

    try (var bw = Files.newBufferedWriter(sink, StandardCharsets.UTF_8)) {
      var writer = formatter.newRecordWriter(bw);
      for (var record : records) {
        writer.writeRecord(record);
      }
    }

    assertThat(sink).hasContent("1,2\r\nHello,World\r\n");
    Files.delete(sink);
  }
}
