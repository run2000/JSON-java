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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.stream.JSONStreamReader.ParseState;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Builds a JSON object model using events supplied from {@link JSONStreamReader}.
 * This uses less stack space than parsing via {@link org.json.JSONTokener},
 * since only one stack frame is allocated per nested object or array.
 *
 * @author JSON.org
 * @version 2016-6-30
 */
public final class JSONObjectBuilder {

    private JSONObjectBuilder() {
    }

    /**
     * Build a JSON value from a {@code Reader}. The value may be one of:
     * <ul>
     *     <li>{@code JSONObject.NULL}</li>
     *     <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     *     <li>A {@code Double}, {@code Long}, or {@code Integer}</li>
     *     <li>A {@code String}</li>
     *     <li>A {@code JSONObject}</li>
     *     <li>A {@code JSONArray}</li>
     * </ul>
     *
     * @param reader     A reader.
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(Reader reader) throws JSONException {
        return buildJSONValue(new JSONStreamReader(reader));
    }

    /**
     * Build a JSON value from a {@code InputStream} and supplied
     * {@code Charset}. The value may be one of:
     * <ul>
     *     <li>{@code JSONObject.NULL}</li>
     *     <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     *     <li>A {@code Double}, {@code Long}, or {@code Integer}</li>
     *     <li>A {@code String}</li>
     *     <li>A {@code JSONObject}</li>
     *     <li>A {@code JSONArray}</li>
     * </ul>
     *
     * @param inputStream   the input stream containing the JSON data
     * @param charset       the character set with which to interpret the
     *                      input stream
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(InputStream inputStream, Charset charset)
            throws JSONException {
        return buildJSONValue(new JSONStreamReader(inputStream, charset));
    }

    /**
     * Build a JSON value from a {@code String}. The value may be one of:
     * <ul>
     *     <li>{@code JSONObject.NULL}</li>
     *     <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     *     <li>A {@code Double}, {@code Long}, or {@code Integer}</li>
     *     <li>A {@code String}</li>
     *     <li>A {@code JSONObject}</li>
     *     <li>A {@code JSONArray}</li>
     * </ul>
     *
     * @param s     A source string.
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(String s) throws JSONException {
        return buildJSONValue(new JSONStreamReader(s));
    }

    /**
     * Build a JSON value from a {@code JSONStreamReader}. The value may be one
     * of:
     * <ul>
     *     <li>{@code JSONObject.NULL}</li>
     *     <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     *     <li>A {@code Double}, {@code Long}, or {@code Integer}</li>
     *     <li>A {@code String}</li>
     *     <li>A {@code JSONObject}</li>
     *     <li>A {@code JSONArray}</li>
     * </ul>
     *
     * @param parser    A source stream.
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(JSONStreamReader parser) throws JSONException {
        ParseState state = parser.nextState();

        if(state != ParseState.DOCUMENT) {
            throw parser.syntaxError("JSON parser should be at the beginning");
        }

        Object result;
        state = parser.nextState();

        switch(state) {
            case OBJECT:
                result = parseObject(parser);
                break;

            case ARRAY:
                result = parseArray(parser);
                break;

            case VALUE:
                result = parser.nextValue();
                break;

            default:
                throw parser.syntaxError("Expected value");
        }

        if(parser.nextState() != ParseState.END_DOCUMENT) {
            throw parser.syntaxError("JSON parser in an unexpected state");
        }

        return result;
    }

    /**
     * Build a JSONObject from a {@code Reader}.
     *
     * @param reader     A reader.
     * @return a JSONObject value
     */
    public static JSONObject buildJSONObject(Reader reader) throws JSONException {
        return buildJSONObject(new JSONStreamReader(reader));
    }

    /**
     * Build a JSONObject from a {@code InputStream} and supplied
     * {@code Charset}.
     *
     * @param inputStream   the input stream containing the JSON data
     * @param charset       the character set with which to interpret the
     *                      input stream
     * @return a JSONObject value
     */
    public static JSONObject buildJSONObject(InputStream inputStream, Charset charset)
            throws JSONException {
        return buildJSONObject(new JSONStreamReader(inputStream, charset));
    }

