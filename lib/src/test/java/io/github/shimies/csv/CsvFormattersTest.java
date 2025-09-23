package io.github.shimies.csv;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Test suite for {@link CsvFormatters}. */
public class CsvFormattersTest {

  @Test
  void rfc4180CompliantCsvFormatterCanBeCreated() throws IOException {
    var formatter = CsvFormatters.ofRfc4180();
    var records = List.of(List.of("x", "y", ""), List.of("1", "2", ""));

    var actual = formatter.formatToString(records);

    assertThat(actual).isEqualTo("x,y,\"\"\r\n1,2,\"\"\r\n");
  }

  @Test
  void rfc4180CompliantCsvFormatterCanBeCreatedWithBehaviorCustomized() throws IOException {
    var formatter = CsvFormatters.ofRfc4180('|', Newline.LF, true);
    var records = List.of(List.of("foo", "bar", ""), List.of("baz", "qux", ""));

    var actual = formatter.formatToString(records);

    assertThat(actual).isEqualTo("foo|bar|\nbaz|qux|\n");
  }
}
