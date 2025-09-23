package io.github.shimies.csv.impl;

import java.io.IOException;
import java.io.Reader;

/** Reads Unicode code points from a {@link Reader}, handling surrogate pairs. */
public record CodePointReader(Reader reader) {
  /**
   * Reads the next Unicode code point from the {@link Reader}.
   *
   * @return the next code point, or -1 if end of stream
   * @throws IOException if an I/O error occurs or an illegal character is encountered
   */
  public int readCodePoint() throws IOException {
    int cp = reader.read(); // read() does not return a code point
    char potentialHigh = (char) cp;
    if (Character.isHighSurrogate(potentialHigh)) {
      char low = (char) reader.read();
      if (!Character.isLowSurrogate(low)) {
        throw new IOException("Encounter illegal unicode character");
      }
      cp = Character.toCodePoint(potentialHigh, low);
    }
    return cp;
  }
}
