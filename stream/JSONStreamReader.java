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
import org.json.stream.JSONLexer.Token;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * Reads a JSON document as a stream of JSON events. Uses an iterator model
 * to produce each event.
 *
 * @author JSON.org
 * @version 2016-06-26
 */
public final class JSONStreamReader {

    /**
     * States of the internal state machine. Tokens returned from the parser
     * are a subset of these states.
     */
    public enum ParseState {
        /** <em>Internal, not exposed</em> */
        INIT(true),
        /** Start a JSON document */
        DOCUMENT,
        /** Start a JSON object */
        OBJECT,
        /** End a JSON object */
        END_OBJECT,
        /** Start a JSON array */
        ARRAY,
        /** End a JSON array */
        END_ARRAY,
        /** <em>Internal, not exposed</em> */
        KEY_SEPARATOR(true),
        /** <em>Internal, not exposed</em> */
        VALUE_SEPARATOR(true),
        /** A key of a JSON object */
        KEY,
        /** A value of a JSON object or JSON array. See {@link ValueType} for possible value types. */
        VALUE,
        /** End a JSON document */
        END_DOCUMENT;

        /**
         * Indicates the state should not be visible outside the internal
         * parse loop. Keep looping until we find a public state.
         */
        private final boolean internalState;

        ParseState() {
            this.internalState = false;
        }

        ParseState(boolean internalState) {
            this.internalState = internalState;
        }

        boolean isInternalState() {
            return internalState;
        }
    }

    /**
     * The type of the current value in the stream. Arrays and Objects are
     * represented as {@link ParseState} values.
     */
    public enum ValueType {
        /** The {@code JSONObject.Null} value */
        NULL_VALUE,
        /** The {@code Boolean.TRUE} or {@code Boolean.FALSE} value */
        BOOLEAN_VALUE,
        /** An {@code Integer}, {@code Long}, or {@code Double} value */
        NUMBER_VALUE,
        /** A {@code String} value */
        STRING_VALUE,
        /** Non-value, such as a key, object, array, or other structure type */
        NOT_A_VALUE
    }

    private final JSONLexer lexer;
    private final ALStack<Token> objectStack = new ALStack<Token>(); // START_ARRAY, START_OBJECT, or one of the _VALUEs
    private final BufferedAppendable bufferedAppender;
    private ParseState state = ParseState.INIT;

    /**
     * Construct a JSONStreamReader from a {@code Reader}.
     *
     * @param reader     A reader.
     */
    public JSONStreamReader(Reader reader) {
        lexer = new JSONLexer(reader);
        bufferedAppender = new BufferedAppendable();
    }

