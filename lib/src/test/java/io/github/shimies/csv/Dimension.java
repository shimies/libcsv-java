package io.github.shimies.csv;

/**
 * Represents the dimensions of a CSV (records and fields).
 *
 * @param recordCount number of records
 * @param fieldCount number of fields per record
 */
public record Dimension(int recordCount, int fieldCount) {}
