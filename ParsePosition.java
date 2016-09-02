package org.json;

/*
Copyright (c) 2002 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import java.io.Serializable;

/**
 * Represents location information for the current location of the
 * {@code Scanner}. This is immutable position data, so represents a
 * point-in-time during the scanning process.
 *
 * @author JSON.org
 * @version 2016-7-12
 */
public class ParsePosition implements Serializable {
    private static final long serialVersionUID = -2189874685522042862L;
    private final long position;
    private final long column;
    private final long line;
    private final char lastCharacter;

    /**
     * Create a new ParsePosition with the given location information.
     *
     * @param position the absolute position
     * @param column the column position
     * @param line the line position
     * @param lastCharacter the most recent character
     */
    public ParsePosition(long position, long column, long line, char lastCharacter) {
        this.position = position;
        this.column = column;
        this.line = line;
        this.lastCharacter = lastCharacter;
    }

    /**
     * Get the absolute position relative to the start of the parsed stream.
     *
     * @return the scanner position
     */
    public long getPosition() {
        return position;
    }

    /**
     * Get the column position for the location in the parsed stream.
     *
     * @return the scanner column position
     */
    public long getColumn() {
        return column;
    }

    /**
     * Get the line position for the location in the parsed stream.
     *
     * @return the scanner line position
     */
    public long getLine() {
        return line;
    }

    /**
     * Get the most recent character parsed by the scanner.
     *
     * @return the most recent character
     */
    public char getLastCharacter() {
        return lastCharacter;
    }

    /**
     * Get a String representation of the parse position.
     *
     * @return the parse position as a String
     */
    public String getPositionDetails() {
        return "at " + this.position + " [character " + this.column + " line " +
                this.line + "]";
    }

    /**
     * Returns a string representation of the parse position.
     *
     * @return a string representation of the parse position
     */
    @Override
    public String toString() {
        return getPositionDetails();
    }
}