    /**
     * Construct a JSONStreamReader from an {@code InputStream} and supplied
     * {@code Charset}.
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
     * Construct a JSONStreamReader from a {@code String}.
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
                        case TRUE_VALUE:
                        case FALSE_VALUE:
                        case NUMBER_VALUE:
                        case STRING_VALUE:
                            objectStack.push(token);
                            state = ParseState.VALUE;
                            break;
                        default:
                            throw lexer.syntaxError("Unexpected token");
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
                            throw lexer.syntaxError("Unexpected token");
                    }
                    break;

                case KEY:
                    if(objectStack.isEmpty()) {
                        throw lexer.syntaxError("Invalid state");
                    }
                    lexer.nextString(NullAppendable.INSTANCE);
                    state = ParseState.KEY_SEPARATOR;

                    if(lexer.nextTokenType() != Token.KEY_SEPARATOR) {
                        throw lexer.syntaxError("Expected key separator");
                    }
                    break;

                case KEY_SEPARATOR:
                    parseStartValue();
                    break;

                case VALUE:
                    if(objectStack.isEmpty()) {
                        throw lexer.syntaxError("Invalid state");
                    }
                    token = objectStack.pop();
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
                            throw lexer.syntaxError("Invalid state");
                    }
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
                                        throw lexer.syntaxError("Unexpected token");
                                    }
                                    break;
                                case START_ARRAY:
                                    parseStartValue();
                                    break;
                                default:
                                    throw lexer.syntaxError("Invalid state");
                            }
                            break;
                        case END_ARRAY:
                            if(objectStack.pop() == Token.START_ARRAY) {
                                state = ParseState.END_ARRAY;
                            } else {
                                throw lexer.syntaxError("Invalid state");
                            }
                            break;
                        case END_OBJECT:
                            if(objectStack.pop() == Token.START_OBJECT) {
                                state = ParseState.END_OBJECT;
                            } else {
                                throw lexer.syntaxError("Invalid state");
                            }
                            break;
                        default:
                            throw lexer.syntaxError("Invalid token");
                    }
                    break;

                case END_DOCUMENT:
                    break;

            }
        } while (state.isInternalState());

        return state;
    }

    private void parseStartValue() throws JSONException {
        Token token;
        token = lexer.nextTokenType();
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
            case TRUE_VALUE:
            case FALSE_VALUE:
            case NUMBER_VALUE:
            case STRING_VALUE:
                objectStack.push(token);
                state = ParseState.VALUE;
                break;
            default:
                throw lexer.syntaxError("Invalid token");
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
            throw lexer.syntaxError("Invalid state");
        }

        StringBuilder builder = lexer.nextString(new StringBuilder());
        state = ParseState.KEY_SEPARATOR;

        if(lexer.nextTokenType() != Token.KEY_SEPARATOR) {
            throw lexer.syntaxError("Expected key separator");
        }

        return builder.toString();
    }

    /**
     * If the ParseState was {@link ParseState#VALUE}, return the value type of
     * the next value. If the value is an object, array, or other structural
     * element, {@link ValueType#NOT_A_VALUE} is returned.
     * <p>
     * Does not distinguish between int, long, and double number value types.</p>
     * <p>
     * This does <em>not</em> advance the parser state.</p>
     *
     * @return one of the {@code ValueType} values
     */
    public ValueType getValueType() {
        if(objectStack.isEmpty()) {
            return ValueType.NOT_A_VALUE;
        }
        switch(objectStack.peek()) {
            case NULL_VALUE:
                return ValueType.NULL_VALUE;
            case TRUE_VALUE:
            case FALSE_VALUE:
                return ValueType.BOOLEAN_VALUE;
            case NUMBER_VALUE:
                return ValueType.NUMBER_VALUE;
            case STRING_VALUE:
                return ValueType.STRING_VALUE;
            default:
                return ValueType.NOT_A_VALUE;
        }
    }

    /**
     * If the ParseState was {@link ParseState#VALUE}, return the value as
     * an Object. The values that can be returned here are:
     * <ul>
     *     <li>{@code JSONObject.NULL}</li>
     *     <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     *     <li>A {@code Double}, {@code Long}, or {@code Integer}</li>
     *     <li>A {@code String}</li>
     * </ul>
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return an Object of the type described above
     */
    public Object nextValue() throws JSONException {
        if((state != ParseState.VALUE) || (objectStack.isEmpty())) {
            throw lexer.syntaxError("Invalid state");
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
            case NUMBER_VALUE:
                state = ParseState.VALUE_SEPARATOR;
                return lexer.nextNumber();
            default:
                throw lexer.syntaxError("Invalid state");
        }
    }

    /**
     * If the ParseState was {@link ParseState#VALUE}, and ValueType was
     * {@link ValueType#STRING_VALUE}, return the value as a String.
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return a String value
     */
    public String nextStringValue() throws JSONException {
        if((state != ParseState.VALUE) || (objectStack.isEmpty())) {
            throw lexer.syntaxError("Invalid state");
        }

        Token token = objectStack.pop();
        switch(token) {
            case STRING_VALUE:
                state = ParseState.VALUE_SEPARATOR;
                return lexer.nextString(new StringBuilder()).toString();
            case NULL_VALUE:
            case TRUE_VALUE:
            case FALSE_VALUE:
            case NUMBER_VALUE:
                throw lexer.syntaxError("Invalid value type");
            default:
                throw lexer.syntaxError("Invalid state");
        }
    }

