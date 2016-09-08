package org.json;

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

import org.json.util.ALStack;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
                writeString(string, this.writer).append(':');
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
     * Append a <code>null</code> value.
     * @return this
     * @throws JSONException there was a problem writing the null value
     */
    public JSONWriter nullValue() throws JSONException {
        this.prepValue();
        writeNull(this.writer);
        return this;
    }

    /**
     * Append either the value <code>true</code> or the value
     * <code>false</code>.
     * @param b A boolean.
     * @return this
     * @throws JSONException there was a problem writing the boolean value
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
        writeDouble(d, this.writer);
        return this;
    }

    /**
     * Append a long value.
     * @param l A long.
     * @return this
     * @throws JSONException there was a problem writing the long value
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
        writeValue(object, this.writer);
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
            writeValue(obj, this.writer);
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
    public JSONWriter entries(Map<?, ?> kvPairs) throws JSONException {
        for(Entry<?, ?> entry : kvPairs.entrySet()) {
            this.key(String.valueOf(entry.getKey()));
            this.prepValue();
            writeValue(entry.getValue(), this.writer);
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

    // 120 spaces, divides by 1, 2, 3, 4, 5, 6, 8, 10, 12, 15, 20, ...
    private static final String PADDING_SPACES = makePaddingSpaces();
    private static final int PADDING_LENGTH = 120;

    private static String makePaddingSpaces() {
        char[] padding = new char[PADDING_LENGTH];
        Arrays.fill(padding, ' ');
        return String.valueOf(padding);
    }

    /**
     * Indent by the given number of spaces.
     *
     * @param indent
     *            the number of character to indent.
     * @param writer
     *            the writer.
     * @param <T>
     *            A subtype of {@code Appendable}, returned to the caller
     *            for chaining purposes
     * @return The writer.
     * @throws IOException there was a problem writing the indentation
     */
    static <T extends Appendable> T indent(int indent, T writer) throws IOException {

        while(indent >= PADDING_LENGTH) {
            writer.append(PADDING_SPACES);
            indent -= PADDING_LENGTH;
        }
        if(indent > 0) {
            writer.append(PADDING_SPACES, 0, indent);
        }
        return writer;
    }

    /**
     * Write the contents of the Object as JSON text to a writer. For
     * compactness, no whitespace is added.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param value
     *            The value to be written
     * @param writer
     *            Writes the serialized JSON
     * @param <T>
     *            A subtype of {@code Appendable}, returned to the caller
     *            for chaining purposes
     * @return The writer.
     * @throws JSONException there was a problem writing the Object
     */
    static <T extends Appendable> T writeValue(Object value, T writer)
            throws JSONException {
        return writeValue(value, writer, 0, 0);
    }

    /**
     * Write the contents of the Object as JSON text to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param value
     *            The value to be written
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
     * @throws JSONException there was a problem writing the Object
     */
    static <T extends Appendable> T writeValue(Object value, T writer,
            int indentFactor, int indent) throws JSONException {

        if ((value == null) || JSONObject.NULL.equals(value)) {
            return writeNull(writer);
        }
        if (value instanceof JSONAppendable) {
            return writeJSONAppendable((JSONAppendable) value, writer);
        }
        if (value instanceof JSONString) {
            return writeJSONString((JSONString) value, writer);
        }
        if (value instanceof JSONObject) {
            return writeJSONObject((JSONObject) value, writer, indentFactor, indent);
        }
        if (value instanceof JSONArray) {
            return writeJSONArray((JSONArray) value, writer, indentFactor, indent);
        }
        if (value instanceof Number) {
            return writeNumber((Number) value, writer);
        }
        if (value instanceof Boolean) {
            return writeBoolean((Boolean) value, writer);
        }
        if (value instanceof CharSequence) {
            return writeString((CharSequence) value, writer);
        }
        if (value instanceof Map) {
            return writeMap((Map<?, ?>) value, writer, indentFactor, indent);
        }
        if (value instanceof Iterable) {
            return writeIterable((Iterable<?>) value, writer, indentFactor, indent);
        }
        if (value.getClass().isArray()) {
            return writeArray(value, writer, indentFactor, indent);
        }
        if (value instanceof Enum<?>) {
            return writeEnum((Enum<?>) value, writer);
        }
        if (JSONObject.objectIsBean(value)) {
            return writeBean(value, writer, indentFactor, indent);
        }
        return writeString(value.toString(), writer);
    }

    /**
     * Write the JSONAppendable as a JSON value to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param value
     *            The JSONString to be written
     * @param writer
     *            Writes the serialized JSON
     * @param <T> a subtype of {@code Appendable}, returned to the caller
     *            for chaining purposes
     * @return The writer.
     * @throws JSONException there was a problem writing the JSONAppendable
     */
    static <T extends Appendable> T writeJSONAppendable(JSONAppendable value,
            T writer) throws JSONException {
        try {
            value.appendJSON(writer);
            return writer;
        } catch(JSONException e) {
            // Propagate directly, because JSONException is a RuntimeException
            throw e;
        } catch(IOException e) {
            throw new JSONException(e);
        } catch(RuntimeException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Write the contents of the JSONString as a JSON value to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param value
     *            The JSONString to be written
     * @param writer
     *            Writes the serialized JSON
     * @param <T> a subtype of {@code Appendable}, returned to the caller
     *            for chaining purposes
     * @return The writer.
     * @throws JSONException there was a problem writing the JSONString
     */
    static <T extends Appendable> T writeJSONString(JSONString value, T writer)
            throws JSONException {
        try {
            String o = value.toJSONString();
            if (o != null) {
                writer.append(o);
            } else {
                writeString(value.toString(), writer);
            }
            return writer;
        } catch(JSONException e) {
            // Propagate directly, because JSONException is a RuntimeException
            throw e;
        } catch (IOException e) {
            throw new JSONException(e);
        } catch (RuntimeException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Write the contents of the JavaBean as JSON object to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param bean
     *            The bean to be written
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
     * @throws JSONException there was a problem writing the bean
     */
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
                                indent(newindent, writer);
                                writeString(String.valueOf(key), writer).append(':');
                                if (indentFactor > 0) {
                                    writer.append(' ');
                                }
                                writeValue(result, writer, indentFactor, newindent);
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
                indent(indent, writer);
            }
            writer.append('}');
            return writer;
        } catch (IOException exception) {
            throw new JSONException(exception);
        } catch (RuntimeException exception) {
            throw new JSONException(exception);
        }
    }

    /**
     * Write the contents of the Map as JSON object to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param map
     *            The Map to be written
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
     * @throws JSONException there was a problem writing the Map
     */
    static <T extends Appendable> T writeMap(Map<?, ?> map, T writer,
            int indentFactor, int indent) throws JSONException {
        try {
            boolean commanate = false;
            final int length = map.size();
            Iterator<?> keys = map.keySet().iterator();
            writer.append('{');

            if (length == 1) {
                Object key = keys.next();
                writeString(String.valueOf(key), writer).append(':');
                if (indentFactor > 0) {
                    writer.append(' ');
                }
                writeValue(map.get(key), writer, indentFactor, indent);
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
                    indent(newindent, writer);
                    writeString(String.valueOf(key), writer).append(':');
                    if (indentFactor > 0) {
                        writer.append(' ');
                    }
                    writeValue(map.get(key), writer, indentFactor, newindent);
                    commanate = true;
                }
                if (indentFactor > 0) {
                    writer.append('\n');
                }
                indent(indent, writer);
            }
            writer.append('}');
            return writer;
        } catch (IOException exception) {
            throw new JSONException(exception);
        }
    }

    /**
     * Determine whether this Iterable has exactly one element. If the
     * Iterable is a Collection, just check the {@code size()} method.
     * Otherwise, start iterating until we determine whether there is
     * more than one element.
     *
     * @param iterable the Iterable
     * @return {@code true} if there is exactly one element, otherwise
     * {@code false}
     */
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

    /**
     * Write the contents of the Iterable as JSON array to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param collection
     *            The Iterable to be written
     * @param writer
     *            Writes the serialized JSON
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @param indent
     *            The indention of the top level.
     * @param <T> a subtype of {@code Appendable}, returned to the caller
     *            for chaining purposes
     * @return The writer.
     * @throws JSONException there was a problem writing the Iterable
     */
    static <T extends Appendable> T writeIterable(Iterable<?> collection, T writer,
            int indentFactor, int indent) throws JSONException {
        try {
            boolean singleElement = singleIterableElement(collection);
            Iterator<?> iterator = collection.iterator();
            boolean commanate = false;
            writer.append('[');

            if ((singleElement) && (iterator.hasNext())) {
                writeValue(iterator.next(), writer,
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
                    indent(newindent, writer);
                    writeValue(iterator.next(), writer,
                            indentFactor, newindent);
                    commanate = true;
                }
                if (indentFactor > 0) {
                    writer.append('\n');
                }
                indent(indent, writer);
            }
            writer.append(']');
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Write the contents of the array as JSON array to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param array
     *            The array to be written
     * @param writer
     *            Writes the serialized JSON
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @param indent
     *            The indention of the top level.
     * @param <T> a subtype of {@code Appendable}, returned to the caller
     *            for chaining purposes
     * @return The writer.
     * @throws JSONException there was a problem writing the array
     */
    static <T extends Appendable> T writeArray(Object array, T writer,
            int indentFactor, int indent) throws JSONException {
        try {
            final int length = Array.getLength(array);
            boolean commanate = false;
            writer.append('[');

            if (length == 1) {
                writeValue(Array.get(array, 0), writer,
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
                    indent(newindent, writer);
                    writeValue(Array.get(array, i), writer,
                            indentFactor, newindent);
                    commanate = true;
                }
                if (indentFactor > 0) {
                    writer.append('\n');
                }
                indent(indent, writer);
            }
            writer.append(']');
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Write the given Enum to the given Appendable as a String.
     *
     * @param <T> subtype of Appendable, returned to the caller
     * @param value
     *            An Enum
     * @param writer
     *            The Appendable to which the Enum value is written
     * @return the given Appendable
     * @throws JSONException there was a problem writing the Enum
     */
    static <T extends Appendable> T writeEnum(Enum<?> value, T writer)
            throws JSONException {
        return writeString(value.name(), writer);
    }

    /**
     * Write the contents of the JSONObject as JSON object to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param object
     *            The JSONObject to be written
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
     * @throws JSONException there was a problem writing the JSONObject
     */
    static <T extends Appendable> T writeJSONObject(JSONObject object, T writer,
            int indentFactor, int indent) throws JSONException {
        try {
            boolean commanate = false;
            final int length = object.length();
            Iterator<String> keys = object.keys();
            writer.append('{');

            if (length == 1) {
                String key = keys.next();
                writeString(key, writer).append(':');
                if (indentFactor > 0) {
                    writer.append(' ');
                }
                writeValue(object.opt(key), writer, indentFactor, indent);
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
                    indent(newindent, writer);
                    writeString(key, writer).append(':');
                    if (indentFactor > 0) {
                        writer.append(' ');
                    }
                    writeValue(object.opt(key), writer, indentFactor, newindent);
                    commanate = true;
                }
                if (indentFactor > 0) {
                    writer.append('\n');
                }
                indent(indent, writer);
            }
            writer.append('}');
            return writer;
        } catch (IOException exception) {
            throw new JSONException(exception);
        }
    }

    /**
     * Write the contents of the JSONArray as JSON array to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param array
     *            The JSONArray to be written
     * @param writer
     *            Writes the serialized JSON
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @param indent
     *            The indention of the top level.
     * @param <T> a subtype of {@code Appendable}, returned to the caller
     *            for chaining purposes
     * @return The writer.
     * @throws JSONException there was a problem writing the JSONArray
     */
    static <T extends Appendable> T writeJSONArray(JSONArray array, T writer,
            int indentFactor, int indent) throws JSONException {
        try {
            boolean commanate = false;
            int length = array.length();
            writer.append('[');

            if (length == 1) {
                writeValue(array.get(0), writer, indentFactor, indent);
            } else if (length != 0) {
                final int newindent = indent + indentFactor;

                for (int i = 0; i < length; i += 1) {
                    if (commanate) {
                        writer.append(',');
                    }
                    if (indentFactor > 0) {
                        writer.append('\n');
                    }
                    indent(newindent, writer);
                    writeValue(array.get(i), writer, indentFactor, newindent);
                    commanate = true;
                }
                if (indentFactor > 0) {
                    writer.append('\n');
                }
                indent(indent, writer);
            }
            writer.append(']');
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
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
     * @throws JSONException there was a problem writing to the Appendable
     */
    static <T extends Appendable> T writeString(CharSequence string, T w)
            throws JSONException {
        try {
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
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Write the given double to the given Appendable.
     *
     * @param <T> subtype of Appendable, returned to the caller
     * @param d
     *            A double
     * @param writer
     *            The Appendable to which the double value is written
     * @return the given Appendable
     * @throws JSONException there was a problem writing the double
     */
    static <T extends Appendable> T writeDouble(double d, T writer)
            throws JSONException {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            return writeNull(writer);
        }

        // Shave off trailing zeros and decimal point, if possible.
        String string = Double.toString(d);
        return writeNumberDigits(string, writer);
    }

    /**
     * Write the given number to the given Appendable.
     *
     * @param <T> subtype of Appendable, returned to the caller
     * @param number
     *            A Number
     * @param writer
     *            The Appendable to which the number value is written
     * @return the given Appendable
     * @throws JSONException there was a problem writing the number
     */
    static <T extends Appendable> T writeNumber(Number number, T writer)
            throws JSONException {
        if (number instanceof Double) {
            if (((Double) number).isInfinite() || ((Double) number).isNaN()) {
                return writeNull(writer);
            }
        } else if (number instanceof Float) {
            if (((Float) number).isInfinite() || ((Float) number).isNaN()) {
                return writeNull(writer);
            }
        }

        // Shave off trailing zeros and decimal point, if possible.
        String string = number.toString();
        return writeNumberDigits(string, writer);
    }

    /**
     * Write a number value, trimming any trailing zero digits from a real number.
     * If all zeros appear immediately after a decimal, the decimal is omitted
     * as well.
     *
     * @param string the string of digits
     * @param writer the Appendable to which the digits will be written
     * @throws JSONException there was a problem writing to the Appendable
     */
    private static <T extends Appendable> T writeNumberDigits(String string,
            T writer) throws JSONException {
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
                    return writer;
                }
            }
            writer.append(string);
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Write the given Boolean to the given Appendable.
     *
     * @param <T> subtype of Appendable, returned to the caller
     * @param value
     *            A Boolean
     * @param writer
     *            The Appendable to which the boolean value is written
     * @return the given Appendable
     * @throws JSONException there was a problem writing the boolean
     */
    static <T extends Appendable> T writeBoolean(Boolean value, T writer)
            throws JSONException {
        try {
            writer.append(value.toString());
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Write the value Null to the given Appendable.
     *
     * @param <T> subtype of Appendable, returned to the caller
     * @param writer
     *            The Appendable to which the null value is written
     * @return the given Appendable
     * @throws JSONException there was a problem writing null
     */
    static <T extends Appendable> T writeNull(T writer) throws JSONException {
        try {
            writer.append("null");
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }
}
