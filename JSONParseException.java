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

    public JSONParseException(String message, ParsePosition pos) {
        super(message + ' ' + pos.getPositionDetails());
        this.position = pos;
    }

    public JSONParseException(String message, Throwable cause, ParsePosition pos) {
        super(message + ' ' + pos.getPositionDetails(), cause);
        this.position = pos;
    }

    public JSONParseException(Throwable cause, ParsePosition pos) {
        super(pos.getPositionDetails(), cause);
        this.position = pos;
    }

    public ParsePosition getPosition() {
        return position;
    }

    public String getPositionDetails() {
        return position.getPositionDetails();
    }
}
