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
import org.json.JSONParseException;
import org.json.stream.JSONStreamReader.ParseState;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Builds a JSON object model using events supplied from {@link JSONStreamReader}.
 * This uses less stack space than parsing via {@link org.json.JSONTokener},
 * since only one stack frame is allocated per nested object or array.
 * <p>
 * A simple example of how to use this class:</p>
 * <pre>
 * JSONObject jsonObject = JSONObjectBuilder.buildJSONObject(JSON_TEXT);
 * </pre>
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
     *     <li>A {@code Double}, {@code Long}, {@code Integer},
     *         {@code BigDecimal}, or {@code BigInteger}</li>
     *     <li>A {@code String}</li>
     *     <li>A {@code JSONObject}</li>
     *     <li>A {@code JSONArray}</li>
     * </ul>
     *
     * @param reader     A reader.
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(Reader reader) throws JSONException {
        return buildJSONValue(new JSONStreamReader(reader), JSONCollector.INSTANCE);
    }

    /**
     * Build a JSON value from a {@code Reader}. The value may be one of:
     * <ul>
     * <li>A null value, as defined by the collector</li>
     * <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     * <li>A {@code Double}, {@code Long}, {@code Integer},
     *     {@code BigDecimal}, or {@code BigInteger}</li>
     * <li>A {@code String}</li>
     * <li>A JSON Object, as specified by the collector result type</li>
     * <li>A JSON Array, as specified by the collector result type</li>
     * </ul>
     *
     * @param reader     A reader.
     * @param collector  A collector object for creating structures
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(Reader reader,
            StructureCollector<?, ?, ?, ?> collector) throws JSONException {
        return buildJSONValue(new JSONStreamReader(reader), collector);
    }

    /**
     * Build a JSON value from a {@code InputStream} and supplied
     * {@code Charset}. The value may be one of:
     * <ul>
     *     <li>{@code JSONObject.NULL}</li>
     *     <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     *     <li>A {@code Double}, {@code Long}, {@code Integer},
     *         {@code BigDecimal}, or {@code BigInteger}</li>
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
        return buildJSONValue(new JSONStreamReader(inputStream, charset), JSONCollector.INSTANCE);
    }

    /**
     * Build a JSON value from a {@code InputStream} and supplied
     * {@code Charset}. The value may be one of:
     * <ul>
     * <li>A null value, as defined by the collector</li>
     * <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     * <li>A {@code Double}, {@code Long}, {@code Integer},
     *     {@code BigDecimal}, or {@code BigInteger}</li>
     * <li>A {@code String}</li>
     * <li>A JSON Object, as specified by the collector result type</li>
     * <li>A JSON Array, as specified by the collector result type</li>
     * </ul>
     *
     * @param inputStream   the input stream containing the JSON data
     * @param charset       the character set with which to interpret the
     *                      input stream
     * @param collector     A collector object for creating structures
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(InputStream inputStream, Charset charset,
            StructureCollector<?, ?, ?, ?> collector) throws JSONException {
        return buildJSONValue(new JSONStreamReader(inputStream, charset), collector);
    }

    /**
     * Build a JSON value from a {@code String}. The value may be one of:
     * <ul>
     *     <li>{@code JSONObject.NULL}</li>
     *     <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     *     <li>A {@code Double}, {@code Long}, {@code Integer},
     *         {@code BigDecimal}, or {@code BigInteger}</li>
     *     <li>A {@code String}</li>
     *     <li>A {@code JSONObject}</li>
     *     <li>A {@code JSONArray}</li>
     * </ul>
     *
     * @param s     A source string.
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(String s) throws JSONException {
        return buildJSONValue(new JSONStreamReader(s), JSONCollector.INSTANCE);
    }

    /**
     * Build a JSON value from a {@code String}. The value may be one of:
     * <ul>
     * <li>A null value, as defined by the collector</li>
     * <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     * <li>A {@code Double}, {@code Long}, {@code Integer},
     *     {@code BigDecimal}, or {@code BigInteger}</li>
     * <li>A {@code String}</li>
     * <li>A JSON Object, as specified by the collector result type</li>
     * <li>A JSON Array, as specified by the collector result type</li>
     * </ul>
     *
     * @param s     A source string.
     * @param collector A collector object for creating structures
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(String s,
            StructureCollector<?, ?, ?, ?> collector) throws JSONException {
        return buildJSONValue(new JSONStreamReader(s), collector);
    }

    /**
     * Build a JSON value from a {@code JSONStreamReader}. The value may be one
     * of:
     * <ul>
     *     <li>{@code JSONObject.NULL}</li>
     *     <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     *     <li>A {@code Double}, {@code Long}, {@code Integer},
     *         {@code BigDecimal}, or {@code BigInteger}</li>
     *     <li>A {@code String}</li>
     *     <li>A {@code JSONObject}</li>
     *     <li>A {@code JSONArray}</li>
     * </ul>
     * <p> The reader must be at the beginning of the document.</p>
     *
     * @param reader    A source stream reader.
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(JSONStreamReader reader) throws JSONException {
        return buildJSONValue(reader, JSONCollector.INSTANCE);
    }

    /**
     * Build a JSON value from a {@code JSONStreamReader}. The value may be one
     * of:
     * <ul>
     * <li>A null value, as defined by the collector</li>
     * <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     * <li>A {@code Double}, {@code Long}, {@code Integer},
     *     {@code BigDecimal}, or {@code BigInteger}</li>
     * <li>A {@code String}</li>
     * <li>A JSON Object, as specified by the collector result type</li>
     * <li>A JSON Array, as specified by the collector result type</li>
     * </ul>
     * <p> The reader must be at the beginning of the document.</p>
     *
     * @param reader    A source stream reader.
     * @param collector A collector object for creating structures
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(JSONStreamReader reader,
            StructureCollector<?, ?, ?, ?> collector) throws JSONException {
        ParseState state = reader.nextState();

        if(state != ParseState.DOCUMENT) {
            throw new JSONParseException("JSON parser should be at the beginning",
                    reader.getParsePosition());
        }

        Object result;
        state = reader.nextState();

        switch(state) {
            case OBJECT:
                result = parseObjectTree(reader, collector);
                break;

            case ARRAY:
                result = parseArrayTree(reader, collector);
                break;

            case NULL_VALUE:
                result = collector.nullValue();
                break;

            case BOOLEAN_VALUE:
            case NUMBER_VALUE:
            case STRING_VALUE:
                result = reader.nextValue();
                break;

            default:
                throw new JSONParseException("Expected value", reader.getParsePosition());
        }

        if(reader.nextState() != ParseState.END_DOCUMENT) {
            throw new JSONParseException("JSON parser in an unexpected state",
                    reader.getParsePosition());
        }

        return result;
    }

    /**
     * Build a {@code JSONObject} from a {@code Reader}.
     *
     * @param reader     A reader.
     * @return a JSONObject value
     */
    public static JSONObject buildJSONObject(Reader reader) throws JSONException {
        return buildJSONObject(new JSONStreamReader(reader), JSONCollector.INSTANCE);
    }

    /**
     * Build a {@code JSONObject} from a {@code Reader}.
     *
     * @param reader     A reader.
     * @param collector  A collector object for creating structures
     * @param <OR>       The resulting JSON object type
     * @return the parsed JSON object value
     */
    public static <OR> OR buildJSONObject(Reader reader,
            StructureCollector<?, ?, OR, ?> collector) throws JSONException {
        return buildJSONObject(new JSONStreamReader(reader), collector);
    }

    /**
     * Build a {@code JSONObject} from a {@code InputStream} and a supplied
     * {@code Charset}.
     *
     * @param inputStream   the input stream containing the JSON data
     * @param charset       the character set with which to interpret the
     *                      input stream
     * @return a JSONObject value
     */
    public static JSONObject buildJSONObject(InputStream inputStream, Charset charset)
            throws JSONException {
        return buildJSONObject(new JSONStreamReader(inputStream, charset), JSONCollector.INSTANCE);
    }

    /**
     * Build a {@code JSONObject} from a {@code InputStream} and a supplied
     * {@code Charset}.
     *
     * @param inputStream   the input stream containing the JSON data
     * @param charset       the character set with which to interpret the
     *                      input stream
     * @param collector     A collector object for creating structures
     * @param <OR>          The resulting JSON object type
     * @return the parsed JSON object value
     */
    public static <OR> OR buildJSONObject(InputStream inputStream, Charset charset,
            StructureCollector<?, ?, OR, ?> collector) throws JSONException {
        return buildJSONObject(new JSONStreamReader(inputStream, charset), collector);
    }

    /**
     * Build a {@code JSONObject} from a {@code String}.
     *
     * @param s     A source string.
     * @return a JSONObject value
     */
    public static JSONObject buildJSONObject(String s) throws JSONException {
        return buildJSONObject(new JSONStreamReader(s), JSONCollector.INSTANCE);
    }

    /**
     * Build a {@code JSONObject} from a {@code String}.
     *
     * @param s         A source string.
     * @param collector A collector object for creating structures
     * @param <OR>      The resulting JSON object type
     * @return the parsed JSON object value
     */
    public static <OR> OR buildJSONObject(String s,
            StructureCollector<?, ?, OR, ?> collector) throws JSONException {
        return buildJSONObject(new JSONStreamReader(s), collector);
    }

    /**
     * Build a {@code JSONObject} from a {@code JSONStreamReader}. The reader must be
     * at the beginning of the document.
     *
     * @param reader    A source stream reader.
     * @return a JSONObject value
     */
    public static JSONObject buildJSONObject(JSONStreamReader reader)
            throws JSONException {
        return buildJSONObject(reader, JSONCollector.INSTANCE);
    }
    /**
     * Build a {@code JSONObject} from a {@code JSONStreamReader}. The reader must be
     * at the beginning of the document.
     *
     * @param reader    A source stream reader.
     * @param collector A collector object for creating structures
     * @param <OR>      The resulting JSON object type
     * @return the parsed JSON object value
     */
    public static <OR> OR buildJSONObject(JSONStreamReader reader,
            StructureCollector<?, ?, OR, ?> collector) throws JSONException {

        if(collector == null) {
            throw new NullPointerException("collector is null");
        }
        ParseState state = reader.nextState();

        if(state != ParseState.DOCUMENT) {
            throw new JSONParseException("JSON parser should be at the beginning",
                    reader.getParsePosition());
        }

        OR result;
        state = reader.nextState();

        switch(state) {
            case OBJECT:
                result = parseObjectTree(reader, collector);
                break;

            default:
                throw new JSONParseException("Expected object", reader.getParsePosition());
        }

        if(reader.nextState() != ParseState.END_DOCUMENT) {
            throw new JSONParseException("JSON parser in an unexpected state",
                    reader.getParsePosition());
        }

        return result;
    }

    /**
     * Build a {@code JSONArray} from a {@code Reader}.
     *
     * @param reader     A reader.
     * @param collector  A collector object for creating structures
     * @param <AR>       The resulting JSON array type
     * @return the parsed JSON array value
     */
    public static <AR> AR buildJSONArray(Reader reader,
            StructureCollector<?, ?, ?, AR> collector) throws JSONException {
        return buildJSONArray(new JSONStreamReader(reader), collector);
    }

    /**
     * Build a {@code JSONArray} from a {@code Reader}.
     *
     * @param reader     A reader.
     * @return a JSONArray value
     */
    public static JSONArray buildJSONArray(Reader reader) throws JSONException {
        return buildJSONArray(new JSONStreamReader(reader), JSONCollector.INSTANCE);
    }

    /**
     * Build a {@code JSONArray} from a {@code InputStream} and a supplied
     * {@code Charset}.
     *
     * @param inputStream   the input stream containing the JSON data
     * @param charset       the character set with which to interpret the
     *                      input stream
     * @return a JSONArray value
     */
    public static JSONArray buildJSONArray(InputStream inputStream, Charset charset)
            throws JSONException {
        return buildJSONArray(new JSONStreamReader(inputStream, charset), JSONCollector.INSTANCE);
    }

    /**
     * Build a {@code JSONArray} from a {@code InputStream} and a supplied
     * {@code Charset}.
     *
     * @param inputStream   the input stream containing the JSON data
     * @param charset       the character set with which to interpret the
     *                      input stream
     * @param collector     A collector object for creating structures
     * @param <AR>          The resulting JSON array type
     * @return the parsed JSON array value
     */
    public static <AR> AR buildJSONArray(InputStream inputStream,
            Charset charset, StructureCollector<?, ?, ?, AR> collector)
            throws JSONException {
        return buildJSONArray(new JSONStreamReader(inputStream, charset), collector);
    }

    /**
     * Build a {@code JSONArray} from a {@code String}.
     *
     * @param s     A source string.
     * @return a JSONArray value
     */
    public static JSONArray buildJSONArray(String s) throws JSONException {
        return buildJSONArray(new JSONStreamReader(s), JSONCollector.INSTANCE);
    }

    /**
     * Build a {@code JSONArray} from a {@code String}.
     *
     * @param s     A source string.
     * @param collector A collector object for creating structures
     * @param <AR>      The resulting JSON array type
     * @return the parsed JSON array value
     */
    public static <AR> AR buildJSONArray(String s,
            StructureCollector<?, ?, ?, AR> collector) throws JSONException {
        return buildJSONArray(new JSONStreamReader(s), collector);
    }

    /**
     * Build a {@code JSONArray} from a {@code JSONStreamReader}. The reader must be
     * at the beginning of the document.
     *
     * @param reader    A source stream.
     * @return a JSONArray value
     */
    public static JSONArray buildJSONArray(JSONStreamReader reader) throws JSONException {
        return buildJSONArray(reader, JSONCollector.INSTANCE);
    }

    /**
     * Build a {@code JSONArray} from a {@code JSONStreamReader}. The reader must be
     * at the beginning of the document.
     *
     * @param reader    A source stream.
     * @param collector A collector object for creating structures
     * @param <AR>      The resulting JSON array type
     * @return the parsed JSON array value
     */
    public static <AR> AR buildJSONArray(JSONStreamReader reader,
            StructureCollector<?, ?, ?, AR> collector) throws JSONException {

        if(collector == null) {
            throw new NullPointerException("collector is null");
        }
        ParseState state = reader.nextState();

        if(state != ParseState.DOCUMENT) {
            throw new JSONParseException("JSON parser should be at the beginning",
                    reader.getParsePosition());
        }

        AR result;
        state = reader.nextState();

        switch(state) {
            case ARRAY:
                result = parseArrayTree(reader, collector);
                break;

            default:
                throw new JSONParseException("Expected array", reader.getParsePosition());
        }

        if(reader.nextState() != ParseState.END_DOCUMENT) {
            throw new JSONParseException("JSON parser in an unexpected state",
                    reader.getParsePosition());
        }

        return result;
    }

    private static <AA, AR> AR parseArrayTree(JSONStreamReader reader,
             StructureCollector<?, AA, ?, AR> collector) throws JSONException {
        AA array = collector.createArrayAccumulator();
        ParseState state = reader.nextState();
        Object value;

        while (state != ParseState.END_ARRAY) {
            switch(state) {
                case NULL_VALUE:
                    collector.addNull(array);
                    break;
                case BOOLEAN_VALUE:
                case NUMBER_VALUE:
                case STRING_VALUE:
                    value = reader.nextValue();
                    collector.addValue(array, value);
                    break;
                case ARRAY:
                    value = parseArrayTree(reader, collector);
                    collector.addValue(array, value);
                    break;
                case OBJECT:
                    value = parseObjectTree(reader, collector);
                    collector.addValue(array, value);
                    break;
                default:
                    throw new JSONParseException("Expected value", reader.getParsePosition());
            }
            state = reader.nextState();
        }
        return collector.finishArray(array);
    }

    private static <OA, OR> OR parseObjectTree(JSONStreamReader reader,
            StructureCollector<OA, ?, OR, ?> collector) throws JSONException {
        OA object = collector.createObjectAccumulator();
        ParseState state = reader.nextState();
        String key;
        Object value;

        while (state != ParseState.END_OBJECT) {
            if(state == ParseState.KEY) {
                key = reader.nextKey();
            } else {
                throw new JSONParseException("Expected key", reader.getParsePosition());
            }

            state = reader.nextState();
            switch(state) {
                case NULL_VALUE:
                    collector.addNull(object, key);
                    break;
                case BOOLEAN_VALUE:
                case NUMBER_VALUE:
                case STRING_VALUE:
                    value = reader.nextValue();
                    collector.addValue(object, key, value);
                    break;
                case ARRAY:
                    value = parseArrayTree(reader, collector);
                    collector.addValue(object, key, value);
                    break;
                case OBJECT:
                    value = parseObjectTree(reader, collector);
                    collector.addValue(object, key, value);
                    break;
                default:
                    throw new JSONParseException("Expected value", reader.getParsePosition());
            }

            state = reader.nextState();
        }
        return collector.finishObject(object);
    }

    /**
     * If the given JSONStreamReader's ParseState was {@link ParseState#OBJECT},
     * return the entire subtree as a JSONObject value. This method advances the
     * parser onto the {@link ParseState#END_OBJECT} state.
     * <p>
     * If the JSON stream is not parseable as an object, a JSONException
     * will be thrown.
     * </p>
     *
     * @param reader A source stream reader.
     * @return a JSONObject representing the subtree starting at the current
     * OBJECT state
     */
    public static JSONObject buildObjectSubTree(JSONStreamReader reader) throws JSONException {
        if((reader.currentState() != ParseState.OBJECT) || (reader.getStackDepth() == 0)) {
            throw new JSONParseException("Expected OBJECT state", reader.getParsePosition());
        }
        return parseObjectTree(reader, JSONCollector.INSTANCE);
    }

    /**
     * If the given JSONStreamReader's ParseState was {@link ParseState#OBJECT},
     * return the entire subtree as an object type, as specified by the given
     * collector. This method advances the parser onto the
     * {@link ParseState#END_OBJECT} state.
     * <p>
     * If the JSON stream is not parseable as an object, a JSONException
     * will be thrown.
     * </p>
     *
     * @param reader A source stream reader.
     * @param collector A collector object for creating structures
     * @param <OR> The resulting JSON object type
     * @return a JSON object representing the subtree starting at the current
     * OBJECT state
     */
    public static <OR> OR buildObjectSubTree(JSONStreamReader reader,
            StructureCollector<?, ?, OR, ?> collector) throws JSONException {
        if((reader.currentState() != ParseState.OBJECT) || (reader.getStackDepth() == 0)) {
            throw new JSONParseException("Expected OBJECT state", reader.getParsePosition());
        }
        return parseObjectTree(reader, collector);
    }

    /**
     * If the given JSONStreamReader's ParseState was {@link ParseState#ARRAY},
     * return the entire subtree as a JSONArray value. This method advances the
     * parser onto the {@link ParseState#END_ARRAY} state.
     * <p>
     * If the JSON stream is not parseable as an array, a JSONException
     * will be thrown.
     * </p>
     *
     * @param reader A source stream reader.
     * @return a JSONArray representing the subtree starting at the current
     * ARRAY state
     */
    public static JSONArray buildArraySubTree(JSONStreamReader reader) throws JSONException {
        if((reader.currentState() != ParseState.ARRAY) || (reader.getStackDepth() == 0)) {
            throw new JSONParseException("Expected ARRAY state", reader.getParsePosition());
        }
        return parseArrayTree(reader, JSONCollector.INSTANCE);
    }

    /**
     * If the given JSONStreamReader's ParseState was {@link ParseState#ARRAY},
     * return the entire subtree as an array type, as specified by the given
     * collector. This method advances the parser onto the
     * {@link ParseState#END_ARRAY} state.
     * <p>
     * If the JSON stream is not parseable as an array, a JSONException
     * will be thrown.
     * </p>
     *
     * @param reader A source stream reader.
     * @param collector A collector object for creating structures
     * @param <AR>      The resulting JSON array type
     * @return the JSON array representing the subtree starting at the current
     * ARRAY state
     */
    public static <AR> AR buildArraySubTree(JSONStreamReader reader,
            StructureCollector<?, ?, ?, AR> collector) throws JSONException {
        if((reader.currentState() != ParseState.ARRAY) || (reader.getStackDepth() == 0)) {
            throw new JSONParseException("Expected ARRAY state", reader.getParsePosition());
        }
        return parseArrayTree(reader, collector);
    }
}
