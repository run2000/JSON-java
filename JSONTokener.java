package org.json;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

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
 * A JSONTokener takes a source string and extracts characters and tokens from
 * it. It is used by the JSONObject and JSONArray constructors to parse
 * JSON source strings.
 * <p>
 * This class performed a lenient parse. For a strict parse, use
 * {@link JSONStrictTokener}.
 * </p>
 * @author JSON.org
 * @version 2012-02-16
 */
public class JSONTokener extends Scanner {

    /**
     * The maximum number of keys in the key pool.
     */
    private static final int keyPoolSize = 100;

    /**
     * Key pooling is like string interning, but without permanently tying up
     * memory. To help conserve memory, storage of duplicated key strings in
     * JSONObjects will be avoided by using a key pool to manage unique key
     * string objects. This is used by {@link #nextKey()}.
     */
    private final Map<String, String> keyPool = new LRUHashMap<String, String>(keyPoolSize);

    /**
     * Terminating symbols for an unquoted key or value.
     */
    private static final String UNQUOTED_DELIMITERS = ",:]}/\\\"[{;=#";

    /**
     * Construct a JSONTokener from a Reader.
     *
     * @param reader     A reader.
     */
    public JSONTokener(Reader reader) {
        super(reader);
    }


    /**
     * Construct a JSONTokener from an InputStream, using the default
     * character set.
     */
    public JSONTokener(InputStream inputStream) {
        super(new InputStreamReader(inputStream));
    }

    /**
     * Construct a JSONTokener from an InputStream and supplied Charset.
     */
    public JSONTokener(InputStream inputStream, Charset charset) {
        super(inputStream, charset);
    }

    /**
     * Construct a JSONTokener from a string.
     *
     * @param s     A source string.
     */
    public JSONTokener(String s) {
        super(new StringReader(s));
    }


    /**
     * Get the next char in the string, skipping whitespace.
     * @throws JSONException Thrown if there is an error reading the source string.
     * @return  A character, or 0 if there are no more characters.
     */
    public char nextClean() throws JSONException {
        for (;;) {
            char c = this.next();
            if ((c > ' ') || ((c == 0) && (this.end()))) {
                return c;
            }
        }
    }

    /**
     * Get the type of the next token in the stream, skipping whitespace.
     *
     * @return the Token type representing the next token type
     * @see JSONToken
     */
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
            case ';':
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
     * Backslash processing is done. The formal JSON format does not
     * allow strings in single quotes, but an implementation is allowed to
     * accept them.
     * @param quote The quoting character, either
     *      <code>"</code>&nbsp;<small>(double quote)</small> or
     *      <code>'</code>&nbsp;<small>(single quote)</small>.
     * @return      A String.
     * @throws JSONException Unterminated string.
     */
    public String nextString(char quote) throws JSONException {
        char c;
        StringBuilder sb = new StringBuilder();

        if((quote != '"') && (quote != '\'')) {
            throw new JSONException("Unexpected string delimiter");
        }
        for (;;) {
            c = this.next();
            switch (c) {
            case 0:
            case '\n':
            case '\r':
                throw this.syntaxError("Unterminated string");
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
                        throw this.syntaxError("Illegal escape.", e);
                    }
                    break;
                case '"':
                case '\'':
                case '\\':
                case '/':
                    sb.append(c);
                    break;
                default:
                    throw this.syntaxError("Illegal escape.");
                }
                break;
            case '"':
            case '\'':
                if (c == quote) {
                    return sb.toString();
                } else {
                    sb.append(c);
                    break;
                }
            default:
                sb.append(c);
            }
        }
    }


    /**
     * Get the next key. This would usually be a quoted String value.
     * Unquoted forms are also allowed.
     *
     * @throws JSONException If syntax error.
     *
     * @return An object.
     */
    public String nextKey() throws JSONException {
        char c = this.nextClean();
        String string;

        switch (c) {
            case '"':
            case '\'':
                return pooledKey(this.nextString(c));
            case '{':
                this.back();
                throw this.syntaxError("Expected key, found object");
            case '[':
                this.back();
                throw this.syntaxError("Expected key, found array");
        }

        /*
         * Handle unquoted text. This could be the values true, false, or
         * null, or it can be a number. An implementation (such as this one)
         * is allowed to also accept non-standard forms.
         *
         * Accumulate characters until we reach the end of the text or a
         * formatting character.
         */

        StringBuilder sb = new StringBuilder();
        while ((c >= ' ') && (UNQUOTED_DELIMITERS.indexOf(c) < 0)) {
            sb.append(c);
            c = this.next();
        }
        this.back();

        string = sb.toString().trim();
        if (string.length() == 0) {
            throw this.syntaxError("Missing value");
        }
        return pooledKey(string);
    }

    /**
     * Add the given key to the key pool, if necessary, and return the
     * canonical key.
     *
     * @param key the key to be pooled
     * @return the canonical key
     */
    protected String pooledKey(String key) {
        String pooled = keyPool.get(key);
        if (pooled == null) {
            // canonical form, if required, eg. new String(key)
            pooled = key;
            keyPool.put(pooled, pooled);
        }
        return pooled;
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
        String string;

        switch (c) {
            case '"':
            case '\'':
                return this.nextString(c);
            case '{':
                this.back();
                return new JSONObject(this);
            case '[':
                this.back();
                return new JSONArray(this);
        }

        /*
         * Handle unquoted text. This could be the values true, false, or
         * null, or it can be a number. An implementation (such as this one)
         * is allowed to also accept non-standard forms.
         *
         * Accumulate characters until we reach the end of the text or a
         * formatting character.
         */

        StringBuilder sb = new StringBuilder();
        while (c >= ' ' && UNQUOTED_DELIMITERS.indexOf(c) < 0) {
            sb.append(c);
            c = this.next();
        }
        this.back();

        string = sb.toString().trim();
        if ("".equals(string)) {
            throw this.syntaxError("Missing value");
        }
        return JSONObject.stringToValue(string);
    }

    /**
     * Make a printable string of this Scanner.
     *
     * @return "JSONTokener at {index} [character {character} line {line}]"
     */
    @Override
    public String toString() {
        return "JSONTokener" + super.toString();
    }

    /**
     * Subclass of LinkedHashMap that acts as a simple LRU cache.
     */
    private static final class LRUHashMap<K,V> extends LinkedHashMap<K,V> {
        private final int maxCapacity;

        LRUHashMap(int maxCapacity) {
            super(16, 0.8f, true);
            this.maxCapacity = maxCapacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
            return size() > maxCapacity;
        }
    }

    /**
     * Tokens that can be identified with at most one character lookahead.
     * Produced by {@link JSONTokener} and consumed by related JSON objects
     * to allow strict versus lenient parsing.
     *
     * @author JSON.org
     * @version 2016-06-08
     */
    public enum JSONToken {
        VALUE,
        START_ARRAY,
        END_ARRAY,
        START_OBJECT,
        END_OBJECT,
        KEY_SEPARATOR,
        VALUE_SEPARATOR,
        END
    }
}
