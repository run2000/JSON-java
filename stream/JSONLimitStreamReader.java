package org.json.stream;

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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONParseException;
import org.json.stream.JSONLexer.Token;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * Reads a JSON document as a stream of JSON events. Uses an iterator model
 * to produce each event. Similar to a StAX event model in XML processing.
 * <p>
 * A simple example of how to use this class:</p>
 * <pre>
 * JSONStreamReader reader = new JSONStreamReader(JSON_TEXT);
 *
 * while(reader.hasNext()) {
 *     ParseState state = reader.nextState();
 *     switch(state) {
 *         case KEY:
 *             System.out.println("KEY = " + reader.nextKey() );
 *             break;
 *         case NULL_VALUE:
 *         case BOOLEAN_VALUE:
 *         case NUMBER_VALUE:
 *         case STRING_VALUE:
 *             System.out.println(state + " = " + reader.nextValue());
 *             break;
 *         default:
 *             System.out.println(state);
 *     }
 * }
 * </pre>
 * <p>
 * A more complete example can be found by reading the {@link JSONObjectBuilder}
 * source code.</p>
 *
 * @author JSON.org
 * @version 2016-06-26
 */
public final class JSONLimitStreamReader extends JSONStreamReader {

    private int keyLength = Integer.MAX_VALUE;
    private int stringLength = Integer.MAX_VALUE;
    private int mantissaDigits = Short.MAX_VALUE;
    private int exponentDigits = Byte.MAX_VALUE;

    /**
     * Construct a {@code JSONStreamReader} from a {@code Reader}.
     *
     * @param reader     A reader.
     */
    public JSONLimitStreamReader(Reader reader) {
        super(reader);
    }

    /**
     * Construct a {@code JSONStreamReader} from an {@code InputStream} and
     * a supplied {@code Charset}.
     *
     * @param inputStream   the input stream containing the JSON data
     * @param charset       the character set with which to interpret the
     *                      input stream
     */
    public JSONLimitStreamReader(InputStream inputStream, Charset charset) {
        super(inputStream, charset);
    }

    /**
     * Construct a {@code JSONStreamReader} from a {@code String}.
     *
     * @param s     A source string.
     */
    public JSONLimitStreamReader(String s) {
        super(s);
    }

    /**
     * Set keyLength, stringLength, mantissaDigits, and exponentDigits
     * using values from the supplied {@link BuilderLimits} object.
     *
     * @param limits the limits to be set
     * @return this object
     */
    public JSONLimitStreamReader withLimits(BuilderLimits limits) {
        this.keyLength = limits.getKeyLength();
        this.stringLength = limits.getStringLength();
        this.mantissaDigits = limits.getMantissaDigits();
        this.exponentDigits = limits.getExponentDigits();
        return this;
    }

    /**
     * The maximum length of any key.
     */
    public void setKeyLength(int keyLength) {
        this.keyLength = (keyLength <= 0) ? Integer.MAX_VALUE : keyLength;
    }

    /**
     * The maximum length of any string value.
     */
    public void setStringLength(int stringLength) {
        this.stringLength = (stringLength <= 0) ? Integer.MAX_VALUE : stringLength;
    }

    /**
     * The maximum number of mantissa digits in a number.
     */
    public void setMantissaDigits(int mantissaDigits) {
        this.mantissaDigits = (mantissaDigits <= 0) ? Short.MAX_VALUE : mantissaDigits;
    }

    /**
     * The maximum number of exponent digits in a number.
     */
    public void setExponentDigits(int exponentDigits) {
        this.exponentDigits = (exponentDigits < 0) ? Byte.MAX_VALUE : exponentDigits;
    }

