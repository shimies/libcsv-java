package io.github.shimies.csv;

import java.io.IOException;
import java.io.Reader;

public class Token<T extends Enum<T>> {

  private final T type;
  private final Object value;

  public Token(T type) {
    this(type, null);
  }

  public Token(T type, Object value) {
    this.type = type;
    this.value = value;
  }

  public T getKind() {
    return type;
  }

  public Object getValue() {
    return value;
  }

  public static interface ITokenizer<T extends Enum<T>> {

    Reader getReader();

    void onReadCodePoint();

    boolean hasNext();

    Token<T> nextToken() throws IOException;

    default int readCodePoint() throws IOException {
      Reader reader = getReader();
      int cp = reader.read(); // read() does not return a code point
      char potentialHigh = (char) cp;
      if (Character.isHighSurrogate(potentialHigh)) {
        char low = (char) reader.read();
        if (!Character.isLowSurrogate(low))
          throw new IOException("Encounter illegal unicode character");
        cp = Character.toCodePoint(potentialHigh, low);
      }
      onReadCodePoint();
      return cp;
    }
  }
}
