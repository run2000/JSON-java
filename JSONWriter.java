package org.json;

import org.json.util.ALStack;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/*
Copyright (c) 2006 JSON.org

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
 * JSONWriter provides a quick and convenient way of producing JSON text.
 * The texts produced strictly conform to JSON syntax rules. No whitespace is
 * added, so the results are ready for transmission or storage. Each instance of
 * JSONWriter can produce one JSON text.
 * <p>
 * A JSONWriter instance provides a <code>value</code> method for appending
 * values to the
 * text, and a <code>key</code>
 * method for adding keys before values in objects. There are <code>array</code>
 * and <code>endArray</code> methods that make and bound array values, and
 * <code>object</code> and <code>endObject</code> methods which make and bound
 * object values. All of these methods return the JSONWriter instance,
 * permitting a cascade style. For example, <pre>
 * new JSONWriter(myWriter)
 *     .object()
 *         .key("JSON")
 *         .value("Hello, World!")
 *     .endObject();</pre> which writes <pre>
 * {"JSON":"Hello, World!"}</pre>
 * <p>
 * The first method called must be <code>array</code> or <code>object</code>.
 * There are no methods for adding commas or colons. JSONWriter adds them for
 * you. Objects and arrays can be nested arbitrarily deep.
 * <p>
 * This can sometimes be easier than using a JSONObject to build a string.
 * @author JSON.org
 * @version 2016-08-04
 */
public class JSONWriter implements Closeable {
    private static final int initdepth = 16;

    /**
     * The comma flag determines if a comma should be output before the next
     * value.
     */
    private boolean comma;

    /**
     * The current mode. Values:
     * 'a' (array),
     * 'd' (done),
     * 'i' (initial),
     * 'k' (key),
     * 'o' (object).
     */
    protected char mode;

    /**
     * The object/array stack.
     */
    private final ALStack<Set<String>> stack;

    /**
     * The writer that will receive the output.
     */
    protected Appendable writer;

    /**
     * Make a fresh JSONWriter. It can be used to build one JSON text.
     *
     * @param w the Writer to which JSON content will be written
     */
    public JSONWriter(Appendable w) {
        this.comma = false;
        this.mode = 'i';
        this.stack = new ALStack<Set<String>>(initdepth);
        this.writer = w;
    }

    /**
     * Append a JSON-encoded value.
     * @param string A string value.
     * @return this
     * @throws JSONException If the value is out of sequence.
     */
    private JSONWriter append(String string) throws JSONException {
        if (string == null) {
            throw new JSONException("Null pointer");
        }
        if (this.mode == 'o' || this.mode == 'a') {
            try {
                if (this.comma && this.mode == 'a') {
                    this.writer.append(',');
                }
                this.writer.append(string);
            } catch (IOException e) {
                throw new JSONException(e);
            }
            if (this.mode == 'o') {
                this.mode = 'k';
            }
            this.comma = true;
            return this;
        }
        throw new JSONException("Value out of sequence.");
    }

