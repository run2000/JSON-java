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
import org.json.util.ALStack;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Builds a JSON object model using events supplied from {@link JSONLimitStreamReader},
 * using secure principles derived from XML parsers.
 * <p>
 * This class is similar in its external contract to the {@link JSONObjectBuilder}
 * class, but intended to be more robust against untrusted JSON sources.
 * For instance, by not using runtime stack for parsing nested JSON structures,
 * it is less susceptible to stack smashing attempts.</p>
 * <p>
 * For some practical limits for parsing JSON data, see {@link BuilderLimits#secureDefaults()}.
 * </p>
 *
 * @author JSON.org
 * @version 2016-08-01
 */
public final class JSONLimitBuilder {

    private static final BuilderLimits DEFAULT_LIMITS = new BuilderLimits();

    private JSONLimitBuilder() {
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
     * @param reader A reader.
     * @param limits the limits imposed on the builder
     * @param collector collector object for creating JSON structures
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(Reader reader, BuilderLimits limits,
            StructureCollector<?, ?, ?, ?> collector) throws JSONException {
        return buildJSONValue(new JSONLimitStreamReader(reader), limits, collector);
    }

    /**
     * Build a JSON value from a {@code Reader}. The value may be one of:
     * <ul>
     * <li>{@code JSONObject.NULL}</li>
     * <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     * <li>A {@code Double}, {@code Long}, {@code Integer},
     *     {@code BigDecimal}, or {@code BigInteger}</li>
     * <li>A {@code String}</li>
     * <li>A {@code JSONObject}</li>
     * <li>A {@code JSONArray}</li>
     * </ul>
     *
     * @param reader A reader.
     * @param limits the limits imposed on the builder
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(Reader reader, BuilderLimits limits)
            throws JSONException {
        return buildJSONValue(new JSONLimitStreamReader(reader), limits,
                JSONCollector.INSTANCE);
    }

    /**
     * Build a JSON value from a {@code Reader}. The value may be one of:
     * <ul>
     * <li>{@code JSONObject.NULL}</li>
     * <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     * <li>A {@code Double}, {@code Long}, {@code Integer},
     *     {@code BigDecimal}, or {@code BigInteger}</li>
     * <li>A {@code String}</li>
     * <li>A {@code JSONObject}</li>
     * <li>A {@code JSONArray}</li>
     * </ul>
     *
     * @param reader A reader.
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(Reader reader) throws JSONException {
        return buildJSONValue(new JSONLimitStreamReader(reader), DEFAULT_LIMITS,
                JSONCollector.INSTANCE);
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
     * @param inputStream the input stream containing the JSON data
     * @param charset     the character set with which to interpret the
     *                    input stream
     * @param limits      the limits imposed on the builder
     * @param collector   collector object for creating JSON structures
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(InputStream inputStream, Charset charset,
            BuilderLimits limits, StructureCollector<?, ?, ?, ?> collector)
            throws JSONException {
        return buildJSONValue(new JSONLimitStreamReader(inputStream, charset),
                limits, collector);
    }

    /**
     * Build a JSON value from a {@code InputStream} and supplied
     * {@code Charset}. The value may be one of:
     * <ul>
     * <li>{@code JSONObject.NULL}</li>
     * <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     * <li>A {@code Double}, {@code Long}, {@code Integer},
     *     {@code BigDecimal}, or {@code BigInteger}</li>
     * <li>A {@code String}</li>
     * <li>A {@code JSONObject}</li>
     * <li>A {@code JSONArray}</li>
     * </ul>
     *
     * @param inputStream the input stream containing the JSON data
     * @param charset     the character set with which to interpret the
     *                    input stream
     * @param limits      the limits imposed on the builder
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(InputStream inputStream, Charset charset,
            BuilderLimits limits) throws JSONException {
        return buildJSONValue(new JSONLimitStreamReader(inputStream, charset),
                limits, JSONCollector.INSTANCE);
    }

    /**
     * Build a JSON value from a {@code InputStream} and supplied
     * {@code Charset}. The value may be one of:
     * <ul>
     * <li>{@code JSONObject.NULL}</li>
     * <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     * <li>A {@code Double}, {@code Long}, {@code Integer},
     *     {@code BigDecimal}, or {@code BigInteger}</li>
     * <li>A {@code String}</li>
     * <li>A {@code JSONObject}</li>
     * <li>A {@code JSONArray}</li>
     * </ul>
     *
     * @param inputStream the input stream containing the JSON data
     * @param charset     the character set with which to interpret the
     *                    input stream
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(InputStream inputStream, Charset charset)
            throws JSONException {
        return buildJSONValue(new JSONLimitStreamReader(inputStream, charset),
                DEFAULT_LIMITS, JSONCollector.INSTANCE);
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
     * @param s      A source string.
     * @param limits the limits imposed on the builder
     * @param collector collector object for creating JSON structures
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(String s, BuilderLimits limits,
            StructureCollector<?, ?, ?, ?> collector) throws JSONException {
        return buildJSONValue(new JSONLimitStreamReader(s), limits, collector);
    }

    /**
     * Build a JSON value from a {@code String}. The value may be one of:
     * <ul>
     * <li>{@code JSONObject.NULL}</li>
     * <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     * <li>A {@code Double}, {@code Long}, {@code Integer},
     *     {@code BigDecimal}, or {@code BigInteger}</li>
     * <li>A {@code String}</li>
     * <li>A {@code JSONObject}</li>
     * <li>A {@code JSONArray}</li>
     * </ul>
     *
     * @param s      A source string.
     * @param limits the limits imposed on the builder
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(String s, BuilderLimits limits) throws JSONException {
        return buildJSONValue(new JSONLimitStreamReader(s), limits,
                JSONCollector.INSTANCE);
    }

    /**
     * Build a JSON value from a {@code String}. The value may be one of:
     * <ul>
     * <li>{@code JSONObject.NULL}</li>
     * <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     * <li>A {@code Double}, {@code Long}, {@code Integer},
     *     {@code BigDecimal}, or {@code BigInteger}</li>
     * <li>A {@code String}</li>
     * <li>A {@code JSONObject}</li>
     * <li>A {@code JSONArray}</li>
     * </ul>
     *
     * @param s A source string.
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(String s) throws JSONException {
        return buildJSONValue(new JSONLimitStreamReader(s), DEFAULT_LIMITS,
                JSONCollector.INSTANCE);
    }

    /**
     * Build a JSON value from a {@code JSONStreamReader}. The value may be one
     * of:
     * <ul>
     * <li>{@code JSONObject.NULL}</li>
     * <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     * <li>A {@code Double}, {@code Long}, {@code Integer},
     *     {@code BigDecimal}, or {@code BigInteger}</li>
     * <li>A {@code String}</li>
     * <li>A {@code JSONObject}</li>
     * <li>A {@code JSONArray}</li>
     * </ul>
     * <p>The reader must be at the beginning of the document.</p>
     *
     * @param reader A source stream.
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(JSONLimitStreamReader reader) throws JSONException {
        return buildJSONValue(reader, DEFAULT_LIMITS, JSONCollector.INSTANCE);
    }

    /**
     * Build a JSON value from a {@code JSONStreamReader}. The value may be one
     * of:
     * <ul>
     * <li>{@code JSONObject.NULL}</li>
     * <li>{@code Boolean.TRUE} or {@code Boolean.FALSE}</li>
     * <li>A {@code Double}, {@code Long}, {@code Integer},
     *     {@code BigDecimal}, or {@code BigInteger}</li>
     * <li>A {@code String}</li>
     * <li>A {@code JSONObject}</li>
     * <li>A {@code JSONArray}</li>
     * </ul>
     * <p>The reader must be at the beginning of the document.</p>
     *
     * @param reader A source stream.
     * @param limits the limits imposed on the builder
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(JSONLimitStreamReader reader,
            BuilderLimits limits) throws JSONException {
        return buildJSONValue(reader, limits, JSONCollector.INSTANCE);
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
     * <p>The reader must be at the beginning of the document.</p>
     *
     * @param reader A source stream.
     * @param limits the limits imposed on the builder
     * @param collector collector object for creating JSON structures
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(JSONLimitStreamReader reader,
            BuilderLimits limits, StructureCollector<?, ?, ?, ?> collector) throws JSONException {
        if(reader == null) {
            throw new NullPointerException("reader is null");
        }
        if(limits == null) {
            throw new NullPointerException("limits is null");
        }
        if(collector == null) {
            throw new NullPointerException("collector is null");
        }
        reader.withLimits(limits);
        ParseState state = reader.nextState();

        if (state != ParseState.DOCUMENT) {
            throw new JSONParseException("JSON parser should be at the beginning",
                    reader.getParsePosition());
        }

        state = reader.nextState();
        Object result;

        switch (state) {
            case OBJECT:
                result = parseObjectTree(reader, limits, collector);
                break;

            case ARRAY:
                result = parseArrayTree(reader, limits, collector);
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

        if (reader.nextState() != ParseState.END_DOCUMENT) {
            throw new JSONParseException("JSON parser in an unexpected state",
                    reader.getParsePosition());
        }

        return result;
    }

    /**
     * Build a JSONObject from a {@code Reader}.
     *
     * @param reader A reader.
     * @param limits the limits imposed on the builder
     * @param collector collector object for creating JSON structures
     * @param <OR> the result type of the JSON object constructed by the collector
     * @return a JSON object value
     */
    public static <OR> OR buildJSONObject(Reader reader, BuilderLimits limits,
            StructureCollector<?, ?, OR, ?> collector) throws JSONException {
        return buildJSONObject(new JSONLimitStreamReader(reader), limits, collector);
    }

    /**
     * Build a JSONObject from a {@code Reader}.
     *
     * @param reader A reader.
     * @param limits the limits imposed on the builder
     * @return a JSONObject value
     */
    public static JSONObject buildJSONObject(Reader reader, BuilderLimits limits)
            throws JSONException {
        return buildJSONObject(new JSONLimitStreamReader(reader), limits,
                JSONCollector.INSTANCE);
    }

    /**
     * Build a JSONObject from a {@code Reader}.
     *
     * @param reader A reader.
     * @return a JSONObject value
     */
    public static JSONObject buildJSONObject(Reader reader) throws JSONException {
        return buildJSONObject(new JSONLimitStreamReader(reader), DEFAULT_LIMITS,
                JSONCollector.INSTANCE);
    }

    /**
     * Build a JSONObject from a {@code InputStream} and supplied
     * {@code Charset}.
     *
     * @param inputStream the input stream containing the JSON data
     * @param charset     the character set with which to interpret the
     *                    input stream
     * @param limits      the limits imposed on the builder
     * @param collector   collector object for creating JSON structures
     * @param <OR> the result type of the JSON object constructed by the collector
     * @return a JSON object value
     */
    public static <OR> OR buildJSONObject(InputStream inputStream, Charset charset,
            BuilderLimits limits, StructureCollector<?, ?, OR, ?> collector)
            throws JSONException {
        return buildJSONObject(new JSONLimitStreamReader(inputStream, charset),
                limits, collector);
    }

    /**
     * Build a JSONObject from a {@code InputStream} and supplied
     * {@code Charset}.
     *
     * @param inputStream the input stream containing the JSON data
     * @param charset     the character set with which to interpret the
     *                    input stream
     * @param limits      the limits imposed on the builder
     * @return a JSONObject value
     */
    public static JSONObject buildJSONObject(InputStream inputStream,
            Charset charset, BuilderLimits limits) throws JSONException {
        return buildJSONObject(new JSONLimitStreamReader(inputStream, charset),
                limits, JSONCollector.INSTANCE);
    }

    /**
     * Build a JSONObject from a {@code InputStream} and supplied
     * {@code Charset}.
     *
     * @param inputStream the input stream containing the JSON data
     * @param charset     the character set with which to interpret the
     *                    input stream
     * @return a JSONObject value
     */
    public static JSONObject buildJSONObject(InputStream inputStream,
            Charset charset) throws JSONException {
        return buildJSONObject(new JSONLimitStreamReader(inputStream, charset),
                DEFAULT_LIMITS, JSONCollector.INSTANCE);
    }

    /**
     * Build a JSONObject from a {@code String}.
     *
     * @param s      A source string.
     * @param limits the limits imposed on the builder
     * @param collector collector object for creating JSON structures
     * @param <OR> the result type of the JSON object constructed by the collector
     * @return a JSON object value
     */
    public static <OR> OR buildJSONObject(String s, BuilderLimits limits,
            StructureCollector<?, ?, OR, ?> collector) throws JSONException {
        return buildJSONObject(new JSONLimitStreamReader(s), limits, collector);
    }

    /**
     * Build a JSONObject from a {@code String}.
     *
     * @param s      A source string.
     * @param limits the limits imposed on the builder
     * @return a JSONObject value
     */
    public static JSONObject buildJSONObject(String s, BuilderLimits limits)
            throws JSONException {
        return buildJSONObject(new JSONLimitStreamReader(s), limits,
                JSONCollector.INSTANCE);
    }

    /**
     * Build a JSONObject from a {@code String}.
     *
     * @param s      A source string.
     * @return a JSONObject value
     */
    public static JSONObject buildJSONObject(String s) throws JSONException {
        return buildJSONObject(new JSONLimitStreamReader(s), DEFAULT_LIMITS,
                JSONCollector.INSTANCE);
    }

    /**
     * Build a JSONObject from a {@code JSONLimitStreamReader}.
     *
     * @param reader A source stream.
     * @return a JSONObject value
     */
    public static JSONObject buildJSONObject(JSONLimitStreamReader reader)
            throws JSONException {
        return buildJSONObject(reader, DEFAULT_LIMITS, JSONCollector.INSTANCE);
    }

    /**
     * Build a JSONObject from a {@code JSONStreamReader}. The reader must be
     * at the beginning of the document.
     *
     * @param reader    A source stream.
     * @param limits the limits imposed on the builder
     * @return a JSONObject value
     */
    public static JSONObject buildJSONObject(JSONLimitStreamReader reader,
            BuilderLimits limits) throws JSONException {
        return buildJSONObject(reader, limits, JSONCollector.INSTANCE);
    }

    /**
     * Build a JSON object from a {@code JSONStreamReader}. The reader must be
     * at the beginning of the document.
     *
     * @param reader    A source stream.
     * @param limits the limits imposed on the builder
     * @param collector collector for creating JSON structures
     * @param <OR> the result type of the JSON object constructed by the collector
     * @return a JSON object value
     */
    public static <OR> OR buildJSONObject(JSONLimitStreamReader reader, BuilderLimits limits,
            StructureCollector<?, ?, OR, ?> collector) throws JSONException {
        if(reader == null) {
            throw new NullPointerException("reader is null");
        }
        if(limits == null) {
            throw new NullPointerException("limits is null");
        }
        if(collector == null) {
            throw new NullPointerException("collector is null");
        }
        reader.withLimits(limits);
        ParseState state = reader.nextState();

        if(state != ParseState.DOCUMENT) {
            throw new JSONParseException("JSON parser should be at the beginning",
                    reader.getParsePosition());
        }

        OR result;
        state = reader.nextState();

        switch(state) {
            case OBJECT:
                result = parseObjectTree(reader, limits, collector);
                break;

            default:
                throw new JSONParseException("Expected object",
                        reader.getParsePosition());
        }

        if(reader.nextState() != ParseState.END_DOCUMENT) {
            throw new JSONParseException("JSON parser in an unexpected state",
                    reader.getParsePosition());
        }

        return result;
    }

    /**
     * Build a JSONArray from a {@code Reader}.
     *
     * @param reader     A reader.
     * @param limits the limits imposed on the builder
     * @param collector collector object for creating JSON structures
     * @param <AR> the result type of the JSON array constructed by the collector
     * @return a JSON array value
     */
    public static <AR> AR buildJSONArray(Reader reader, BuilderLimits limits,
            StructureCollector<?, ?, ?, AR> collector) throws JSONException {
        return buildJSONArray(new JSONLimitStreamReader(reader), limits, collector);
    }

    /**
     * Build a JSONArray from a {@code Reader}.
     *
     * @param reader     A reader.
     * @param limits the limits imposed on the builder
     * @return a JSONArray value
     */
    public static JSONArray buildJSONArray(Reader reader, BuilderLimits limits)
            throws JSONException {
        return buildJSONArray(new JSONLimitStreamReader(reader), limits,
                JSONCollector.INSTANCE);
    }

    /**
     * Build a JSONArray from a {@code Reader}.
     *
     * @param reader     A reader.
     * @return a JSONArray value
     */
    public static JSONArray buildJSONArray(Reader reader) throws JSONException {
        return buildJSONArray(new JSONLimitStreamReader(reader), DEFAULT_LIMITS,
                JSONCollector.INSTANCE);
    }

    /**
     * Build a JSONArray from a {@code InputStream} and supplied
     * {@code Charset}.
     *
     * @param inputStream   the input stream containing the JSON data
     * @param charset       the character set with which to interpret the
     *                      input stream
     * @param limits the limits imposed on the builder
     * @param collector collector object for creating JSON structures
     * @param <AR> the result type of the JSON array constructed by the collector
     * @return a JSON array value
     */
    public static <AR> AR buildJSONArray(InputStream inputStream, Charset charset,
            BuilderLimits limits, StructureCollector<?, ?, ?, AR> collector)
            throws JSONException {
        return buildJSONArray(new JSONLimitStreamReader(inputStream, charset),
                limits, collector);
    }

    /**
     * Build a JSONArray from a {@code InputStream} and supplied
     * {@code Charset}.
     *
     * @param inputStream   the input stream containing the JSON data
     * @param charset       the character set with which to interpret the
     *                      input stream
     * @param limits the limits imposed on the builder
     * @return a JSONArray value
     */
    public static JSONArray buildJSONArray(InputStream inputStream, Charset charset,
            BuilderLimits limits) throws JSONException {
        return buildJSONArray(new JSONLimitStreamReader(inputStream, charset),
                limits, JSONCollector.INSTANCE);
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
        return buildJSONArray(new JSONLimitStreamReader(inputStream, charset),
                DEFAULT_LIMITS, JSONCollector.INSTANCE);
    }

    /**
     * Build a JSONArray from a {@code String}.
     *
     * @param s     A source string.
     * @param limits the limits imposed on the builder
     * @param collector collector object for creating JSON structures
     * @param <AR> the result type of the JSON array constructed by the collector
     * @return a JSON array value
     */
    public static <AR> AR buildJSONArray(String s, BuilderLimits limits,
            StructureCollector<?, ?, ?, AR> collector) throws JSONException {
        return buildJSONArray(new JSONLimitStreamReader(s), limits, collector);
    }

    /**
     * Build a JSONArray from a {@code String}.
     *
     * @param s     A source string.
     * @param limits the limits imposed on the builder
     * @return a JSONArray value
     */
    public static JSONArray buildJSONArray(String s, BuilderLimits limits) throws JSONException {
        return buildJSONArray(new JSONLimitStreamReader(s), limits,
                JSONCollector.INSTANCE);
    }

    /**
     * Build a JSONArray from a {@code String}.
     *
     * @param s     A source string.
     * @return a JSONArray value
     */
    public static JSONArray buildJSONArray(String s) throws JSONException {
        return buildJSONArray(new JSONLimitStreamReader(s), DEFAULT_LIMITS,
                JSONCollector.INSTANCE);
    }

    /**
     * Build a JSONArray from a {@code JSONLimitStreamReader}.
     *
     * @param reader A source reader.
     * @return a JSONArray value
     */
    public static JSONArray buildJSONArray(JSONLimitStreamReader reader) throws JSONException {
        return buildJSONArray(reader, DEFAULT_LIMITS, JSONCollector.INSTANCE);
    }

    /**
     * Build a JSONArray from a {@code JSONStreamReader}. The reader must be
     * at the beginning of the document.
     *
     * @param reader    A source stream.
     * @param limits the limits imposed on the builder
     * @return a JSONArray value
     */
    public static JSONArray buildJSONArray(JSONLimitStreamReader reader,
            BuilderLimits limits) throws JSONException {
        return buildJSONArray(reader, limits, JSONCollector.INSTANCE);
    }

    /**
     * Build a JSON array from a {@code JSONStreamReader}. The reader must be
     * at the beginning of the document.
     *
     * @param reader    A source stream.
     * @param limits the limits imposed on the builder
     * @param collector collector object for creating structures
     * @param <AR> the result type of the JSON array constructed by the collector
     * @return a JSON array value
     */
    public static <AR> AR buildJSONArray(JSONLimitStreamReader reader, BuilderLimits limits,
            StructureCollector<?, ?, ?, AR> collector) throws JSONException {
        if(reader == null) {
            throw new NullPointerException("reader is null");
        }
        if(limits == null) {
            throw new NullPointerException("limits is null");
        }
        if(collector == null) {
            throw new NullPointerException("collector is null");
        }
        reader.withLimits(limits);
        ParseState state = reader.nextState();

        if(state != ParseState.DOCUMENT) {
            throw new JSONParseException("JSON parser should be at the beginning",
                    reader.getParsePosition());
        }

        AR result;
        state = reader.nextState();

        switch(state) {
            case ARRAY:
                result = parseArrayTree(reader, limits, collector);
                break;

            default:
                throw new JSONParseException("Expected array",
                        reader.getParsePosition());
        }

        if(reader.nextState() != ParseState.END_DOCUMENT) {
            throw new JSONParseException("JSON parser in an unexpected state",
                    reader.getParsePosition());
        }

        return result;
    }

    /**
     * Iterate over a stack of StructureBuilder objects, starting from an
     * initial {@code JSONArray} builder. Using this type of iterative approach
     * instead of recursion is known as trampolining.
     *
     * @param reader the stream reader
     * @param limits the limits imposed on the builder
     * @param collector collector object for creating new objects and arrays
     * @return the populated JSON array
     */
    private static <OA, AA, OR, AR> AR parseArrayTree(JSONLimitStreamReader reader,
            BuilderLimits limits, StructureCollector<OA, AA, OR, AR> collector)
            throws JSONException {
        ALStack<StructureIdentifier> stack = new ALStack<StructureIdentifier>();
        StructureArrayBuilder<?, ?, ?, AR> builder = new StructureArrayBuilder<OA, AA, OR, AR>(null, limits, collector);
        stack.push(builder);
        ParseState state;

        try {
            StructureBuilder<?> curr = builder;
            while(curr != null) {
                state = reader.nextState();
                curr = curr.accept(state, stack, reader);
            }
        } catch (RuntimeException e) {
            throw new JSONParseException(e.getMessage() + ", at "
                    + JSONPointerUtils.toJSONPointer(stack),
                    reader.getParsePosition());
        }
        return builder.getResult();
    }

    /**
     * Iterate over a stack of StructureBuilder objects, starting from an
     * initial {@code JSONObject} builder. Using this type of iterative approach
     * instead of recursion is known as trampolining.
     *
     * @param reader the stream reader
     * @param limits the limits imposed on the builder
     * @param collector collector object for creating new objects and arrays
     * @return the populated JSON object
     */
    private static <OA, AA, OR, AR> OR parseObjectTree(JSONLimitStreamReader reader,
            BuilderLimits limits, StructureCollector<OA, AA, OR, AR> collector)
            throws JSONException {
        ALStack<StructureIdentifier> stack = new ALStack<StructureIdentifier>();
        StructureObjectBuilder<?, ?, OR, ?> builder = new StructureObjectBuilder<OA, AA, OR, AR>(null, limits, collector);
        stack.push(builder);
        ParseState state;

        try {
            StructureBuilder<?> curr = builder;
            while(curr != null) {
                state = reader.nextState();
                curr = curr.accept(state, stack, reader);
            }
        } catch (RuntimeException e) {
            throw new JSONParseException(e.getMessage() + ", at "
                    + JSONPointerUtils.toJSONPointer(stack),
                    reader.getParsePosition());
        }
        return builder.getResult();
    }

    /**
     * If the given JSONLimitStreamReader's ParseState was {@link ParseState#OBJECT},
     * return the entire subtree as a JSONObject value. This method advances the
     * parser onto the {@link ParseState#END_OBJECT} state.
     * <p>
     * If the JSON stream is not parseable as an object, a JSONException
     * will be thrown.
     * </p>
     *
     * @param reader A source stream reader.
     * @param limits the limits imposed on the builder
     * @return a JSONObject representing the subtree starting at the current
     * OBJECT state
     */
    public static JSONObject buildObjectSubTree(JSONLimitStreamReader reader,
            BuilderLimits limits) throws JSONException {
        if((reader.currentState() != ParseState.OBJECT) || (reader.getStackDepth() == 0)) {
            throw new JSONParseException("Expected OBJECT state", reader.getParsePosition());
        }
        if(limits == null) {
            throw new NullPointerException("limits is null");
        }
        return parseObjectTree(reader, limits, JSONCollector.INSTANCE);
    }

    /**
     * If the given JSONLimitStreamReader's ParseState was {@link ParseState#OBJECT},
     * return the entire subtree as an object type, as specified by the given
     * collector. This method advances the parser onto the
     * {@link ParseState#END_OBJECT} state.
     * <p>
     * If the JSON stream is not parseable as an object, a JSONException
     * will be thrown.
     * </p>
     *
     * @param reader A source stream reader.
     * @param limits the limits imposed on the builder
     * @param collector collector object for creating JSON structures
     * @param <OR> the result type of the JSON object constructed by the collector
     * @return a JSON object representing the subtree starting at the current
     * OBJECT state
     */
    public static <OR> OR buildObjectSubTree(JSONLimitStreamReader reader,
            BuilderLimits limits, StructureCollector<?, ?, OR, ?> collector)
            throws JSONException {
        if((reader.currentState() != ParseState.OBJECT) || (reader.getStackDepth() == 0)) {
            throw new JSONParseException("Expected OBJECT state", reader.getParsePosition());
        }
        if(limits == null) {
            throw new NullPointerException("limits is null");
        }
        if(collector == null) {
            throw new NullPointerException("collector is null");
        }
        return parseObjectTree(reader, limits, collector);
    }

    /**
     * If the given JSONLimitStreamReader's ParseState was {@link ParseState#ARRAY},
     * return the entire subtree as an array type, as specified by the given
     * collector. This method advances the parser onto the
     * {@link ParseState#END_ARRAY} state.
     * <p>
     * If the JSON stream is not parseable as an array, a JSONException
     * will be thrown.
     * </p>
     *
     * @param reader A source stream reader.
     * @param limits the limits imposed on the builder
     * @return a JSON array representing the subtree starting at the current
     * ARRAY state
     */
    public static JSONArray buildArraySubTree(JSONLimitStreamReader reader,
            BuilderLimits limits) throws JSONException {
        if((reader.currentState() != ParseState.ARRAY) || (reader.getStackDepth() == 0)) {
            throw new JSONParseException("Expected ARRAY state", reader.getParsePosition());
        }
        if(limits == null) {
            throw new NullPointerException("limits is null");
        }
        return parseArrayTree(reader, limits, JSONCollector.INSTANCE);
    }

    /**
     * If the given JSONLimitStreamReader's ParseState was {@link ParseState#ARRAY},
     * return the entire subtree as a JSONArray value. This method advances the
     * parser onto the {@link ParseState#END_ARRAY} state.
     * <p>
     * If the JSON stream is not parseable as an array, a JSONException
     * will be thrown.
     * </p>
     *
     * @param reader A source stream reader.
     * @param limits the limits imposed on the builder
     * @param collector collector object for creating JSON structures
     * @param <AR> the result type of the JSON array constructed by the collector
     * @return a JSON array representing the subtree starting at the current
     * ARRAY state
     */
    public static <AR> AR buildArraySubTree(JSONLimitStreamReader reader,
            BuilderLimits limits, StructureCollector<?, ?, ?, AR> collector)
            throws JSONException {
        if((reader.currentState() != ParseState.ARRAY) || (reader.getStackDepth() == 0)) {
            throw new JSONParseException("Expected ARRAY state", reader.getParsePosition());
        }
        if(limits == null) {
            throw new NullPointerException("limits is null");
        }
        if(collector == null) {
            throw new NullPointerException("collector is null");
        }
        return parseArrayTree(reader, limits, collector);
    }
}
