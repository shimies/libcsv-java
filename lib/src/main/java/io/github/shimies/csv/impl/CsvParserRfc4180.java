package io.github.shimies.csv.impl;

import io.github.shimies.csv.CsvParser;
import io.github.shimies.csv.ParserException;
import io.github.shimies.csv.RecordReader;
import io.github.shimies.csv.TextLocator;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RFC 4180 implementation of {@link CsvParser}.
 *
 * <p>Besides a comma defined as the field delimiter in RFC 4180, arbitrary Unicode code point can
 * be set so that this implementation can recognize it instead when reading. It also recognizes CR
 * and LF themselves as the record delimiter, in addition to CRLF defined in the specification.
 *
 * <p>A few flags are available to extend the behavior. If all is set to {@code false}, the behavior
 * complies with original and thus strict RFC 4180.
 *
 * <p>a) {@code stripFields} controls whether whitespaces that surround each field get eliminated or
 * not. This may be useful when the CSV is comma-aligned with spaces and/or tabs.
 *
 * <p>b) {@code allowRecordEndWithEmptyField} allows an empty field at the end of the record, for
 * example, an empty record or a record that ends with a comma, that RFC 4180 originally forbids.
 *
 * <p>c) {@code allowVariadicFields} allows records to have different number of fields from one
 * another.
 *
 * <p>d) {@code allowSpaceEncloseEscaped} allows escaped fields to be surrounded by spaces.
 * Enclosing spaces are not included in a field. Originally, escaped fields must follow the field
 * delimiter without anything else in between.
 */
public class CsvParserRfc4180 implements CsvParser {

  private final int delimiter;
  private final boolean stripFields;
  private final boolean allowRecordEndWithEmptyField;
  private final boolean allowVariadicFields;
  private final boolean allowSpaceEncloseEscaped;

  /**
   * Constructs.
   *
   * @param delimiter the field delimiter character
   * @param stripFields whether to strip whitespace from fields
   * @param allowRecordEndWithEmptyField whether to allow records ending with an empty field
   * @param allowVariadicFields whether to allow records with varying field counts
   * @param allowSpaceEncloseEscaped whether to allow spaces around escaped fields
   */
  public CsvParserRfc4180(
      int delimiter,
      boolean stripFields,
      boolean allowRecordEndWithEmptyField,
      boolean allowVariadicFields,
      boolean allowSpaceEncloseEscaped) {
    this.delimiter = delimiter;
    this.stripFields = stripFields;
    this.allowRecordEndWithEmptyField = allowRecordEndWithEmptyField;
    this.allowVariadicFields = allowVariadicFields;
    this.allowSpaceEncloseEscaped = allowSpaceEncloseEscaped;
  }

  /**
   * Creates a {@link RecordReader} for reading CSV records from a {@link Reader}.
   *
   * @param reader the {@link Reader} to read records from
   * @return an instance of {@link RecordReader}
   * @throws IOException if an I/O error occurs
   */
  @Override
  public RecordReader newRecordReader(Reader reader) throws IOException {
    return new RecordReaderImpl(reader);
  }

  /** RFC 4180 implementation of {@code RecordReader}. This class is not thread-safe. */
  private class RecordReaderImpl implements RecordReader {

    private static final String ERROR_ILLEGAL_NUMBER_OF_FIELDS = "Illegal number of fields found";
    private static final String ERROR_EMPTY_FIELD_FOLLOWED_BY_EOR =
        "Empty field followed by end of record";
    private static final String ERROR_DQUOTE_IN_NONESCAPED_FIELD =
        "Double quotation found in non-escaped field";
    private static final String ERROR_EOF_REACHED_IN_ESCAPED_FIELD =
        "EOF reached while parsing escaped field";
    private static final String ERROR_ILLEGAL_CHAR_AFTER_ESCAPED_FIELD =
        "Escaped field followed by illegal character";

    private final Tokenizer tokenizer;
    private Token<TokenKind> token;
    private int fieldCount = -1;

