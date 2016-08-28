package org.json;

import org.json.util.ALStack;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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
     * The object/array stack, for duplicate key detection.
     * Arrays are represented as null elements, while objects are
     * represented as sets of key strings.
     */
    private final ALStack<Set<String>> stack;

    /**
     * The writer that will receive the output.
     */
    protected final Appendable writer;

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
     * Prepare for the next value to be written. Append a comma if required,
     * assert and advance the mode as needed.
     */
    private void prepValue() throws JSONException {
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
            this.comma = true;
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
            try {
                this.prepValue();
                this.writer.append('[');
            } catch (IOException e) {
                throw new JSONException(e);
            }
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
                if (this.comma) {
                    this.writer.append(',');
                }
                quote(string, this.writer);
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
            try {
                this.prepValue();
                this.writer.append('{');
            } catch (IOException e) {
                throw new JSONException(e);
            }
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
        String string = Boolean.toString(b);
        try {
            this.prepValue();
            this.writer.append(string);
            return this;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Append a double value.
     * @param d A double.
     * @return this
     * @throws JSONException If the number is not finite.
     */
    public JSONWriter value(double d) throws JSONException {
        this.prepValue();
        writeDouble(this.writer, d);
        return this;
    }

    /**
     * Append a long value.
     * @param l A long.
     * @return this
     * @throws JSONException
     */
    public JSONWriter value(long l) throws JSONException {
        String string = Long.toString(l);
        try {
            this.prepValue();
            this.writer.append(string);
            return this;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }


    /**
     * Append an object value.
     * @param object The object to appendString. It can be null, or a Boolean, Number,
     *   String, JSONObject, or JSONArray, or an object that implements JSONString.
     * @return this
     * @throws JSONException If the value is out of sequence.
     */
    public JSONWriter value(Object object) throws JSONException {
        this.prepValue();
        writeValue(this.writer, object);
        return this;
    }

    /**
     * Append a sequence of values into an array.
     *
     * @param values The objects to appendString. They can be null, or a Boolean, Number,
     *   String, JSONObject, or JSONArray, or an object that implements JSONString.
     * @return this
     * @throws JSONException If a value is out of place. For example, a value
     *  occurs where a key is expected.
     */
    public JSONWriter values(Iterable<?> values) throws JSONException {
        for(Object obj : values) {
            this.prepValue();
            writeValue(this.writer, obj);
        }
        return this;
    }

    /**
     * Append a sequence of keys and values in an object.
     * The key will be associated with the corresponding value. In an
     * object, every value must be associated with a key.
     *
     * @param kvPairs The objects to appendString. The values can be null, or a Boolean,
     *   Number, String, JSONObject, or JSONArray, or an object that implements
     *   JSONString.
     * @return this
     * @throws JSONException If a key or value is out of place. For example, keys
     *  do not belong in arrays or if the key is null.
     */
    public JSONWriter entries(Iterable<Entry<String, ?>> kvPairs) throws JSONException {
        for(Entry<String, ?> entry : kvPairs) {
            this.key(entry.getKey());
            this.prepValue();
            writeValue(this.writer, entry.getValue());
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

    // -- static writer methods

    // 24 spaces, divides by 1, 2, 3, 4, 6, 8, 12.
    private static final String PADDING_SPACES = "                        ";

    static void indent(Appendable writer, int indent) throws IOException {
        final int padding = PADDING_SPACES.length();

        while(indent >= padding) {
            writer.append(PADDING_SPACES);
            indent -= padding;
        }
        if(indent > 0) {
            writer.append(PADDING_SPACES, 0, indent);
        }
    }

    static <T extends Appendable> T writeValue(T writer, Object value)
            throws JSONException {
        try {
            return writeValue(writer, value, 0 ,0);
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    static <T extends Appendable> T writeValue(T writer, Object value,
            int indentFactor, int indent) throws JSONException, IOException {
        if (value == null || value.equals(null)) {
            writer.append("null");
        } else if (value instanceof JSONAppendable) {
            try {
                ((JSONAppendable)value).appendJSON(writer);
            } catch(IOException e) {
                throw new JSONException(e);
            } catch(RuntimeException e) {
                throw new JSONException(e);
            }
        } else if (value instanceof JSONString) {
            String o;
            try {
                o = ((JSONString) value).toJSONString();
            } catch (Exception e) {
                throw new JSONException(e);
            }
            if (o != null) {
                writer.append(o);
            } else {
                quote(value.toString(), writer);
            }
        } else if (value instanceof Number) {
            writeNumber(writer, (Number) value);
        } else if (value instanceof Boolean) {
            writer.append(value.toString());
        } else if (value instanceof JSONObject) {
            writeJSONObject((JSONObject)value, writer, indentFactor, indent);
        } else if (value instanceof JSONArray) {
            writeJSONArray((JSONArray) value, writer, indentFactor, indent);
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            writeMap(map, writer, indentFactor, indent);
        } else if (value instanceof Iterable) {
            Iterable<?> coll = (Iterable<?>) value;
            writeIterable(coll, writer, indentFactor, indent);
        } else if (value.getClass().isArray()) {
            writeArray(value, writer, indentFactor, indent);
        } else if(value instanceof CharSequence) {
            quote((CharSequence) value, writer);
        } else if (value instanceof Enum<?>) {
            quote(((Enum<?>)value).name(), writer);
        } else if(JSONObject.objectIsBean(value)) {
            writeBean(value, writer, indentFactor, indent);
        } else {
            quote(value.toString(), writer);
        }
        return writer;
    }

    /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places. A backslash will be inserted within &lt;/, producing &lt;\/,
     * allowing JSON text to be delivered in HTML. In JSON text, a string cannot
     * contain a control character or an unescaped quote or backslash.
     *
     * @param string
     *            A character sequence to be quoted
     * @param w
     *            the Appendable to which the quoted character sequence will be
     *            written
     * @param <T>
     *            A subtype of {@code Appendable}, returned to the caller
     *            for chaining purposes
     * @return A String correctly formatted for insertion in a JSON text.
     * @throws IOException there was a problem writing to the Appendable
     */
    static <T extends Appendable> T quote(CharSequence string, T w) throws IOException {
        if (string == null || string.length() == 0) {
            w.append("\"\"");
            return w;
        }

        char b;
        char c = 0;
        String hhhh;
        int i;
        int len = string.length();
        int prev = 0;

        w.append('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    if(prev < i) {
                        w.append(string, prev, i);
                    }
                    w.append('\\');
                    prev = i;
                    break;
                case '/':
                    if (b == '<') {
                        if(prev < i) {
                            w.append(string, prev, i);
                        }
                        w.append('\\');
                        prev = i;
                    }
                    break;
                case '\b':
                    if(prev < i) {
                        w.append(string, prev, i);
                    }
                    w.append("\\b");
                    prev = i + 1;
                    break;
                case '\t':
                    if(prev < i) {
                        w.append(string, prev, i);
                    }
                    w.append("\\t");
                    prev = i + 1;
                    break;
                case '\n':
                    if(prev < i) {
                        w.append(string, prev, i);
                    }
                    w.append("\\n");
                    prev = i + 1;
                    break;
                case '\f':
                    if(prev < i) {
                        w.append(string, prev, i);
                    }
                    w.append("\\f");
                    prev = i + 1;
                    break;
                case '\r':
                    if(prev < i) {
                        w.append(string, prev, i);
                    }
                    w.append("\\r");
                    prev = i + 1;
                    break;
                default:
                    if (c < ' ' || (c >= '\u0080' && c < '\u00a0')
                            || (c >= '\u2000' && c < '\u2100')) {
                        if(prev < i) {
                            w.append(string, prev, i);
                        }
                        hhhh = Integer.toHexString(c);
                        w.append("\\u0000", 0, 6 - hhhh.length());
                        w.append(hhhh);
                        prev = i + 1;
                    }
            }
        }
        if(prev == 0) {
            w.append(string);
        } else if(prev < len) {
            w.append(string, prev, len);
        }
        w.append('"');
        return w;
    }

    static <T extends Appendable> T writeBean(Object bean, T writer,
            int indentFactor, int indent) throws JSONException {
        try {
            final int newindent = indent + indentFactor;
            Class<?> klass = bean.getClass();

            // If klass is a System class then set includeSuperClass to false.
            boolean includeSuperClass = klass.getClassLoader() != null;
            Method[] methods = includeSuperClass ? klass.getMethods()
                    : klass.getDeclaredMethods();
            boolean commanate = false;

            writer.append('{');
            for (int i = 0; i < methods.length; i += 1) {
                try {
                    Method method = methods[i];
                    if (Modifier.isPublic(method.getModifiers()) &&
                            !Modifier.isStatic(method.getModifiers()) &&
                            !method.isSynthetic() &&
                            (method.getReturnType() != Void.TYPE)) {
                        String name = method.getName();
                        String key = JSONObject.keyFromMethodName(name);
                        if ((key != null)
                                && (method.getParameterTypes().length == 0)) {
                            Object result = method.invoke(bean, (Object[]) null);
                            if (result != null) {
                                if (commanate) {
                                    writer.append(',');
                                }
                                if (indentFactor > 0) {
                                    writer.append('\n');
                                }
                                indent(writer, newindent);
                                quote(String.valueOf(key), writer);
                                writer.append(':');
                                if (indentFactor > 0) {
                                    writer.append(' ');
                                }
                                writeValue(writer, result, indentFactor, newindent);
                                commanate = true;
                            }
                        }
                    }
                } catch (Exception ignore) {
                }
            }
            if(commanate) {
                if (indentFactor > 0) {
                    writer.append('\n');
                }
                indent(writer, indent);
            }
            writer.append('}');
            return writer;
        } catch (IOException exception) {
            throw new JSONException(exception);
        } catch (RuntimeException exception) {
            throw new JSONException(exception);
        }
    }

    static <T extends Appendable> T writeMap(Map<?, ?> map, T writer,
            int indentFactor, int indent) throws JSONException {
        try {
            boolean commanate = false;
            final int length = map.size();
            Iterator<?> keys = map.keySet().iterator();
            writer.append('{');

            if (length == 1) {
                Object key = keys.next();
                quote(String.valueOf(key), writer);
                writer.append(':');
                if (indentFactor > 0) {
                    writer.append(' ');
                }
                writeValue(writer, map.get(key), indentFactor, indent);
            } else if (length != 0) {
                final int newindent = indent + indentFactor;
                while (keys.hasNext()) {
                    Object key = keys.next();
                    if (commanate) {
                        writer.append(',');
                    }
                    if (indentFactor > 0) {
                        writer.append('\n');
                    }
                    indent(writer, newindent);
                    quote(String.valueOf(key), writer);
                    writer.append(':');
                    if (indentFactor > 0) {
                        writer.append(' ');
                    }
                    writeValue(writer, map.get(key), indentFactor, newindent);
                    commanate = true;
                }
                if (indentFactor > 0) {
                    writer.append('\n');
                }
                indent(writer, indent);
            }
            writer.append('}');
            return writer;
        } catch (IOException exception) {
            throw new JSONException(exception);
        }
    }

    private static boolean singleIterableElement(Iterable<?> iterable) {
        if(iterable instanceof Collection) {
            return ((Collection)iterable).size() == 1;
        }
        Iterator<?> iterator = iterable.iterator();
        if(!iterator.hasNext()) {
            return false;
        }
        iterator.next();
        return !iterator.hasNext();
    }

    static <T extends Appendable> T writeIterable(Iterable<?> collection, T writer,
            int indentFactor, int indent) throws JSONException {
        try {
            boolean singleElement = singleIterableElement(collection);
            Iterator<?> iterator = collection.iterator();
            boolean commanate = false;
            writer.append('[');

            if ((singleElement) && (iterator.hasNext())) {
                writeValue(writer, iterator.next(),
                        indentFactor, indent);
            } else if (iterator.hasNext()) {
                final int newindent = indent + indentFactor;

                while (iterator.hasNext()) {
                    if (commanate) {
                        writer.append(',');
                    }
                    if (indentFactor > 0) {
                        writer.append('\n');
                    }
                    indent(writer, newindent);
                    writeValue(writer, iterator.next(),
                            indentFactor, newindent);
                    commanate = true;
                }
                if (indentFactor > 0) {
                    writer.append('\n');
                }
                indent(writer, indent);
            }
            writer.append(']');
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    static <T extends Appendable> T writeArray(Object array, T writer,
            int indentFactor, int indent) throws JSONException {
        try {
            final int length = Array.getLength(array);
            boolean commanate = false;
            writer.append('[');

            if (length == 1) {
                writeValue(writer, Array.get(array, 0),
                        indentFactor, indent);
            } else if (length != 0) {
                final int newindent = indent + indentFactor;

                for (int i = 0; i < length; i += 1) {
                    if (commanate) {
                        writer.append(',');
                    }
                    if (indentFactor > 0) {
                        writer.append('\n');
                    }
                    indent(writer, newindent);
                    writeValue(writer, Array.get(array, i),
                            indentFactor, newindent);
                    commanate = true;
                }
                if (indentFactor > 0) {
                    writer.append('\n');
                }
                indent(writer, indent);
            }
            writer.append(']');
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Write the contents of the JSONObject as JSON text to a writer. For
     * compactness, no whitespace is added.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param writer
     *            Writes the serialized JSON
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @param indent
     *            The indention of the top level.
     * @param <T>
     *            A subtype of {@code Appendable}, returned to the caller
     *            for chaining purposes
     * @return The writer.
     * @throws JSONException
     */
    static <T extends Appendable> T writeJSONObject(JSONObject object, T writer, int indentFactor, int indent)
            throws JSONException {
        try {
            boolean commanate = false;
            final int length = object.length();
            Iterator<String> keys = object.keys();
            writer.append('{');

            if (length == 1) {
                String key = keys.next();
                quote(key, writer);
                writer.append(':');
                if (indentFactor > 0) {
                    writer.append(' ');
                }
                writeValue(writer, object.opt(key), indentFactor, indent);
            } else if (length != 0) {
                final int newindent = indent + indentFactor;
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (commanate) {
                        writer.append(',');
                    }
                    if (indentFactor > 0) {
                        writer.append('\n');
                    }
                    indent(writer, newindent);
                    quote(key, writer);
                    writer.append(':');
                    if (indentFactor > 0) {
                        writer.append(' ');
                    }
                    writeValue(writer, object.opt(key), indentFactor, newindent);
                    commanate = true;
                }
                if (indentFactor > 0) {
                    writer.append('\n');
                }
                indent(writer, indent);
            }
            writer.append('}');
            return writer;
        } catch (IOException exception) {
            throw new JSONException(exception);
        }
    }

    /**
     * Write the contents of the JSONArray as JSON text to a writer. For
     * compactness, no whitespace is added.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param writer
     *            Writes the serialized JSON
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @param indent
     *            The indention of the top level.
     * @param <T> a subtype of {@code Appendable}, returned to the caller
     *            for chaining purposes
     * @return The writer.
     * @throws JSONException
     */
    static <T extends Appendable> T writeJSONArray(JSONArray array, T writer, int indentFactor, int indent)
            throws JSONException {
        try {
            boolean commanate = false;
            int length = array.length();
            writer.append('[');

            if (length == 1) {
                writeValue(writer, array.get(0), indentFactor, indent);
            } else if (length != 0) {
                final int newindent = indent + indentFactor;

                for (int i = 0; i < length; i += 1) {
                    if (commanate) {
                        writer.append(',');
                    }
                    if (indentFactor > 0) {
                        writer.append('\n');
                    }
                    indent(writer, newindent);
                    writeValue(writer, array.get(i), indentFactor, newindent);
                    commanate = true;
                }
                if (indentFactor > 0) {
                    writer.append('\n');
                }
                indent(writer, indent);
            }
            writer.append(']');
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Write the given double to the given Appendable.
     *
     * @param writer
     *            The Appendable to which the double value is written
     * @param d
     *            A double
     * @param <T> subtype of Appendable, returned to the caller
     * @return the given Appendable
     * @throws JSONException there was a problem writing the double
     */
    static <T extends Appendable> T writeDouble(T writer, double d) throws JSONException {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            try {
                writer.append("null");
                return writer;
            } catch (IOException e) {
                throw new JSONException(e);
            }
        }

        // Shave off trailing zeros and decimal point, if possible.
        String string = Double.toString(d);
        writeNumberDigits(writer, string);
        return writer;
    }

    /**
     * Write the given number to the given Appendable.
     *
     * @param writer
     *            The Appendable to which the number value is written
     * @param number
     *            A Number
     * @param <T> subtype of Appendable, returned to the caller
     * @return the given Appendable
     * @throws JSONException there was a problem writing the number
     */
    static <T extends Appendable> T writeNumber(T writer, Number number) throws JSONException {
        if (number == null) {
            throw new JSONException("Null pointer");
        }
        JSONObject.testValidity(number);

        // Shave off trailing zeros and decimal point, if possible.
        String string = number.toString();
        writeNumberDigits(writer, string);
        return writer;
    }

    /**
     * Write a number value, trimming any trailing zero digits from a real number.
     * If all zeros appear immediately after a decimal, the decimal is omitted
     * as well.
     *
     * @param writer the Appendable to which the digits will be written
     * @param string the string of digits
     */
    private static void writeNumberDigits(Appendable writer, String string) throws JSONException {
        try {
            if (string.indexOf('.') > 0 && string.indexOf('e') < 0
                    && string.indexOf('E') < 0) {
                final int len = string.length();
                int last = len;
                while ((last > 0) && (string.charAt(last - 1) == '0')) {
                    last--;
                }
                if ((last > 0) && (string.charAt(last - 1) == '.')) {
                    last--;
                }
                if (last < len) {
                    writer.append(string, 0, last);
                    return;
                }
            }
            writer.append(string);
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }
}
