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
import org.json.util.JSONPointerUtils;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Builds a JSON object model using events supplied from {@link JSONStreamReader}.
 * This uses minimal stack space than parsing via {@link org.json.JSONTokener},
 * since only a few stack frames are allocated, by using a trampoline.
 *
 * @author JSON.org
 * @version 2016-08-01
 */
public final class JSONTrampolineBuilder {

    private JSONTrampolineBuilder() {
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
     * <p>The reader must be at the beginning of the document.</p>
     *
     * @param reader    A source stream.
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(JSONStreamReader reader) throws JSONException {
        return buildJSONValue(reader, null);
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
     * <p>The reader must be at the beginning of the document.</p>
     *
     * @param reader    A source stream.
     * @return a JSON value of the type defined above
     */
    public static Object buildJSONValue(JSONStreamReader reader, LimitFilter filter) throws JSONException {
        ParseState state = reader.nextState();

        if(state != ParseState.DOCUMENT) {
            throw new JSONParseException("JSON parser should be at the beginning",
                    reader.getParsePosition());
        }

        state = reader.nextState();
        Object result;

        switch(state) {
            case OBJECT:
                result = parseObjectTree(reader, filter);
                break;

            case ARRAY:
                result = parseArrayTree(reader, filter);
                break;

            case NULL_VALUE:
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
     * Build a JSONObject from a {@code JSONStreamReader}. The reader must be
     * at the beginning of the document.
     *
     * @param reader    A source stream.
     * @return a JSONObject value
     */
    public static JSONObject buildJSONObject(JSONStreamReader reader) throws JSONException {
        return buildJSONObject(reader, null);
    }
    /**
     * Build a JSONObject from a {@code JSONStreamReader}. The reader must be
     * at the beginning of the document.
     *
     * @param reader    A source stream.
     * @return a JSONObject value
     */
    public static JSONObject buildJSONObject(JSONStreamReader reader, LimitFilter filter) throws JSONException {
        ParseState state = reader.nextState();

        if(state != ParseState.DOCUMENT) {
            throw new JSONParseException("JSON parser should be at the beginning",
                    reader.getParsePosition());
        }

        JSONObject result;
        state = reader.nextState();

        switch(state) {
            case OBJECT:
                result = parseObjectTree(reader, filter);
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
    public static JSONArray buildJSONArray(InputStream inputStream, Charset charset) throws JSONException {
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
     * Build a JSONArray from a {@code JSONStreamReader}. The reader must be
     * at the beginning of the document.
     *
     * @param reader    A source stream.
     * @return a JSONArray value
     */
    public static JSONArray buildJSONArray(JSONStreamReader reader) throws JSONException {
        return buildJSONArray(reader, null);
    }

    /**
     * Build a JSONArray from a {@code JSONStreamReader}. The reader must be
     * at the beginning of the document.
     *
     * @param reader    A source stream.
     * @return a JSONArray value
     */
    public static JSONArray buildJSONArray(JSONStreamReader reader, LimitFilter filter) throws JSONException {
        ParseState state = reader.nextState();

        if(state != ParseState.DOCUMENT) {
            throw new JSONParseException("JSON parser should be at the beginning",
                    reader.getParsePosition());
        }

        JSONArray result;
        state = reader.nextState();

        switch(state) {
            case ARRAY:
                result = parseArrayTree(reader, filter);
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
     * @param filter any filter to be applied to the stream
     * @return the populated JSON array
     */
    private static JSONArray parseArrayTree(JSONStreamReader reader,
                                            LimitFilter filter) throws JSONException {
        JSONArray array = new JSONArray();
        ALStack<StructureBuilder> stack = new ALStack<StructureBuilder>();
        stack.push(new StructureArrayBuilder(array, filter));
        ParseState state;

        try {
            while(!stack.isEmpty()) {
                state = reader.nextState();
                stack.peek().accept(state, stack, reader);
            }
        } catch (RuntimeException e) {
            throw new JSONParseException(e.getMessage() + ", at "
                    + JSONPointerUtils.toJSONPointer(stack),
                    reader.getParsePosition());
        }
        return array;
    }

    /**
     * Iterate over a stack of StructureBuilder objects, starting from an
     * initial {@code JSONObject} builder. Using this type of iterative approach
     * instead of recursion is known as trampolining.
     *
     * @param reader the stream reader
     * @param filter any filter to be applied to the stream
     * @return the populated JSON object
     */
    private static JSONObject parseObjectTree(JSONStreamReader reader,
                                              LimitFilter filter) throws JSONException {
        JSONObject object = new JSONObject();
        ALStack<StructureBuilder> stack = new ALStack<StructureBuilder>();
        stack.push(new StructureObjectBuilder(object, filter));
        ParseState state;

        try {
            while(!stack.isEmpty()) {
                state = reader.nextState();
                stack.peek().accept(state, stack, reader);
            }
        } catch (RuntimeException e) {
            throw new JSONParseException(e.getMessage() + ", at "
                    + JSONPointerUtils.toJSONPointer(stack),
                    reader.getParsePosition());
        }
        return object;
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
     * @return a JSONObject representing the subtree starting at the current
     * OBJECT state
     */
    public static JSONObject buildObjectSubTree(JSONStreamReader reader) throws JSONException {
        if((reader.currentState() != ParseState.OBJECT) || (reader.getStackDepth() == 0)) {
            throw new JSONParseException("Expected OBJECT state",
                    reader.getParsePosition());
        }
        return parseObjectTree(reader, null);
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
     * @return a JSONArray representing the subtree starting at the current
     * ARRAY state
     */
    public static JSONArray buildArraySubTree(JSONStreamReader reader) throws JSONException {
        if((reader.currentState() != ParseState.ARRAY) || (reader.getStackDepth() == 0)) {
            throw new JSONParseException("Expected ARRAY state",
                    reader.getParsePosition());
        }
        return parseArrayTree(reader, null);
    }
}
