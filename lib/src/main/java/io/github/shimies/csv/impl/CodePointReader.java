package io.github.shimies.csv.impl;

import java.io.IOException;
import java.io.Reader;

public class CodePointReader {

  private final Reader reader;

  public CodePointReader(Reader reader) {
    this.reader = reader;
  }

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