    /**
     * If the ParseState was {@link ParseState#KEY}, return the text of the
     * key name.
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return the parsed text of the key
     * @throws JSONException there was a problem with the source stream
     */
    @Override
    public String nextKey() throws JSONException {
        if(state != ParseState.KEY) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        StringBuilder builder = lexer.nextString(new StringBuilder(), keyLength);
        state = ParseState.KEY_SEPARATOR;

        if(lexer.nextTokenType() != Token.KEY_SEPARATOR) {
            throw new JSONParseException("Expected key separator", lexer.parsePosition());
        }

        return builder.toString();
    }

    /**
     * If the ParseState was {@link ParseState#NULL_VALUE},
     * {@link ParseState#BOOLEAN_VALUE}, {@link ParseState#NUMBER_VALUE}, or
     * {@link ParseState#STRING_VALUE}, return the value as an {@code Object}.
     * The values that can be returned here are:
     * <ul>
     *     <li>{@code JSONObject.NULL}</li>
     *     <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     *     <li>A {@code Double}, {@code Long}, {@code Integer},
     *     {@code BigInteger}, or {@code BigDecimal}</li>
     *     <li>A {@code String}</li>
     * </ul>
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return an Object of the type described above
     */
    @Override
    public Object nextValue() throws JSONException {
        if (objectStack.isEmpty() || !state.isValue()) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        switch(token) {
            case NULL_VALUE:
                state = ParseState.VALUE_SEPARATOR;
                return JSONObject.NULL;
            case TRUE_VALUE:
                state = ParseState.VALUE_SEPARATOR;
                return Boolean.TRUE;
            case FALSE_VALUE:
                state = ParseState.VALUE_SEPARATOR;
                return Boolean.FALSE;
            case STRING_VALUE:
                state = ParseState.VALUE_SEPARATOR;
                return lexer.nextString(new StringBuilder(), stringLength).toString();
            case NUMBER_VALUE: {
                state = ParseState.VALUE_SEPARATOR;
                StringBuilder sb = new StringBuilder();
                boolean isDbl = lexer.nextNumber(sb, mantissaDigits, exponentDigits);
                return decodeNumber(sb.toString(), isDbl);
            }
            default:
                throw new JSONParseException("Invalid state", lexer.parsePosition());
        }
    }

