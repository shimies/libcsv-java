package io.github.shimies.csv;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Test suite for {@link CsvParsers}. */
public class CsvParsersTest {

  @ParameterizedTest
  @MethodSource
  void rfc4180CompliantCsvParserCanBeCreated(String csvPath, int errorLine) {
    var parser = CsvParsers.ofStrictRfc4180(true);
    var path = ResourceHelper.makePathOutOfResource(csvPath);

    assertThatThrownBy(() -> parser.parseFile(path, StandardCharsets.UTF_8))
        .isInstanceOf(ParserException.class)
        .satisfies(
            e -> {
              var exception = (ParserException) e;
              assertThat(exception.getLineNumber()).isEqualTo(errorLine);
            });
  }

  static Stream<Arguments> rfc4180CompliantCsvParserCanBeCreated() {
    return Stream.of(
        Arguments.of("variadic_n_fields_0.csv", 2),
        Arguments.of("variadic_n_fields_0.tsv", 1),
        Arguments.of("fixed_n_fields_1.csv", 1));
  }

  @Test
  void rfc4180CompliantCsvParserCanBeCreatedWithFieldsConstraintsRelaxed() throws IOException {
    var parser = CsvParsers.ofStrictRfc4180ForVariadicFields(true);
    var path = ResourceHelper.makePathOutOfResource("variadic_n_fields_0.csv");

    var records = parser.parseFile(path, StandardCharsets.UTF_8);

    assertThat(records)
        .containsExactly(
            List.of("circle", "r=10,0"),
            List.of("rectangle", "x=5,0", "y=10,0"),
            List.of("square", "n=10,0"));
  }

  @Test
  void rfc4180CompliantCsvParserCanBeCreatedWithBehaviorCustomized() throws IOException {
    var parser = CsvParsers.ofVariantRfc4180('|', true, true, true, true);
    var path = ResourceHelper.makePathOutOfResource("variadic_n_fields_1.tsv");

    var records = parser.parseFile(path, StandardCharsets.UTF_8);

    assertThat(records)
        .containsExactly(
            List.of("circle", "r=10,0", ""),
            List.of("rectangle", "x=5,0", "y=10,0", ""),
            List.of("square", "n=10,0", ""));
  }
}
