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

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * A JSONStrictTokener takes a source stream and extracts characters and tokens
 * from it. It is used by the JSONObject and JSONArray constructors to parse
 * JSON source streams as strictly according to the specification as possible.
 * <p>
 * For a lenient parse, use {@link JSONTokener}.
 * </p>
 *
 * @author JSON.org
 * @version 2016-06-08
 */
public class JSONStrictTokener extends JSONTokener {

    /**
     * Construct a JSONStrictTokener from a Reader.
     *
     * @param reader     A reader.
     */
    public JSONStrictTokener(Reader reader) {
        super(reader);
    }

    /**
     * Construct a JSONStrictTokener from an InputStream and supplied Charset.
     */
    public JSONStrictTokener(InputStream inputStream, Charset charset) {
        super(inputStream, charset);
    }

    /**
     * Construct a JSONStrictTokener from a string.
     *
     * @param s     A source string.
     */
    public JSONStrictTokener(String s) {
        super(s);
    }

    /**
     * Get the next char in the stream, skipping insignificant whitespace.
     * Control characters less than U+0020, apart from newline and
     * carriage return, result in an error.
     *
     * @throws JSONException
     * @return  A character, or 0 if there are no more characters.
     */
    public char nextClean() throws JSONException {
        for (;;) {
            char c = this.next();
            switch(c) {
                case (char)0:
                    if (this.end()) {
                        return c;
                    } else {
                        throw this.syntaxError("Illegal control code");
                    }
                case (char)1:
                case (char)2:
                case (char)3:
                case (char)4:
                case (char)5:
                case (char)6:
                case (char)7:
                case (char)8:
                case (char)11:
                case (char)12:
                case (char)14:
                case (char)15:
                case (char)16:
                case (char)17:
                case (char)18:
                case (char)19:
                case (char)20:
                case (char)21:
                case (char)22:
                case (char)23:
                case (char)24:
                case (char)25:
                case (char)26:
                case (char)27:
                case (char)28:
                case (char)29:
                case (char)30:
                case (char)31:
                    throw this.syntaxError("Illegal control code");
                case (char)9: // \t
                case (char)10: // \n
                case (char)13: // \r
                case (char)32: // ' '
                    // valid whitespace
                    break;
                default:
                    return c;
            }
        }
    }

    /**
     * Get the type of the next token in the stream, skipping whitespace.
     * This parses the literal separators only.
     *
     * @return the Token type representing the next token type
     */
    @Override
    public JSONToken nextTokenType() throws JSONException {
        char c = nextClean();

        switch(c) {
            case '{':
                return JSONToken.START_OBJECT;
            case '}':
                return JSONToken.END_OBJECT;
            case '[':
                return JSONToken.START_ARRAY;
            case ']':
                return JSONToken.END_ARRAY;
            case ':':
                return JSONToken.KEY_SEPARATOR;
            case ',':
                return JSONToken.VALUE_SEPARATOR;
            case (char)0:
                if(this.end()) {
                    return JSONToken.END;
                }
            default:
                return JSONToken.VALUE;
        }
    }

    /**
     * Return the characters up to the next close quote character.
     * Backslash processing is done. The formal JSON format only allows
     * strings in double quotes.
     * @param quote The quoting character, must be
     *      <code>"</code>&nbsp;<small>(double quote)</small>.
     * @return      A String.
     * @throws JSONException Unterminated string.
     */
    @Override
    public String nextString(char quote) throws JSONException {
        StringBuilder sb = new StringBuilder();
        char c;

        if(quote != '"') {
            throw new JSONException("Unexpected string delimiter");
        }
        for (;;) {
            c = this.next();
            switch (c) {
                case (char)0:
                case (char)10: // \n
                case (char)13: // \r
                    throw this.syntaxError("Unterminated string");
                case (char)1:
                case (char)2:
                case (char)3:
                case (char)4:
                case (char)5:
                case (char)6:
                case (char)7:
                case (char)8:
                case (char)9:
                case (char)11:
                case (char)12:
                case (char)14:
                case (char)15:
                case (char)16:
                case (char)17:
                case (char)18:
                case (char)19:
                case (char)20:
                case (char)21:
                case (char)22:
                case (char)23:
                case (char)24:
                case (char)25:
                case (char)26:
                case (char)27:
                case (char)28:
                case (char)29:
                case (char)30:
                case (char)31:
                    throw this.syntaxError("Unescaped control code");
                case '\\':
                    c = this.next();
                    switch (c) {
                        case 'b':
                            sb.append('\b');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 'u':
                            try {
                                sb.append((char)Integer.parseInt(this.next(4), 16));
                            } catch (NumberFormatException e) {
                                throw new JSONException("Illegal unicode escape");
                            }
                            break;
                        case '"':
                        case '\\':
                        case '/':
                            sb.append(c);
                            break;
                        default:
                            throw this.syntaxError("Illegal escape");
                    }
                    break;
                case '"':
                    return sb.toString();
                default:
                    sb.append(c);
                    break;
            }
        }
    }