    /**
     * If the ParseState was {@link ParseState#STRING_VALUE},
     * return the value as a {@code String}.
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return a String value
     */
    @Override
    public String nextStringValue() throws JSONException {
        if ((state != ParseState.STRING_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        if (token == Token.STRING_VALUE) {
            state = ParseState.VALUE_SEPARATOR;
            return lexer.nextString(new StringBuilder(), stringLength).toString();
        } else {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }
    }

    /**
     * If the ParseState was {@link ParseState#NUMBER_VALUE},
     * return the value as a {@code Number}.
     * <p>
     * The number type returned is one of:</p>
     * <ul>
     *     <li>A {@code Double}</li>
     *     <li>A {@code Long}</li>
     *     <li>An {@code Integer}</li>
     *     <li>A {@code BigDecimal}</li>
     *     <li>A {@code BigInteger}</li>
     * </ul>
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return a value that is a subclass of the {@code Number} class
     */
    @Override
    public Number nextNumberValue() throws JSONException {
        if ((state != ParseState.NUMBER_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        if (token == Token.NUMBER_VALUE) {
            state = ParseState.VALUE_SEPARATOR;
            StringBuilder sb = new StringBuilder();
            boolean isDbl = lexer.nextNumber(sb, mantissaDigits, exponentDigits);
            return decodeNumber(sb.toString(), isDbl);
        } else {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }
    }

    /**
     * If the ParseState was {@link ParseState#NUMBER_VALUE},
     * append the number sequence to the given {@code Appendable}.
     * <p>
     * This method is suitable for cases where the caller wishes to perform
     * their own conversion of number values into a corresponding Object type.
     * </p>
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @param <T> the type of Appendable, returned to the caller
     * @param writer the writer to which the number sequence will be written
     * @return the given Appendable object
     */
    @Override
    public <T extends Appendable> T appendNextNumberValue(T writer) throws JSONException {
        if ((state != ParseState.NUMBER_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        if (token == Token.NUMBER_VALUE) {
            state = ParseState.VALUE_SEPARATOR;
            lexer.nextNumber(writer, mantissaDigits, exponentDigits);
            return writer;
        } else {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }
    }

    /**
     * If the ParseState was {@link ParseState#NUMBER_VALUE},
     * return the value as a {@code BigDecimal}.
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return a number value as a {@code BigDecimal} class
     */
    @Override
    public BigDecimal nextBigDecimalValue() throws JSONException {
        if ((state != ParseState.NUMBER_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        if (token == Token.NUMBER_VALUE) {
            state = ParseState.VALUE_SEPARATOR;
            StringBuilder sb = new StringBuilder();
            lexer.nextNumber(sb, mantissaDigits, exponentDigits);
            return decodeBigDecimal(sb.toString());
        } else {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }
    }

    /**
     * If the ParseState was {@link ParseState#NUMBER_VALUE},
     * return the value as a {@code BigInteger}.
     * <p>
     * If the JSON value is not parseable as a big integer, as defined by the
     * JSON grammar, a JSONException will be thrown.</p>
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return a number value as a {@code BigInteger} class
     */
    @Override
    public BigInteger nextBigIntegerValue() throws JSONException {
        if ((state != ParseState.NUMBER_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        if (token == Token.NUMBER_VALUE) {
            state = ParseState.VALUE_SEPARATOR;
            StringBuilder sb = new StringBuilder();
            boolean isDbl = lexer.nextNumber(sb, mantissaDigits, exponentDigits);
            return decodeBigInteger(sb.toString(), isDbl);
        } else {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }
    }

    /**
     * If the ParseState was {@link ParseState#NUMBER_VALUE},
     * return the value as a double.
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return a double value
     */
    @Override
    public double nextDoubleValue() throws JSONException {
        if ((state != ParseState.NUMBER_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        if (token == Token.NUMBER_VALUE) {
            state = ParseState.VALUE_SEPARATOR;
            StringBuilder sb = new StringBuilder();
            boolean isDbl = lexer.nextNumber(sb, mantissaDigits, exponentDigits);
            return decodeDouble(sb.toString(), isDbl);
        } else {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }
    }

    /**
     * If the ParseState was {@link ParseState#NUMBER_VALUE},
     * return the value as an int.
     * <p>
     * If the JSON value is not parseable as an int, as defined by the JSON
     * grammar, a {@code JSONException} will be thrown.</p>
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return an int value
     */
    @Override
    public int nextIntValue() throws JSONException {
        if ((state != ParseState.NUMBER_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        if (token == Token.NUMBER_VALUE) {
            state = ParseState.VALUE_SEPARATOR;
            StringBuilder sb = new StringBuilder();
            boolean isDbl = lexer.nextNumber(sb, mantissaDigits, exponentDigits);
            return decodeInt(sb.toString(), isDbl);
        } else {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }
    }

    /**
     * If the ParseState was {@link ParseState#NUMBER_VALUE},
     * return the value as a long.
     * <p>
     * If the JSON value is not parseable as a long, as defined by the JSON
     * grammar, a {@code JSONException} will be thrown.</p>
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return a long value
     */
    @Override
    public long nextLongValue() throws JSONException {
        if ((state != ParseState.NUMBER_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        if (token == Token.NUMBER_VALUE) {
            state = ParseState.VALUE_SEPARATOR;
            StringBuilder sb = new StringBuilder();
            boolean isDbl = lexer.nextNumber(sb, mantissaDigits, exponentDigits);
            return decodeLong(sb.toString(), isDbl);
        } else {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }
    }

    /**
     * Returns a string representation of the stream reader, including its
     * current position in the source stream.
     */
    @Override
    public String toString() {
        return "JSONSecStreamReader " + lexer.parsePosition().getPositionDetails();
    }
}
