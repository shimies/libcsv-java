package io.github.shimies.csv;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;

/** Utility class for loading resources for tests. */
public class ResourceHelper {

  /**
   * Converts a resource name to a {@link Path} for use in tests.
   *
   * @param resource the resource name
   * @return {@link Path} to the resource file
   * @throws RuntimeException if the resource cannot be found or URI syntax is invalid
   */
  public static Path makePathOutOfResource(String resource) {
    try {
      var url = CsvParserTest.class.getClassLoader().getResource(resource);
      Objects.requireNonNull(url);
      return Path.of(url.toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