    /**
     * Build a JSONObject from a {@code String}.
     *
     * @param s     A source string.
     * @return a JSONObject value
     */
    public static JSONObject buildJSONObject(String s) throws JSONException {
        return buildJSONObject(new JSONStreamReader(s));
    }

    /**
     * Build a JSONObject from a {@code JSONStreamReader}.
     *
     * @param parser    A source stream.
     * @return a JSONObject value
     */
    public static JSONObject buildJSONObject(JSONStreamReader parser) throws JSONException {
        ParseState state = parser.nextState();

        if(state != ParseState.DOCUMENT) {
            throw parser.syntaxError("JSON parser should be at the beginning");
        }

        JSONObject result;
        state = parser.nextState();

        switch(state) {
            case OBJECT:
                result = parseObject(parser);
                break;

            default:
                throw parser.syntaxError("Expected object");
        }

        if(parser.nextState() != ParseState.END_DOCUMENT) {
            throw parser.syntaxError("JSON parser in an unexpected state");
        }

        return result;
    }

    /**
     * Build a JSONArray from a {@code Reader}.
     *
     * @param reader     A reader.
     * @return a JSONArray value
     */
    public static JSONArray buildJSONArray(Reader reader) throws JSONException {
        return buildJSONArray(new JSONStreamReader(reader));
    }

    /**
     * Build a JSONArray from a {@code InputStream} and supplied
     * {@code Charset}.
     *
     * @param inputStream   the input stream containing the JSON data
     * @param charset       the character set with which to interpret the
     *                      input stream
     * @return a JSONArray value
     */
    public static JSONArray buildJSONArray(InputStream inputStream, Charset charset)
            throws JSONException {
        return buildJSONArray(new JSONStreamReader(inputStream, charset));
    }

    /**
     * Build a JSONArray from a {@code String}.
     *
     * @param s     A source string.
     * @return a JSONArray value
     */
    public static JSONArray buildJSONArray(String s) throws JSONException {
        return buildJSONArray(new JSONStreamReader(s));
    }

    /**
     * Build a JSONArray from a {@code JSONStreamReader}.
     *
     * @param parser    A source stream.
     * @return a JSONArray value
     */
    public static JSONArray buildJSONArray(JSONStreamReader parser) throws JSONException {
        ParseState state = parser.nextState();

        if(state != ParseState.DOCUMENT) {
            throw parser.syntaxError("JSON parser should be at the beginning");
        }

        JSONArray result;
        state = parser.nextState();

        switch(state) {
            case ARRAY:
                result = parseArray(parser);
                break;

            default:
                throw parser.syntaxError("Expected array");
        }

        if(parser.nextState() != ParseState.END_DOCUMENT) {
            throw parser.syntaxError("JSON parser in an unexpected state");
        }

        return result;
    }

    private static JSONArray parseArray(JSONStreamReader parser) throws JSONException {
        JSONArray array = new JSONArray();
        ParseState state = parser.nextState();
        Object value;

        while (state != ParseState.END_ARRAY) {
            switch(state) {
                case VALUE:
                    value = parser.nextValue();
                    break;
                case ARRAY:
                    value = parseArray(parser);
                    break;
                case OBJECT:
                    value = parseObject(parser);
                    break;
                default:
                    throw parser.syntaxError("Expected value");
            }
            array.put(value);
            state = parser.nextState();
        }
        return array;
    }

    private static JSONObject parseObject(JSONStreamReader parser) throws JSONException {
        JSONObject object = new JSONObject();
        ParseState state = parser.nextState();
        String key;
        Object value;

        while (state != ParseState.END_OBJECT) {
            if(state == ParseState.KEY) {
                key = parser.nextKey();
            } else {
                throw parser.syntaxError("Expected key");
            }

            state = parser.nextState();
            switch(state) {
                case VALUE:
                    value = parser.nextValue();
                    break;
                case ARRAY:
                    value = parseArray(parser);
                    break;
                case OBJECT:
                    value = parseObject(parser);
                    break;
                default:
                    throw parser.syntaxError("Expected value");
            }

            object.putOnce(key, value);
            state = parser.nextState();
        }
        return object;
    }
}