    /**
     * Append a value, converting it into a JSON string.
     *
     * @param val A value.
     * @return this
     * @throws JSONException If the value is out of sequence.
     */
    private JSONWriter appendValue(Object val) throws JSONException {
        try {
            switch (this.mode) {
                case 'a':
                    if (this.comma) {
                        this.writer.append(',');
                    }
                    break;
                case 'o':
                    this.mode = 'k';
                    break;
                default:
                    throw new JSONException("Value out of sequence.");
            }
            JSONObject.writeValue(this.writer, val);
            this.comma = true;
            return this;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Begin appending a new array. All values until the balancing
     * <code>endArray</code> will be appended to this array. The
     * <code>endArray</code> method must be called to mark the array's end.
     * @return this
     * @throws JSONException If the nesting is too deep, or if the object is
     * started in the wrong place (for example as a key or after the end of the
     * outermost array or object).
     */
    public JSONWriter array() throws JSONException {
        if (this.mode == 'i' || this.mode == 'o' || this.mode == 'a') {
            this.push(false);
            this.append("[");
            this.comma = false;
            return this;
        }
        throw new JSONException("Misplaced array.");
    }

    /**
     * End something.
     * @param mode Mode
     * @param c Closing character
     * @return this
     * @throws JSONException If unbalanced.
     */
    private JSONWriter end(char mode, char c) throws JSONException {
        if (this.mode != mode) {
            throw new JSONException(mode == 'a'
                ? "Misplaced endArray."
                : "Misplaced endObject.");
        }
        this.pop(mode);
        try {
            this.writer.append(c);
        } catch (IOException e) {
            throw new JSONException(e);
        }
        this.comma = true;
        return this;
    }

    /**
     * End an array. This method most be called to balance calls to
     * <code>array</code>.
     * @return this
     * @throws JSONException If incorrectly nested.
     */
    public JSONWriter endArray() throws JSONException {
        return this.end('a', ']');
    }

    /**
     * End an object. This method most be called to balance calls to
     * <code>object</code>.
     * @return this
     * @throws JSONException If incorrectly nested.
     */
    public JSONWriter endObject() throws JSONException {
        return this.end('k', '}');
    }

    /**
     * Append a key. The key will be associated with the next value. In an
     * object, every value must be preceded by a key.
     * @param string A key string.
     * @return this
     * @throws JSONException If the key is out of place. For example, keys
     *  do not belong in arrays or if the key is null.
     */
    public JSONWriter key(String string) throws JSONException {
        if (string == null) {
            throw new JSONException("Null key.");
        }
        if (this.mode == 'k') {
            try {
                if(!this.stack.peek().add(string)) {
                    throw new JSONException("Duplicate key \"" + string + "\"");
                }
                //this.stack[this.top - 1].putOnce(string, Boolean.TRUE);
                if (this.comma) {
                    this.writer.append(',');
                }
                JSONObject.quote(string, this.writer);
                this.writer.append(':');
                this.comma = false;
                this.mode = 'o';
                return this;
            } catch (IOException e) {
                throw new JSONException(e);
            }
        }
        throw new JSONException("Misplaced key.");
    }


    /**
     * Begin appending a new object. All keys and values until the balancing
     * <code>endObject</code> will be appended to this object. The
     * <code>endObject</code> method must be called to mark the object's end.
     * @return this
     * @throws JSONException If the nesting is too deep, or if the object is
     * started in the wrong place (for example as a key or after the end of the
     * outermost array or object).
     */
    public JSONWriter object() throws JSONException {
        if (this.mode == 'i') {
            this.mode = 'o';
        }
        if (this.mode == 'o' || this.mode == 'a') {
            this.append("{");
            this.push(true);
            this.comma = false;
            return this;
        }
        throw new JSONException("Misplaced object.");

    }


    /**
     * Pop an array or object scope.
     * @param c The scope to close.
     * @throws JSONException If nesting is wrong.
     */
    private void pop(char c) throws JSONException {
        if (this.stack.isEmpty()) {
            throw new JSONException("Nesting error.");
        }
        char m = this.stack.pop() == null ? 'a' : 'k';
        if (m != c) {
            throw new JSONException("Nesting error.");
        }
        this.mode = this.stack.isEmpty()
            ? 'd'
            : this.stack.peek() == null
            ? 'a'
            : 'k';
    }

    /**
     * Push an object or array scope.
     *
     * @param obj {@code true} to indicate an Object, otherwise {@code false}
     *            to indicate an Array
     * @throws JSONException If nesting is too deep.
     */
    private void push(boolean obj) throws JSONException {
        this.stack.push(obj ? new HashSet<String>() : null);
        this.mode = obj ? 'k' : 'a';
    }

    /**
     * Append either the value <code>true</code> or the value
     * <code>false</code>.
     * @param b A boolean.
     * @return this
     * @throws JSONException
     */
    public JSONWriter value(boolean b) throws JSONException {
        return this.append(b ? "true" : "false");
    }

    /**
     * Append a double value.
     * @param d A double.
     * @return this
     * @throws JSONException If the number is not finite.
     */
    public JSONWriter value(double d) throws JSONException {
        return this.append(JSONObject.numberToString(Double.valueOf(d)));
    }

    /**
     * Append a long value.
     * @param l A long.
     * @return this
     * @throws JSONException
     */
    public JSONWriter value(long l) throws JSONException {
        return this.append(Long.toString(l));
    }


    /**
     * Append an object value.
     * @param object The object to append. It can be null, or a Boolean, Number,
     *   String, JSONObject, or JSONArray, or an object that implements JSONString.
     * @return this
     * @throws JSONException If the value is out of sequence.
     */
    public JSONWriter value(Object object) throws JSONException {
        return this.appendValue(object);
    }

    /**
     * Append a sequence of values into an array.
     *
     * @param values The objects to append. They can be null, or a Boolean, Number,
     *   String, JSONObject, or JSONArray, or an object that implements JSONString.
     * @return this
     * @throws JSONException If a value is out of place. For example, a value
     *  occurs where a key is expected.
     */
    public JSONWriter values(Iterable<?> values) throws JSONException {
        for(Object obj : values) {
            this.appendValue(obj);
        }
        return this;
    }

    /**
     * Append a sequence of key-value pairs into an object.
     *
     * @param kvPairs The objects to append. The values can be null, or a Boolean,
     *   Number, String, JSONObject, or JSONArray, or an object that implements
     *   JSONString.
     * @return this
     * @throws JSONException If a value is out of place. For example, a value
     *  occurs where a key is expected.
     */
    public JSONWriter entries(Map<String, ?> kvPairs) throws JSONException {
        for(Map.Entry<String, ?> entry : kvPairs.entrySet()) {
            this.key(entry.getKey());
            this.appendValue(entry);
        }
        return this;
    }

    /**
     * Asserts the JSON writer is finished, and close any underlying
     * {@code Closeable} writer.
     *
     * @throws IOException the writer cannot be closed
     */
    @Override
    public void close() throws IOException {
        if(!this.stack.isEmpty()) {
            throw new IOException("JSON stack is not empty");
        }

        if(writer instanceof Closeable) {
            ((Closeable)writer).close();
        }
    }
}
