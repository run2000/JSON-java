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
import org.json.ParsePosition;
import org.json.stream.JSONLexer.Token;

import java.io.IOException;
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
public final class JSONStreamReader {

    private static final int NONE = 0;
    private static final int INTERNAL = 1;
    private static final int VALUE = 2;
    private static final int BEGIN_STRUCTURE = 4;
    private static final int END_STRUCTURE = 8;
//    private static final int TEXT = 16;
//    private static final int DOCUMENT_DELIMITER = 32;
//    private static final int ARRAY_DELIMITER = 64;
//    private static final int OBJECT_DELIMITER = 128;

    /**
     * States of the internal state machine. Tokens returned from the
     * {@link JSONStreamReader#nextState() nextState()} loop are a subset of
     * these states.
     */
    public enum ParseState {
        /** <em>Internal state</em> -- before the DOCUMENT state */
        INIT(INTERNAL),
        /** Start a JSON document */
        DOCUMENT(NONE),
        /** Start a JSON object */
        OBJECT(BEGIN_STRUCTURE),
        /** End a JSON object */
        END_OBJECT(END_STRUCTURE),
        /** Start a JSON array */
        ARRAY(BEGIN_STRUCTURE),
        /** End a JSON array */
        END_ARRAY(END_STRUCTURE),
        /** <em>Internal state</em> -- between a KEY and *_VALUE state */
        KEY_SEPARATOR(INTERNAL),
        /** <em>Internal state</em> -- after a *_VALUE state */
        VALUE_SEPARATOR(INTERNAL),
        /** A key of a JSON object */
        KEY(NONE),
        /** The {@code JSONObject.Null} value */
        NULL_VALUE(VALUE),
        /** The {@code Boolean.TRUE} or {@code Boolean.FALSE} value */
        BOOLEAN_VALUE(VALUE),
        /** A {@code Number} value */
        NUMBER_VALUE(VALUE),
        /** A {@code String} value */
        STRING_VALUE(VALUE),
        /** End a JSON document */
        END_DOCUMENT(NONE);

        private final int type;

        ParseState(int type) {
            this.type = type;
        }

        public boolean isInternal() {
            return this.type == INTERNAL;
        }

        public boolean isValue() {
            return this.type == VALUE;
        }

        public boolean isBeginStructure() {
            return this.type == BEGIN_STRUCTURE;
        }

        public boolean isEndStructure() {
            return this.type == END_STRUCTURE;
        }
    }

    private final JSONLexer lexer;
    private final ALStack<Token> objectStack = new ALStack<Token>(); // START_ARRAY, START_OBJECT, or one of the _VALUEs
    private final BufferedAppendable bufferedAppender;
    private ParseState state = ParseState.INIT;

    /**
     * Construct a {@code JSONStreamReader} from a {@code Reader}.
     *
     * @param reader     A reader.
     */
    public JSONStreamReader(Reader reader) {
        lexer = new JSONLexer(reader);
        bufferedAppender = new BufferedAppendable();
    }

    /**
     * Construct a {@code JSONStreamReader} from an {@code InputStream} and
     * a supplied {@code Charset}.
     *
     * @param inputStream   the input stream containing the JSON data
     * @param charset       the character set with which to interpret the
     *                      input stream
     */
    public JSONStreamReader(InputStream inputStream, Charset charset) {
        lexer = new JSONLexer(inputStream, charset);
        bufferedAppender = new BufferedAppendable();
    }

    /**
     * Construct a {@code JSONStreamReader} from a {@code String}.
     *
     * @param s     A source string.
     */
    public JSONStreamReader(String s) {
        lexer = new JSONLexer(s);
        bufferedAppender = new BufferedAppendable();
    }

    /**
     * Are there more parse states remaining in the source stream.
     *
     * @return {@code true} if there are more parse states remaining, otherwise
     *         {@code false}
     */
    public boolean hasNext() {
        return state != ParseState.END_DOCUMENT;
    }