    public RecordReaderImpl(Reader reader) throws IOException {
      this.tokenizer = new Tokenizer(reader, delimiter);
      this.token = tokenizer.nextToken();
    }

    @Override
    public boolean hasMoreRecord() {
      return token.getKind() != TokenKind.EOF;
    }

    @Override
    public List<String> readRecord() throws IOException {
      List<String> fields = parseRecord();
      if (!allowVariadicFields) {
        if (fieldCount == -1) {
          fieldCount = fields.size();
        } else if (fieldCount != fields.size()) {
          // -1 as token is always one step ahead, meaning that it is already
          // on the next line here at the end of record
          int errorLineNo = tokenizer.getLineNumber() - 1;
          throw new ParserException(ERROR_ILLEGAL_NUMBER_OF_FIELDS, errorLineNo);
        }
      }
      return Collections.unmodifiableList(fields);
    }

    private List<String> parseRecord() throws IOException {
      List<String> fields = new ArrayList<>();
      fields.add(parseField());
      while (true) {
        switch (token.getKind()) {
          case NEWLINE:
            token = tokenizer.nextToken();
          // caution: fall though
          case EOF:
            break;
          case DELIM:
            token = tokenizer.nextToken();
          // caution: fall though
          default:
            fields.add(parseField());
            continue;
        }
        break;
      }
      return fields;
    }

    private String parseField() throws IOException {
      boolean isEscapedField = false;
      StringBuilder sb = new StringBuilder();
      while (true) {
        switch (token.getKind()) {
          case QUOTE:
            sb.delete(0, sb.length());
            token = tokenizer.nextToken();
            parseEscapedField(sb);
            isEscapedField = true;
            break;
          case BLANK:
            if (allowSpaceEncloseEscaped) {
              sb.append((String) token.getValue());
              token = tokenizer.nextToken();
              continue;
            }
          // caution: fall though otherwise
          default:
            parseNonEscapedField(sb);
            break;
        }
        break;
      }
      String field = sb.toString();
      if (stripFields && !isEscapedField) {
        field = field.strip();
      }
      return field;
    }

    private void parseNonEscapedField(StringBuilder sb) throws IOException {
      while (true) {
        switch (token.getKind()) {
          case NEWLINE:
          case EOF:
            if (!allowRecordEndWithEmptyField && sb.isEmpty()) {
              throw new ParserException(ERROR_EMPTY_FIELD_FOLLOWED_BY_EOR, tokenizer);
            }
          // caution: fall though otherwise
          case DELIM:
            return;
          case WORD:
          case BLANK:
            sb.append((String) token.getValue());
            token = tokenizer.nextToken();
            break;
          case QUOTE:
            throw new ParserException(ERROR_DQUOTE_IN_NONESCAPED_FIELD, tokenizer);
        }
      }
    }

    private void parseEscapedField(StringBuilder sb) throws IOException {
      while (true) {
        switch (token.getKind()) {
          case EOF:
            throw new ParserException(ERROR_EOF_REACHED_IN_ESCAPED_FIELD, tokenizer);
          case QUOTE:
            token = tokenizer.nextToken();
            if (token.getKind() != TokenKind.QUOTE) {
              // other end of quote pair is found
              parseEscapedFieldEnd();
              return;
            }
          // caution: fall though otherwise
          default:
            sb.append((String) token.getValue());
            token = tokenizer.nextToken();
            break;
        }
      }
    }

    private void parseEscapedFieldEnd() throws IOException {
      switch (token.getKind()) {
        case NEWLINE:
        case DELIM:
        case EOF:
          return;
        case BLANK:
          if (allowSpaceEncloseEscaped) {
            // allow spaces but discard
            token = tokenizer.nextToken();
            parseEscapedFieldEnd();
            return;
          }
        // caution: fall though otherwise
        default:
          throw new ParserException(ERROR_ILLEGAL_CHAR_AFTER_ESCAPED_FIELD, tokenizer);
      }
    }
  }

  private static class Tokenizer implements Token.Tokenizer<TokenKind>, TextLocator {