    /**
     * Get the next key. This must be a quoted String value.
     *
     * @throws JSONException If syntax error.
     *
     * @return An object.
     */
    @Override
    public String nextKey() throws JSONException {
        char c = this.nextClean();

        switch (c) {
            case '"':
                return pooledKey(this.nextString(c));
            case '{':
                this.back();
                throw this.syntaxError("Expected key, found object");
            case '[':
                this.back();
                throw this.syntaxError("Expected key, found array");
            default:
                throw this.syntaxError("Expected key");
        }
    }

    /**
     * Get the next value. The value can be a Boolean, Double, Integer,
     * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
     * @throws JSONException If syntax error.
     *
     * @return An object.
     */
    public Object nextValue() throws JSONException {
        char c = this.nextClean();

        switch (c) {
            case '"':
                return this.nextString(c);
            case '{':
                this.back();
                return this.nextJSONObject();
            case '[':
                this.back();
                return this.nextJSONArray();
        }

        /*
         * Handle unquoted text. This could be the values true, false, or
         * null, or it could be a number. In strict mode, avoid accepting
         * non-standard forms.
         *
         * Accumulate characters until we reach the end of a valid syntactic
         * form. Any illegal suffix can be rejected when the next token is
         * parsed.
         */

        switch (c) {
            case 't':
                this.next('r');
                this.next('u');
                this.next('e');
                return Boolean.TRUE;
            case 'f':
                this.next('a');
                this.next('l');
                this.next('s');
                this.next('e');
                return Boolean.FALSE;
            case 'n':
                this.next('u');
                this.next('l');
                this.next('l');
                return JSONObject.NULL;
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                this.back();
                return nextNumber();
            default:
                throw this.syntaxError("Unexpected value");
        }
    }

    /**
     * Parse a JSON object strictly according to the JSON specification.
     *
     * @return the JSON object represented by the token sequence
     */
    protected JSONObject nextJSONObject() {
        JSONObject obj = new JSONObject();
        JSONToken c = this.nextTokenType();
        String key;

        // Parse the start object
        if (c != JSONToken.START_OBJECT) {
            throw this.syntaxError("A JSONObject text must begin with '{'");
        }

        // Parse the key
        c = this.nextTokenType();
        if(c == JSONToken.END_OBJECT) {
            return obj;
        }

        for (;;) {
            // Parse the key
            if (c == JSONToken.VALUE) {
                this.back();
                key = this.nextKey();
            } else {
                throw this.syntaxError("A JSONObject text must end with '}'");
            }

            // The key is followed by the key separator.
            c = this.nextTokenType();
            if (c == JSONToken.KEY_SEPARATOR) {
                obj.putOnce(key, this.nextValue());
            } else {
                throw this.syntaxError("Expected a ':' after a key");
            }

            // Pairs are separated by value separators.
            c = this.nextTokenType();
            switch (c) {
                case VALUE_SEPARATOR:
                    break;
                case END_OBJECT:
                    return obj;
                default:
                    throw this.syntaxError("Expected a ',' or '}'");
            }

            c = this.nextTokenType();
        }
    }

    /**
     * Parse a JSON array strictly according to the JSON specification.
     *
     * @return the JSON array represented by the token sequence
     */
    protected JSONArray nextJSONArray() {
        JSONArray arr = new JSONArray();
        JSONToken c = this.nextTokenType();

        if (c != JSONToken.START_ARRAY) {
            throw this.syntaxError("A JSONArray text must start with '['");
        }

        c = this.nextTokenType();
        if(c == JSONToken.END_ARRAY) {
            return arr;
        }
        for (;;) {
            switch(c) {
                case VALUE:
                case START_ARRAY:
                case START_OBJECT:
                    this.back();
                    arr.put(this.nextValue());
                    break;
                default:
                    throw this.syntaxError("expected array value");
            }

            c = this.nextTokenType();
            switch (c) {
                case VALUE_SEPARATOR:
                    break;
                case END_ARRAY:
                    return arr;
                default:
                    throw this.syntaxError("Expected a ',' or ']'");
            }

            c = this.nextTokenType();
        }
    }

