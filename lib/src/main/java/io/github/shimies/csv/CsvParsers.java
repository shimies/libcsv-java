package io.github.shimies.csv;

import io.github.shimies.csv.impl.CsvParserRfc4180;

/** Factory for creating {@link CsvParser} instances. */
public class CsvParsers {

  /**
   * Returns a strict RFC 4180 CSV parser.
   *
   * @param stripFields whether to strip whitespaces from fields
   * @return an instance of {@link CsvParser}
   */
  public static CsvParser ofStrictRfc4180(boolean stripFields) {
    return new CsvParserRfc4180(',', stripFields, false, false, false);
  }

  /**
   * Returns a strict RFC 4180 CSV parser that supports variadic fields.
   *
   * @param stripFields whether to strip whitespaces from fields
   * @return an instance of {@link CsvParser}
   */
  public static CsvParser ofStrictRfc4180ForVariadicFields(boolean stripFields) {
    return new CsvParserRfc4180(',', stripFields, false, true, false);
  }

  /**
   * Returns a variant RFC 4180 CSV parser with custom settings.
   *
   * @param delimiter the field delimiter character
   * @param stripFields whether to strip whitespaces from fields
   * @param allowRecordEndWithEmptyField whether to allow records ending with an empty field
   * @param allowVariadicFields whether to allow variadic fields
   * @param allowSpaceEncloseEscaped whether to allow space-enclosed escaped fields
   * @return an instance of {@link CsvParser}
   */
  public static CsvParser ofVariantRfc4180(
      int delimiter,
      boolean stripFields,
      boolean allowRecordEndWithEmptyField,
      boolean allowVariadicFields,
      boolean allowSpaceEncloseEscaped) {
    return new CsvParserRfc4180(
        delimiter,
        stripFields,
        allowRecordEndWithEmptyField,
        allowVariadicFields,
        allowSpaceEncloseEscaped);
  }

  private CsvParsers() {}
}
