package io.github.shimies.csv;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

/** Provides helper methods for creating a stream of arguments for junit parameterized tests. */
public class ArgumentsHelper {

  /**
   * Generates a stream of {@link Arguments} representing the cartesian product of the provided
   * lists.
   *
   * @param values lists of values to combine
   * @return {@link Stream} of {@link Arguments} representing all instances of cartesian product
   */
  public static Stream<Arguments> cartesianProduct(List<?>... values) {
    var len = values.length;
    var buf = new Object[len];
    return cartesianProduct(0, buf, values);
  }

  private static Stream<Arguments> cartesianProduct(int n, Object[] args, List<?>[] valueSets) {
    if (n == args.length) {
      return Stream.of(Arguments.of(Arrays.copyOf(args, args.length)));
    }
    return valueSets[n].stream()
        .flatMap(
            e -> {
              args[n] = e;
              return cartesianProduct(n + 1, args, valueSets);
            });
  }
}