    /**
     * If the ParseState was {@link ParseState#VALUE}, and ValueType was
     * {@link ValueType#STRING_VALUE}, append the decoded value to the given
     * {@code Appendable}.
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
        if((state != ParseState.VALUE) || (objectStack.isEmpty())) {
            throw lexer.syntaxError("Invalid state");
        }
        if(writer == null) {
            throw new JSONException("writer is null");
        }

        Token token = objectStack.pop();
        switch(token) {
            case STRING_VALUE:
            try {
                state = ParseState.VALUE_SEPARATOR;
                BufferedAppendable buff = bufferedAppender.with(writer);
                try {
                    lexer.nextString(buff);
                } finally {
                    buff.close();
                }
                return writer;
            } catch(IOException e) {
                throw new JSONException("Error parsing string value", e);
            }
            case NULL_VALUE:
            case TRUE_VALUE:
            case FALSE_VALUE:
            case NUMBER_VALUE:
                throw lexer.syntaxError("Invalid value type");
            default:
                throw lexer.syntaxError("Invalid state");
        }
    }

    /**
     * If the ParseState was {@link ParseState#VALUE}, and ValueType was
     * {@link ValueType#BOOLEAN_VALUE}, return the value as a boolean.
     * <p>
     * If the JSON value is not parseable as a boolean, as defined by the JSON
     * grammar, a JSONException will be thrown.</p>
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return a boolean value
     */
    public boolean nextBooleanValue() throws JSONException {
        if((state != ParseState.VALUE) || (objectStack.isEmpty())) {
            throw lexer.syntaxError("Invalid state");
        }

        Token token = objectStack.pop();
        switch(token) {
            case TRUE_VALUE:
                state = ParseState.VALUE_SEPARATOR;
                return true;
            case FALSE_VALUE:
                state = ParseState.VALUE_SEPARATOR;
                return false;
            case NULL_VALUE:
            case STRING_VALUE:
            case NUMBER_VALUE:
                throw lexer.syntaxError("Invalid value type");
            default:
                throw lexer.syntaxError("Invalid state");
        }
    }

    /**
     * If the ParseState was {@link ParseState#VALUE}, and ValueType was
     * {@link ValueType#NUMBER_VALUE}, return the value as a Number.
     * <p>
     * The number type returned is one of:</p>
     * <ul>
     *     <li>A {@code Double}</li>
     *     <li>A {@code Long}</li>
     *     <li>An {@code Integer}</li>
     * </ul>
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return a value that is a subclass of the {@code Number} class
     */
    public Number nextNumberValue() throws JSONException {
        if((state != ParseState.VALUE) || (objectStack.isEmpty())) {
            throw lexer.syntaxError("Invalid state");
        }

        Token token = objectStack.pop();
        switch(token) {
            case NUMBER_VALUE:
                state = ParseState.VALUE_SEPARATOR;
                return lexer.nextNumber();
            case NULL_VALUE:
            case TRUE_VALUE:
            case FALSE_VALUE:
            case STRING_VALUE:
                throw lexer.syntaxError("Invalid value type");
            default:
                throw lexer.syntaxError("Invalid state");
        }
    }

    /**
     * If the ParseState was {@link ParseState#VALUE}, and ValueType was
     * {@link ValueType#NUMBER_VALUE}, return the value as a BigDecimal.
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return a number value as a {@code BigDecimal} class
     */
    public BigDecimal nextBigDecimalValue() throws JSONException {
        if((state != ParseState.VALUE) || (objectStack.isEmpty())) {
            throw lexer.syntaxError("Invalid state");
        }

        Token token = objectStack.pop();
        switch(token) {
            case NUMBER_VALUE: {
                state = ParseState.VALUE_SEPARATOR;
                StringBuilder builder = new StringBuilder();
                lexer.nextNumber(builder);
                try {
                    return new BigDecimal(builder.toString());
                } catch (Exception exception) {
                    // fall through
                }
            }
            case NULL_VALUE:
            case TRUE_VALUE:
            case FALSE_VALUE:
            case STRING_VALUE:
                throw lexer.syntaxError("Invalid value type");
            default:
                throw lexer.syntaxError("Invalid state");
        }
    }

    /**
     * If the ParseState was {@link ParseState#VALUE}, and ValueType was
     * {@link ValueType#NUMBER_VALUE}, return the value as a BigInteger.
     * <p>
     * If the JSON value is not parseable as a big integer, as defined by the
     * JSON grammar, a JSONException will be thrown.</p>
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return a number value as a {@code BigInteger} class
     */
    public BigInteger nextBigIntegerValue() throws JSONException {
        if((state != ParseState.VALUE) || (objectStack.isEmpty())) {
            throw lexer.syntaxError("Invalid state");
        }

        Token token = objectStack.pop();
        switch(token) {
            case NUMBER_VALUE: {
                state = ParseState.VALUE_SEPARATOR;
                StringBuilder builder = new StringBuilder();
                boolean dbl = lexer.nextNumber(builder);
                if(!dbl) {
                    try {
                        return new BigInteger(builder.toString());
                    } catch (Exception exception) {
                        // fall through
                    }
                }
            }
            case NULL_VALUE:
            case TRUE_VALUE:
            case FALSE_VALUE:
            case STRING_VALUE:
                throw lexer.syntaxError("Invalid value type");
            default:
                throw lexer.syntaxError("Invalid state");
        }
    }