    /**
     * Advance the parser onto the next state in the source stream, and
     * return a {@link ParseState} representing the state that was encountered.
     *
     * @return one of the ParseState values
     * @throws JSONException there was a problem with the source stream
     */
    public ParseState nextState() throws JSONException {
        Token token;

        do {
            switch(state) {
                case INIT:
                    state = ParseState.DOCUMENT;
                    break;

                case DOCUMENT:
                    parseStartValue();
                    break;

                case ARRAY:
                    token = lexer.nextTokenType();
                    switch(token) {
                        case END_ARRAY:
                            objectStack.pop();
                            state = ParseState.END_ARRAY;
                            break;
                        case START_ARRAY:
                            objectStack.push(token);
                            state = ParseState.ARRAY;
                            break;
                        case START_OBJECT:
                            objectStack.push(token);
                            state = ParseState.OBJECT;
                            break;
                        case NULL_VALUE:
                            objectStack.push(token);
                            state = ParseState.NULL_VALUE;
                            break;
                        case TRUE_VALUE:
                        case FALSE_VALUE:
                            objectStack.push(token);
                            state = ParseState.BOOLEAN_VALUE;
                            break;
                        case NUMBER_VALUE:
                            objectStack.push(token);
                            state = ParseState.NUMBER_VALUE;
                            break;
                        case STRING_VALUE:
                            objectStack.push(token);
                            state = ParseState.STRING_VALUE;
                            break;
                        default:
                            throw new JSONParseException("Unexpected token",
                                    lexer.parsePosition());
                    }
                    break;

                case OBJECT:
                    token = lexer.nextTokenType();
                    switch(token) {
                        case END_OBJECT:
                            objectStack.pop();
                            state = ParseState.END_OBJECT;
                            break;
                        case STRING_VALUE:
                            state = ParseState.KEY;
                            break;
                        default:
                            throw new JSONParseException("Unexpected token",
                                    lexer.parsePosition());
                    }
                    break;

                case KEY:
                    if(objectStack.isEmpty()) {
                        throw new JSONParseException("Invalid state",
                                lexer.parsePosition());
                    }
                    lexer.nextString(NullAppendable.INSTANCE);
                    state = ParseState.KEY_SEPARATOR;

                    if(lexer.nextTokenType() != Token.KEY_SEPARATOR) {
                        throw new JSONParseException("Expected key separator",
                                lexer.parsePosition());
                    }
                    break;

                case KEY_SEPARATOR:
                    parseStartValue();
                    break;

                case NULL_VALUE:
                case BOOLEAN_VALUE:
                case NUMBER_VALUE:
                case STRING_VALUE:
                    skipValue();
                    break;

                case VALUE_SEPARATOR:
                case END_ARRAY:
                case END_OBJECT:
                    if(objectStack.isEmpty()) {
                        state = ParseState.END_DOCUMENT;
                        break;
                    }
                    token = lexer.nextTokenType();
                    switch(token) {
                        case VALUE_SEPARATOR:
                            switch (objectStack.peek()) {
                                case START_OBJECT:
                                    token = lexer.nextTokenType();
                                    if (token == Token.STRING_VALUE) {
                                        state = ParseState.KEY;
                                    } else {
                                        throw new JSONParseException("Unexpected token",
                                                lexer.parsePosition());
                                    }
                                    break;
                                case START_ARRAY:
                                    parseStartValue();
                                    break;
                                default:
                                    throw new JSONParseException("Invalid state",
                                            lexer.parsePosition());
                            }
                            break;
                        case END_ARRAY:
                            if(objectStack.pop() == Token.START_ARRAY) {
                                state = ParseState.END_ARRAY;
                            } else {
                                throw new JSONParseException("Invalid state",
                                        lexer.parsePosition());
                            }
                            break;
                        case END_OBJECT:
                            if(objectStack.pop() == Token.START_OBJECT) {
                                state = ParseState.END_OBJECT;
                            } else {
                                throw new JSONParseException("Invalid state",
                                        lexer.parsePosition());
                            }
                            break;
                        default:
                            throw new JSONParseException("Invalid token",
                                    lexer.parsePosition());
                    }
                    break;

                case END_DOCUMENT:
                    break;

            }
        } while (state.isInternal());

        return state;
    }

