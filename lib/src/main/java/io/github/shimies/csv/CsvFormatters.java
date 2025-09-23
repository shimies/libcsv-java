package io.github.shimies.csv;

import io.github.shimies.csv.impl.CsvFormatterRfc4180;

/** Factory for creating {@link CsvFormatter} instances. */
public class CsvFormatters {

  /**
   * Returns a CSV formatter for RFC 4180 with default settings.
   *
   * @return an instance of {@link CsvFormatter}
   */
  public static CsvFormatter ofRfc4180() {
    return CsvFormatters.ofRfc4180(',', Newline.CRLF, false);
  }

  /**
   * Returns a CSV formatter for RFC 4180 with custom settings.
   *
   * @param delimiter the field delimiter character
   * @param newline the newline type
   * @param allowRecordEndWithEmptyField whether to allow records ending with an empty field
   * @return an instance of {@link CsvFormatter}
   */
  public static CsvFormatter ofRfc4180(
      int delimiter, Newline newline, boolean allowRecordEndWithEmptyField) {
    return new CsvFormatterRfc4180(delimiter, newline.toString(), allowRecordEndWithEmptyField);
  }

  private CsvFormatters() {}
}