    /**
     * If the ParseState was {@link ParseState#VALUE}, and ValueType was
     * {@link ValueType#NUMBER_VALUE}, return the value as a double.
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return a double value
     */
    public double nextDoubleValue() throws JSONException {
        if((state != ParseState.VALUE) || (objectStack.isEmpty())) {
            throw lexer.syntaxError("Invalid state");
        }

        Token token = objectStack.pop();
        switch(token) {
            case NUMBER_VALUE:
                state = ParseState.VALUE_SEPARATOR;
                return lexer.nextDouble();
            case NULL_VALUE:
            case TRUE_VALUE:
            case FALSE_VALUE:
            case STRING_VALUE:
                throw lexer.syntaxError("Invalid value type");
            default:
                throw lexer.syntaxError("Invalid state");
        }
    }

    /**
     * If the ParseState was {@link ParseState#VALUE}, and ValueType was
     * {@link ValueType#NUMBER_VALUE}, return the value as an int.
     * <p>
     * If the JSON value is not parseable as an int, as defined by the JSON
     * grammar, a JSONException will be thrown.</p>
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return an int value
     */
    public int nextIntValue() throws JSONException {
        if((state != ParseState.VALUE) || (objectStack.isEmpty())) {
            throw lexer.syntaxError("Invalid state");
        }

        Token token = objectStack.pop();
        switch(token) {
            case NUMBER_VALUE:
                state = ParseState.VALUE_SEPARATOR;
                return lexer.nextInt();
            case NULL_VALUE:
            case TRUE_VALUE:
            case FALSE_VALUE:
            case STRING_VALUE:
                throw lexer.syntaxError("Invalid value type");
            default:
                throw lexer.syntaxError("Invalid state");
        }
    }

    /**
     * If the ParseState was {@link ParseState#VALUE}, and ValueType was
     * {@link ValueType#NUMBER_VALUE}, return the value as a long.
     * <p>
     * If the JSON value is not parseable as a long, as defined by the JSON
     * grammar, a JSONException will be thrown.</p>
     * <p>
     * This method advances the parser onto the next state.</p>
     *
     * @return a long value
     */
    public long nextLongValue() throws JSONException {
        if((state != ParseState.VALUE) || (objectStack.isEmpty())) {
            throw lexer.syntaxError("Invalid state");
        }

        Token token = objectStack.pop();
        switch(token) {
            case NUMBER_VALUE:
                state = ParseState.VALUE_SEPARATOR;
                return lexer.nextLong();
            case NULL_VALUE:
            case TRUE_VALUE:
            case FALSE_VALUE:
            case STRING_VALUE:
                throw lexer.syntaxError("Invalid value type");
            default:
                throw lexer.syntaxError("Invalid state");
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
     * Skip over the content of the current object or array, including any
     * nested objects or arrays.
     *
     * @return the closing ParseState of the object or array
     */
    public ParseState skipToEndObject() throws JSONException {
        if(objectStack.isEmpty()) {
            throw lexer.syntaxError("Invalid state");
        }

        int stackDepth = getStackDepth();

        switch(objectStack.peek()) {
            case START_OBJECT:
            case START_ARRAY:
                while(hasNext() && (objectStack.size() >= stackDepth)) {
                    nextState();
                }
                break;
            default:
                throw lexer.syntaxError("Invalid state");
        }
        return state;
    }

    /**
     * Create a JSONException given the current stream position.
     *
     * @param message the exception message
     * @return a new JSONException object with the given message
     */
    public JSONException syntaxError(String message) {
        return lexer.syntaxError(message);
    }

    /**
     * Create a JSONException given the current stream position.
     *
     * @param message the exception message
     * @param t the underlying exception
     * @return a new JSONException object with the given message and cause
     */
    public JSONException syntaxError(String message, Throwable t) {
        return lexer.syntaxError(message, t);
    }

    /**
     * Create a JSONException given the current stream position.
     *
     * @param t the underlying exception
     * @return a new JSONException object with the given cause
     */
    public JSONException syntaxError(Throwable t) {
        return lexer.syntaxError(t);
    }

    /**
     * Returns a string representation of the stream reader, including its
     * current position in the source stream.
     */
    @Override
    public String toString() {
        return "JSONStreamReader" + lexer.position();
    }
}