    private static final int CP_LINE_FEED = 0x0a;
    private static final int CP_CARRIAGE_RETURN = 0x0d;
    private static final int CP_DOUBLE_QUOTE = 0x22;

    private final CodePointReader reader;
    private final int delimiter;
    private int lastCodePoint;
    private boolean isNewlineJustRead = false;
    private int lineNo = 0;
    private int characterNo = 0;

    public Tokenizer(Reader reader, int delimiter) throws IOException {
      this.reader = new CodePointReader(reader);
      this.delimiter = delimiter;
      lastCodePoint =
          this.reader.readCodePoint(); // do not update location for accurate location reporting
    }

    @Override
    public boolean hasNext() {
      return lastCodePoint != -1;
    }

    @Override
    public Token<TokenKind> nextToken() throws IOException {
      TokenKind type = null;
      Object value = null;
      CharacterClass ctype;
      switch ((ctype = mapCodePoint(lastCodePoint))) {
        case LETTER:
          type = TokenKind.WORD;
          value = readWhile(ctype);
          break;
        case SPACE:
          type = TokenKind.BLANK;
          value = readWhile(ctype);
          break;
        case CR:
        case LF:
          type = TokenKind.NEWLINE;
          value = readNewline(ctype);
          break;
        case DELIM:
          type = TokenKind.DELIM;
          value = readCharacter();
          break;
        case QUOTE:
          type = TokenKind.QUOTE;
          value = readCharacter();
          break;
        case EOF:
          type = TokenKind.EOF;
          break;
      }
      return new Token<>(type, value);
    }

    @Override
    public int getLineNumber() {
      return lineNo + 1;
    }

    @Override
    public int getCharacterNumber() {
      return characterNo;
    }

    private CharacterClass mapCodePoint(int codePoint) {
      CharacterClass type = CharacterClass.LETTER;
      if (codePoint < 0) {
        type = CharacterClass.EOF;
      } else if (codePoint == delimiter) {
        type = CharacterClass.DELIM;
      } else if (codePoint == CP_LINE_FEED) {
        type = CharacterClass.LF;
      } else if (codePoint == CP_CARRIAGE_RETURN) {
        type = CharacterClass.CR;
      } else if (codePoint == CP_DOUBLE_QUOTE) {
        type = CharacterClass.QUOTE;
      } else if (Character.isWhitespace(codePoint)) {
        type = CharacterClass.SPACE;
      }
      return type;
    }

    private String readWhile(CharacterClass ccls) throws IOException {
      StringBuilder sb = new StringBuilder();
      do {
        sb.append(Character.toChars(lastCodePoint));
        readCodePoint();
      } while (mapCodePoint(lastCodePoint) == ccls);
      return sb.toString();
    }

    private String readNewline(CharacterClass hint) throws IOException {
      StringBuilder sb = new StringBuilder();
      sb.append(Character.toChars(lastCodePoint));
      readCodePoint();
      if (hint == CharacterClass.CR) {
        if (mapCodePoint(lastCodePoint) == CharacterClass.LF) {
          sb.append(Character.toChars(lastCodePoint));
          readCodePoint();
        }
      }
      isNewlineJustRead = true;
      return sb.toString();
    }

    private String readCharacter() throws IOException {
      String c = new String(Character.toChars(lastCodePoint));
      readCodePoint();
      return c;
    }

    private void readCodePoint() throws IOException {
      lastCodePoint = reader.readCodePoint();
      updateTextLocation();
    }

    private void updateTextLocation() {
      if (!isNewlineJustRead) {
        characterNo++;
      } else {
        isNewlineJustRead = false;
        lineNo++;
        characterNo = 0;
      }
    }
  }

  private enum TokenKind {
    WORD,
    BLANK,
    NEWLINE,
    DELIM,
    QUOTE,
    EOF
  }

  private enum CharacterClass {
    DELIM,
    QUOTE,
    CR,
    LF,
    LETTER,
    SPACE,
    EOF
  }
}
