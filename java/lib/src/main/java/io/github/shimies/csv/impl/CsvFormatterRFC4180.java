package io.github.shimies.csv.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;

import io.github.shimies.csv.ICsvFormatter;
import io.github.shimies.csv.IRecordWriter;
import io.github.shimies.csv.Token;

/**
 * RFC 4180 implementation of {@code ICsvFormatter}.
 *
 * Besides a comma and CRLF defined in RFC 4180 as the field delimiter and the
 * record delimiter, arbitrary Unicode code point and {@code String} can be
 * respectively set so that this implementation can use them instead when
 * writing.
 *
 * RFC 4180 also forbids an empty field at the end of the record. For example,
 * an empty record and a record that ends with a comma are not allowed. This
 * implementation writes an escaped empty field, {@code ""}, for such cases.
 * This behavior can be configured to instead allow such an empty field to be
 * written without escaping it.
 */
public class CsvFormatterRFC4180 implements ICsvFormatter {

    private static final String DOUBLE_QUOTE = "\"";
    private static final String ESCAPED_EMPTY_FIELD = DOUBLE_QUOTE + DOUBLE_QUOTE;

    private final int delimiter;
    private final String fieldDelimiter;
    private final String recordDelimiter;
    private final boolean allowRecordEndWithEmptyField;

    public CsvFormatterRFC4180(int fieldDelimiter, String recordDelimiter, boolean allowRecordEndWithEmptyField) {
        this.delimiter = fieldDelimiter;
        this.fieldDelimiter = new String(Character.toChars(fieldDelimiter));
        this.recordDelimiter = recordDelimiter;
        this.allowRecordEndWithEmptyField = allowRecordEndWithEmptyField;
    }

    @Override
    public IRecordWriter newRecordWriter(Writer writer) throws IOException {
        return new RecordWriter(writer);
    }

    /**
     * RFC 4180 implementation of {@code IRecordWriter}. This class is not thread
     * safe.
     */
    private class RecordWriter implements IRecordWriter {

        private final Writer writer;

        public RecordWriter(Writer writer) throws IOException {
            this.writer = writer;
        }

        @Override
        public void writeRecord(List<String> record) throws IOException {
            int size = record.size();
            if (size == 0)
                return;
            int lastIndex = size - 1;
            List<String> allButLast = record.subList(0, lastIndex);
            for (String field : allButLast) {
                writer.write(escapeField(field));
                writer.write(fieldDelimiter);
            }
            writer.write(escapeLastField(record.get(lastIndex)));
            writer.write(recordDelimiter);
        }

        private String escapeField(String field) throws IOException {
            Tokenizer tokenizer = new Tokenizer(field);
            StringBuilder sb = new StringBuilder();
            boolean mustEscape = false;
            while (tokenizer.hasNext()) {
                Token<TokenKind> token = tokenizer.nextToken();
                String tokenValue = (String) token.getValue();
                switch (token.getKind()) {
                case MUST_ESCAPE_DQUOTE:
                    sb.append(tokenValue);
                    // caution: fall through
                case MUST_ESCAPE_OTHERS:
                    mustEscape = true;
                    break;
                default:
                    break;
                }
                sb.append(tokenValue);
            }
            if (mustEscape) {
                sb.append(DOUBLE_QUOTE);
                sb.insert(0, DOUBLE_QUOTE);
            }
            return sb.toString();
        }

        private String escapeLastField(String field) throws IOException {
            String escapedField = escapeField(field);
            // it must be escaped if it's last field and empty
            if (!allowRecordEndWithEmptyField && escapedField.isEmpty())
                escapedField = ESCAPED_EMPTY_FIELD;
            return escapedField;
        }

    }

    private class Tokenizer implements Token.ITokenizer<TokenKind> {

        private static final int CP_LINE_FEED = 0x0a;
        private static final int CP_CARRIAGE_RETURN = 0x0d;
        private static final int CP_DOUBLE_QUOTE = 0x22;

        private final Reader reader;
        private int lastCodePoint;

        public Tokenizer(String field) throws IOException {
            reader = new StringReader(field);
            lastCodePoint = readCodePoint();
        }

        @Override
        public Reader getReader() {
            return reader;
        }

        @Override
        public void onReadCodePoint() {
        }

        @Override
        public boolean hasNext() {
            return lastCodePoint != -1;
        }

        @Override
        public Token<TokenKind> nextToken() throws IOException {
            TokenKind type = mapCodePoint(lastCodePoint);
            Object value;
            switch (type) {
            case MUST_ESCAPE_DQUOTE:
                value = DOUBLE_QUOTE;
                lastCodePoint = readCodePoint();
                break;
            default:
                value = readWhile(type);
                break;
            }
            return new Token<TokenKind>(type, value);
        }

        private TokenKind mapCodePoint(int codePoint) {
            TokenKind type = TokenKind.NO_ESCAPE_NEEDED;
            if (!hasNext())
                type = null;
            else if (codePoint == CP_DOUBLE_QUOTE)
                type = TokenKind.MUST_ESCAPE_DQUOTE;
            else if (codePoint == delimiter || codePoint == CP_LINE_FEED || codePoint == CP_CARRIAGE_RETURN)
                type = TokenKind.MUST_ESCAPE_OTHERS;
            return type;
        }

        private String readWhile(TokenKind kind) throws IOException {
            StringBuilder sb = new StringBuilder();
            do {
                sb.append(Character.toChars(lastCodePoint));
                lastCodePoint = readCodePoint();
            } while (mapCodePoint(lastCodePoint) == kind);
            return sb.toString();
        }

    }

    private enum TokenKind {
        NO_ESCAPE_NEEDED, MUST_ESCAPE_DQUOTE, MUST_ESCAPE_OTHERS;
    }

}