    private void parseStartValue() throws JSONException {
        Token token = lexer.nextTokenType();

        switch(token) {
            case START_ARRAY:
                objectStack.push(token);
                state = ParseState.ARRAY;
                break;
            case START_OBJECT:
                objectStack.push(token);
                state = ParseState.OBJECT;
                break;
            case NULL_VALUE:
                objectStack.push(token);
                state = ParseState.NULL_VALUE;
                break;
            case TRUE_VALUE:
            case FALSE_VALUE:
                objectStack.push(token);
                state = ParseState.BOOLEAN_VALUE;
                break;
            case NUMBER_VALUE:
                objectStack.push(token);
                state = ParseState.NUMBER_VALUE;
                break;
            case STRING_VALUE:
                objectStack.push(token);
                state = ParseState.STRING_VALUE;
                break;
            default:
                throw new JSONParseException("Invalid token", lexer.parsePosition());
        }
    }

    private void skipValue() {
        if (objectStack.isEmpty()) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();

        switch(token) {
            case NULL_VALUE:
            case TRUE_VALUE:
            case FALSE_VALUE:
                state = ParseState.VALUE_SEPARATOR;
                break;
            case STRING_VALUE:
                state = ParseState.VALUE_SEPARATOR;
                lexer.nextString(NullAppendable.INSTANCE);
                break;
            case NUMBER_VALUE:
                state = ParseState.VALUE_SEPARATOR;
                lexer.nextNumber(NullAppendable.INSTANCE);
                break;
            default:
                throw new JSONParseException("Invalid state", lexer.parsePosition());
        }
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
    public String nextKey() throws JSONException {
        if(state != ParseState.KEY) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        StringBuilder builder = lexer.nextString(new StringBuilder());
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
                return lexer.nextString(new StringBuilder()).toString();
            case NUMBER_VALUE: {
                state = ParseState.VALUE_SEPARATOR;
                StringBuilder sb = new StringBuilder();
                boolean isDbl = lexer.nextNumber(sb);
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
    public String nextStringValue() throws JSONException {
        if ((state != ParseState.STRING_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        if (token == Token.STRING_VALUE) {
            state = ParseState.VALUE_SEPARATOR;
            return lexer.nextString(new StringBuilder()).toString();
        } else {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }
    }

    /**
     * If the ParseState was {@link ParseState#STRING_VALUE},
     * append the decoded value to the given {@code Appendable}.
     * <p>
     * This method is suitable for cases where very long String data is
     * expected, for instance for base-64 encoded data.</p>
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @param <T> the type of Appendable, returned to the caller
     * @param writer the writer to which the String value will be written
     * @return the given Appendable object
     */
    public <T extends Appendable> T appendNextStringValue(T writer) throws JSONException {
        if ((state != ParseState.STRING_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }
        if(writer == null) {
            throw new JSONException("writer is null");
        }

        Token token = objectStack.pop();
        if (token == Token.STRING_VALUE) {
            try {
                state = ParseState.VALUE_SEPARATOR;
                BufferedAppendable buff = bufferedAppender.with(writer);
                try {
                    lexer.nextString(buff);
                } finally {
                    buff.close();
                }
                return writer;
            } catch (IOException e) {
                throw new JSONParseException("Error parsing string value", e,
                        lexer.parsePosition());
            }
        } else {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }
    }

    /**
     * If the ParseState was {@link ParseState#BOOLEAN_VALUE},
     * return the value as a boolean.
     * <p>
     * If the JSON value is not parseable as a boolean, as defined by the JSON
     * grammar, a JSONException will be thrown.</p>
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return a boolean value
     */
    public boolean nextBooleanValue() throws JSONException {
        if ((state != ParseState.BOOLEAN_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        switch(token) {
            case TRUE_VALUE:
                state = ParseState.VALUE_SEPARATOR;
                return true;
            case FALSE_VALUE:
                state = ParseState.VALUE_SEPARATOR;
                return false;
            default:
                throw new JSONParseException("Invalid state", lexer.parsePosition());
        }
    }

    /**
     * If the ParseState was {@link ParseState#NULL_VALUE},
     * return the value as a NULL object.
     * <p>
     * If the JSON value is not parseable as a null, as defined by the JSON
     * grammar, a JSONException will be thrown.</p>
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return the JSONObject.NULL value
     */
    public Object nextNullValue() throws JSONException {
        if((state != ParseState.NULL_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        if(token == Token.NULL_VALUE) {
            state = ParseState.VALUE_SEPARATOR;
            return JSONObject.NULL;
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
    public Number nextNumberValue() throws JSONException {
        if ((state != ParseState.NUMBER_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        if (token == Token.NUMBER_VALUE) {
            state = ParseState.VALUE_SEPARATOR;
            StringBuilder sb = new StringBuilder();
            boolean isDbl = lexer.nextNumber(sb);
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
    public <T extends Appendable> T appendNextNumberValue(T writer) throws JSONException {
        if ((state != ParseState.NUMBER_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        if (token == Token.NUMBER_VALUE) {
            state = ParseState.VALUE_SEPARATOR;
            lexer.nextNumber(writer);
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
    public BigDecimal nextBigDecimalValue() throws JSONException {
        if ((state != ParseState.NUMBER_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        if (token == Token.NUMBER_VALUE) {
            state = ParseState.VALUE_SEPARATOR;
            StringBuilder sb = new StringBuilder();
            lexer.nextNumber(sb);
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
    public BigInteger nextBigIntegerValue() throws JSONException {
        if ((state != ParseState.NUMBER_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        if (token == Token.NUMBER_VALUE) {
            state = ParseState.VALUE_SEPARATOR;
            StringBuilder sb = new StringBuilder();
            boolean isDbl = lexer.nextNumber(sb);
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
    public double nextDoubleValue() throws JSONException {
        if ((state != ParseState.NUMBER_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        if (token == Token.NUMBER_VALUE) {
            state = ParseState.VALUE_SEPARATOR;
            StringBuilder sb = new StringBuilder();
            boolean isDbl = lexer.nextNumber(sb);
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
    public int nextIntValue() throws JSONException {
        if ((state != ParseState.NUMBER_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        if (token == Token.NUMBER_VALUE) {
            state = ParseState.VALUE_SEPARATOR;
            StringBuilder sb = new StringBuilder();
            boolean isDbl = lexer.nextNumber(sb);
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
    public long nextLongValue() throws JSONException {
        if ((state != ParseState.NUMBER_VALUE) || (objectStack.isEmpty())) {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }

        Token token = objectStack.pop();
        if (token == Token.NUMBER_VALUE) {
            state = ParseState.VALUE_SEPARATOR;
            StringBuilder sb = new StringBuilder();
            boolean isDbl = lexer.nextNumber(sb);
            return decodeLong(sb.toString(), isDbl);
        } else {
            throw new JSONParseException("Invalid state", lexer.parsePosition());
        }
    }

    /**
     * Get the depth of nested objects, arrays, or values.
     *
     * @return the depth of nested objects, arrays, or values
     */
    public int getStackDepth() {
        return objectStack.size();
    }

    /**
     * Indicates the current position of the scanner.
     *
     * @return a {@code ParsePosition} representing the current location of
     * the scanner
     */
    public ParsePosition getParsePosition() {
        return lexer.parsePosition();
    }

    /**
     * Return the <em>current</em> state of the parser, including any of the
     * internal states. Only required in exceptional circumstances.
     *
     * @return the current {@link ParseState}
     */
    public ParseState currentState() {
        return state;
    }

    /**
     * Skip over the content of the current object or array, including any
     * nested objects or arrays.
     *
     * @return the closing ParseState of the object or array
     */
    public ParseState skipToEndStructure() throws JSONException {
        if(state.isValue()) {
            skipValue();
        }

        final int stackDepth = getStackDepth();

        if(stackDepth == 0) {
            state = ParseState.END_DOCUMENT;
        } else {
            switch(objectStack.peek()) {
                case START_OBJECT:
                case START_ARRAY:
                    while(hasNext() && (objectStack.size() >= stackDepth)) {
                        nextState();
                    }
                    break;
                default:
                    throw new JSONParseException("Invalid state", lexer.parsePosition());
            }
        }
        return state;
    }

    /**
     * Decode a number strictly according to the JSON specification.
     *
     * @return the number represented by the token sequence
     */
    private Number decodeNumber(String val, boolean isDbl) throws JSONException {

        if (isDbl) {
            try {
                Double d = Double.valueOf(val);
                if (!d.isInfinite() && !d.isNaN()) {
                    return d;
                }
            } catch (Exception ignore) {
                // fall through
            }
            try {
                BigDecimal bd = new BigDecimal(val);
                return bd;
            } catch (Exception e) {
                // fall through
            }
        } else {
            try {
                Long myLong = Long.valueOf(val);
                if (myLong.longValue() == myLong.intValue()) {
                    return Integer.valueOf(myLong.intValue());
                } else {
                    return myLong;
                }
            } catch (Exception ignore) {
                // fall through
            }
            try {
                BigInteger bi = new BigInteger(val);
                return bi;
            } catch(Exception e) {
                // fall through
            }
        }
        throw new JSONParseException("Could not parse number", lexer.parsePosition());
    }

    /**
     * Decode a number as a double strictly according to the JSON specification.
     *
     * @return the number represented by the token sequence
     */
    private double decodeDouble(String val, boolean isDbl) throws JSONException {

        try {
            if(isDbl) {
                double d = Double.parseDouble(val);
                if ((!Double.isInfinite(d)) && (!Double.isNaN(d))) {
                    return d;
                }
            } else {
                long l = Long.parseLong(val);
                return (double)l;
            }
        } catch (Exception ignore) {
            // fall through
        }
        throw new JSONParseException("Could not parse double", lexer.parsePosition());
    }

    /**
     * Parse a number as an int strictly according to the JSON specification.
     * No coercion of double or long values.
     *
     * @return the number represented by the token sequence
     */
    private int decodeInt(String val, boolean isDbl) throws JSONException {
        try {
            if (!isDbl) {
                int i = Integer.parseInt(val);
                return i;
            }
        } catch (Exception ignore) {
            // fall through
        }
        throw new JSONParseException("Could not parse int", lexer.parsePosition());
    }

    /**
     * Parse a number as a long strictly according to the JSON specification.
     * No coercion of double values.
     *
     * @return the number represented by the token sequence
     */
    private long decodeLong(String val, boolean isDbl) throws JSONException {
        try {
            if (!isDbl) {
                long l = Long.parseLong(val);
                return l;
            }
        } catch (Exception ignore) {
            // fall through
        }
        throw new JSONParseException("Could not parse int", lexer.parsePosition());
    }

    /**
     * Parse a number as a {@code BigDecimal} strictly according to the JSON
     * specification.
     *
     * @return the number represented by the token sequence
     */
    private BigDecimal decodeBigDecimal(String val) throws JSONException {

        try {
            return new BigDecimal(val);
        } catch (Exception exception) {
            // fall through
        }
        throw new JSONParseException("Could not parse big decimal", lexer.parsePosition());
    }

    /**
     * Parse a number as a {@code BigInteger} strictly according to the JSON
     * specification.
     *
     * @return the number represented by the token sequence
     */
    private BigInteger decodeBigInteger(String val, boolean isDbl) throws JSONException {

        if(!isDbl) {
            try {
                return new BigInteger(val);
            } catch (Exception exception) {
                // fall through
            }
        }
        throw new JSONParseException("Could not parse big integer", lexer.parsePosition());
    }

    /**
     * Returns a string representation of the stream reader, including its
     * current position in the source stream.
     */
    @Override
    public String toString() {
        return "JSONStreamReader " + lexer.parsePosition().getPositionDetails();
    }
}