    /**
     * Parse a number strictly according to the JSON specification.
     *
     * @return the number represented by the token sequence
     */
    protected Number nextNumber() {
        StringBuilder sb = new StringBuilder();
        boolean dbl = false;
        char c = this.next();

        // likely digit
        if(c == '-') {
            sb.append(c);
            c = this.next();
        }
        if(c == '0') {
            sb.append(c);
            c = this.next();
        } else if((c >= '1') && (c <= '9')) {
            sb.append(c);

            // whole number values
            c = this.next();
            while((c >= '0') && (c <= '9')) {
                sb.append(c);
                c = this.next();
            }
        } else {
            throw this.syntaxError("Expected number");
        }

        if(c == '.') {
            dbl = true;
            sb.append(c);

            // decimal place values
            c = this.next();
            while((c >= '0') && (c <= '9')) {
                sb.append(c);
                c = this.next();
            }
        }
        if((c == 'e') || (c == 'E')) {
            dbl = true;
            sb.append(c);

            // exponent values
            c = this.next();
            if((c == '+') || (c == '-')) {
                sb.append(c);
                c = this.next();
            }
            if((c >= '0') && (c <= '9')) {
                sb.append(c);
                c = this.next();
            } else {
                throw this.syntaxError("Expected exponent value");
            }
            while((c >= '0') && (c <= '9')) {
                sb.append(c);
                c = this.next();
            }
        }
        this.back();

        try {
            if (dbl) {
                Double d = Double.valueOf(sb.toString());
                if ((!d.isInfinite()) && (!d.isNaN())) {
                    return d;
                }
            } else {
                Long myLong = Long.valueOf(sb.toString());
                if (myLong.longValue() == myLong.intValue()) {
                    return Integer.valueOf(myLong.intValue());
                } else {
                    return myLong;
                }
            }
        }  catch (Exception ignore) {
            // fall through
        }
        throw this.syntaxError("Could not parse number");
    }

    // --- Static factory methods

    /**
     * Construct a JSONObject from a Reader.
     *
     * @param reader the source reader
     * @return a JSONObject containing the parsed data
     * @throws JSONException there was a parse error
     */
    public static JSONObject parseJSONObject(Reader reader) throws JSONException {
        JSONStrictTokener tokener = new JSONStrictTokener(reader);
        return tokener.nextJSONObject();
    }

    /**
     * Construct a JSONObject from an InputStream and supplied Charset.
     *
     * @param inputStream the input stream containing the byte sequences to parse
     * @param charset the character set for interpreting the byte sequence
     * @return a JSONObject containing the parsed data
     * @throws JSONException there was a parse error
     */
    public static JSONObject parseJSONObject(InputStream inputStream, Charset charset) {
        JSONStrictTokener tokener = new JSONStrictTokener(inputStream, charset);
        return tokener.nextJSONObject();
    }

    /**
     * Construct a JSONObject from a string.
     *
     * @param s     A source string.
     * @return a JSONObject containing the parsed data
     * @throws JSONException there was a parse error
     */
    public static JSONObject parseJSONObject(String s) {
        JSONStrictTokener tokener = new JSONStrictTokener(s);
        return tokener.nextJSONObject();
    }

    /**
     * Construct a JSONArray from a Reader.
     *
     * @param reader the source reader
     * @return a JSONArray containing the parsed data
     * @throws JSONException there was a parse error
     */
    public static JSONArray parseJSONArray(Reader reader) throws JSONException {
        JSONStrictTokener tokener = new JSONStrictTokener(reader);
        return tokener.nextJSONArray();
    }

    /**
     * Construct a JSONArray from an InputStream and supplied Charset.
     *
     * @param inputStream the input stream containing the byte sequences to parse
     * @param charset the character set for interpreting the byte sequence
     * @return a JSONArray containing the parsed data
     * @throws JSONException there was a parse error
     */
    public static JSONArray parseJSONArray(InputStream inputStream, Charset charset) throws JSONException {
        JSONStrictTokener tokener = new JSONStrictTokener(inputStream, charset);
        return tokener.nextJSONArray();
    }

    /**
     * Construct a JSONArray from a string.
     *
     * @param s     A source string.
     * @return a JSONArray containing the parsed data
     * @throws JSONException there was a parse error
     */
    public static JSONArray parseJSONArray(String s) throws JSONException {
        JSONStrictTokener tokener = new JSONStrictTokener(s);
        return tokener.nextJSONArray();
    }

    /**
     * Construct a JSON value from a Reader.
     *
     * @param reader the source reader
     * @return an Object containing the parsed data
     * @throws JSONException there was a parse error
     */
    public static Object parseJSONValue(Reader reader) throws JSONException {
        JSONStrictTokener tokener = new JSONStrictTokener(reader);
        return tokener.nextValue();
    }

    /**
     * Construct a JSON value from an InputStream and supplied Charset.
     *
     * @param inputStream the input stream containing the byte sequences to parse
     * @param charset the character set for interpreting the byte sequence
     * @return an Object containing the parsed data
     * @throws JSONException there was a parse error
     */
    public static Object parseJSONValue(InputStream inputStream, Charset charset) throws JSONException {
        JSONStrictTokener tokener = new JSONStrictTokener(inputStream, charset);
        return tokener.nextValue();
    }

    /**
     * Construct a JSON value from a string.
     *
     * @param s     A source string.
     * @return an Object containing the parsed data
     * @throws JSONException there was a parse error
     */
    public static Object parseJSONValue(String s) throws JSONException {
        JSONStrictTokener tokener = new JSONStrictTokener(s);
        return tokener.nextValue();
    }
}
