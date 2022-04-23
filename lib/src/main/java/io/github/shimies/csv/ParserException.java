package io.github.shimies.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParserException extends IOException implements ITextLocator {

    private final int lineNo;
    private final int characterNo;

    public ParserException(String message, int lineNo, int characterNo) {
        super(message);
        this.lineNo = lineNo;
        this.characterNo = characterNo;
    }

    public ParserException(String message, int lineNo) {
        this(message, lineNo, -1);
    }

    public ParserException(String message, ITextLocator locator) {
        this(message, locator.getLineNumber(), locator.getCharacterNumber());
    }

    @Override
    public int getLineNumber() {
        return lineNo;
    }

    @Override
    public int getCharacterNumber() {
        return characterNo;
    }

    @Override
    public String getMessage() {
        return appendTextLocation(super.getMessage());
    }

    private String appendTextLocation(String base) {
        List<String> xs = new ArrayList<>();
        if (lineNo >= 0)
            xs.add(new StringBuilder().append('L').append(lineNo).toString());
        if (characterNo >= 0)
            xs.add(new StringBuilder().append('C').append(characterNo).toString());
        if (xs.size() == 0)
            return base;
        StringBuilder sb = new StringBuilder(base).append(' ');
        sb.append('[');
        sb.append(String.join(",", xs));
        sb.append(']');
        return sb.toString();
    }

}
