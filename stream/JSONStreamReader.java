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

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
//import java.util.Iterator;

/**
 * Reads a JSON document as a stream of JSON events. Uses an iterator model
 * to produce each event.
 *
 * @author JSON.org
 * @version 2016-06-26
 */
public final class JSONStreamReader /*implements Iterator<JSONStreamReader.ParseState>*/ {

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
        /** A value of a JSON object or JSON array */
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
     * handled as {@link ParseState} values.
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
    private ParseState state = ParseState.INIT;

    public JSONStreamReader(Reader reader) {
        lexer = new JSONLexer(reader);
    }

    public JSONStreamReader(InputStream is, Charset cs) {
        lexer = new JSONLexer(is, cs);
    }

    public JSONStreamReader(String s) {
        lexer = new JSONLexer(s);
    }

    public boolean hasNext() {
        return state != ParseState.END_DOCUMENT;
    }

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
     * Get the value type of the next value. If the value is an object,
     * array, or other structural element, {@code NOT_A_VALUE} is returned.
     * Does not distinguish between int, long, and double number value types.
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

    public int getStackDepth() {
        return objectStack.size();
    }

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

    public JSONException syntaxError(String message) {
        return lexer.syntaxError(message);
    }

    public JSONException syntaxError(String message, Throwable t) {
        return lexer.syntaxError(message, t);
    }

    public JSONException syntaxError(Throwable t) {
        return lexer.syntaxError(t);
    }

/*
    @Override
    public ParseState next() throws JSONException {
        return nextState();
    }

    @Override
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
*/

    @Override
    public String toString() {
        return "JSONStreamReader" + lexer.position();
    }
}
