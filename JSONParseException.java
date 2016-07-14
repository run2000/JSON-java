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

/**
 * Extends JSONException by providing parameterised information about the
 * line, column, and character where the syntax error occurred.
 *
 * @author JSON.org
 * @version 2016-06-17
 */
public class JSONParseException extends JSONException {
    private static final long serialVersionUID = 8010183095901970945L;
    private final ParsePosition position;

    /**
     * Constructs a JSONParseException with an explanatory message and
     * parse position.
     *
     * @param message
     *            Detail about the reason for the exception.
     * @param pos
     *            the current position of the {@code Scanner} where the
     *            error occurred
     */
    public JSONParseException(String message, ParsePosition pos) {
        super(message + ' ' + pos.getPositionDetails());
        this.position = pos;
    }

    /**
     * Constructs a JSONParseException with an explanatory message, cause,
     * and parse position.
     *
     * @param message
     *            Detail about the reason for the exception.
     * @param cause
     *            The cause.
     * @param pos
     *            the current position of the {@code Scanner} where the
     *            error occurred
     */
    public JSONParseException(String message, Throwable cause, ParsePosition pos) {
        super(message + ' ' + pos.getPositionDetails(), cause);
        this.position = pos;
    }

    /**
     * Constructs a new JSONParseException with the specified cause and
     * parse position.
     *
     * @param cause
     *            The cause.
     * @param pos
     *            the current position of the {@code Scanner} where the
     *            error occurred
     */
    public JSONParseException(Throwable cause, ParsePosition pos) {
        super(pos.getPositionDetails(), cause);
        this.position = pos;
    }

    /**
     * Return the parse position where the error occured as a
     * {@code ParsePosition} object.
     *
     * @return the ParsePosition object containing location information
     */
    public ParsePosition getPosition() {
        return position;
    }

    /**
     * Get a String representation of the parse position where the error
     * occurred.
     *
     * @return the parse position as a String
     */
    public String getPositionDetails() {
        return position.getPositionDetails();
    }
}
