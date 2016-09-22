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
import org.json.util.JSONPointerUtils;
import org.json.write.CompactWriterFactory;
import org.json.write.NullStructureWriter;
import org.json.write.PrettyWriterFactory;
import org.json.write.SimpleStructureWriter;
import org.json.write.StructureWriter;
import org.json.write.StructureWriterFactory;
import org.json.write.WriterUtil;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * JSONWriter provides a quick and convenient way of producing JSON text.
 * The texts produced strictly conform to JSON syntax rules. By default,
 * no whitespace is added, so the results are ready for transmission or storage.
 * Each instance of JSONWriter can produce one JSON text.
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
 * The first method called must be <code>array</code> or <code>object</code>,
 * unless this object has been constructed with {@code allowSimpleValues}
 * set to true.
 * <p>
 * There are no methods for adding commas or colons. JSONWriter adds them for
 * you. Objects and arrays can be nested arbitrarily deep.
 * <p>
 * This can be less memory intensive than using a JSONObject to build a string.
 *
 * @author JSON.org
 * @version 2016-09-18
 */
public class JSONWriter implements Closeable {
    private static final int initdepth = 16;

    /**
     * Factory for creating strategy objects for writing a particular structure.
     */
    private final StructureWriterFactory factory;

    /**
     * Strategy for writing JSON structures, parameterizes writing of
     * JSON Objects and JSON Arrays, as well as pretty printing or compact.
     */
    private StructureWriter currentStructure;

    /**
     * Indicate when writing has finished.
     */
    protected boolean done;

    /**
     * The object/array stack.
     */
    private final ALStack<StructureWriter> stack;

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
        this.done = false;
        this.stack = new ALStack<StructureWriter>(initdepth);
        this.writer = w;
        this.factory = CompactWriterFactory.INSTANCE;
        this.currentStructure = NullStructureWriter.getInstance();
    }

    /**
     * Make a fresh JSONWriter. It can be used to build one JSON text.
     *
     * @param w the Writer to which JSON content will be written
     * @param allowSimpleValues {@code true} to allow the root value to be
     * a simple value, otherwise {@code false} to only allow objects and arrays
     * at the root level
     */
    public JSONWriter(Appendable w, boolean allowSimpleValues) {
        this.done = false;
        this.stack = new ALStack<StructureWriter>(initdepth);
        this.writer = w;
        this.factory = CompactWriterFactory.INSTANCE;
        this.currentStructure = allowSimpleValues ?
                SimpleStructureWriter.getInstance(0) :
                NullStructureWriter.getInstance();
    }

    /**
     * Make a fresh JSONWriter with the given indent factor.
     * It can be used to build one JSON text with pretty-printing.
     *
     * @param w the Writer to which JSON content will be written
     * @param indentFactor indent level, or 0 for compact output
     */
    public JSONWriter(Appendable w, int indentFactor) {
        this.done = false;
        this.stack = new ALStack<StructureWriter>(initdepth);
        this.writer = w;
        this.factory = (indentFactor > 0)
                ? new PrettyWriterFactory(indentFactor)
                : CompactWriterFactory.INSTANCE;
        this.currentStructure = NullStructureWriter.getInstance();
    }

    /**
     * Make a fresh JSONWriter with the given indent factor and initial
     * indentation. It can be used to build one JSON text with pretty-printing.
     *
     * @param w the Writer to which JSON content will be written
     * @param indentFactor indent level, or 0 for compact output
     * @param indent initial indent
     */
    public JSONWriter(Appendable w, int indentFactor, int indent) {
        this.done = false;
        this.stack = new ALStack<StructureWriter>(initdepth);
        this.writer = w;
        this.factory = (indentFactor > 0)
                ? new PrettyWriterFactory(indentFactor, indent)
                : CompactWriterFactory.INSTANCE;
        this.currentStructure = NullStructureWriter.getInstance();
    }

    /**
     * Make a fresh JSONWriter with the given indent factor and initial
     * indentation. It can be used to build one JSON text with pretty-printing.
     *
     * @param w the Writer to which JSON content will be written
     * @param indentFactor indent level, or 0 for compact output
     * @param indent initial indent
     * @param allowSimpleValues {@code true} to allow the root value to be
     * a simple value, otherwise {@code false} to only allow objects and arrays
     * at the root level
     */
    public JSONWriter(Appendable w, int indentFactor, int indent,
            boolean allowSimpleValues) {
        this.done = false;
        this.stack = new ALStack<StructureWriter>(initdepth);
        this.writer = w;
        this.factory = (indentFactor > 0)
                ? new PrettyWriterFactory(indentFactor, indent)
                : CompactWriterFactory.INSTANCE;
        this.currentStructure = allowSimpleValues ?
                SimpleStructureWriter.getInstance(indent) :
                NullStructureWriter.getInstance();
    }

    private String getLocation() {
        return JSONPointerUtils.toJSONPointer(stack);
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
        if (done) {
            throw new JSONWriterException("Misplaced array.", getLocation());
        }
        currentStructure.subValue(writer);
        currentStructure = factory.newArrayWriter(currentStructure);
        stack.push(currentStructure);
        currentStructure.beginStructure(writer);
        return this;
    }

    /**
     * End an array. This method most be called to balance calls to
     * <code>array</code>.
     * @return this
     * @throws JSONException If incorrectly nested.
     */
    public JSONWriter endArray() throws JSONException {
        if(done || currentStructure.getStructureType() != 'a') {
            throw new JSONWriterException("Misplaced endArray.", getLocation());
        }
        try {
            done = currentStructure.endStructure(writer);
            stack.pop();
            if (stack.isEmpty()) {
                currentStructure = null;
                done = true;
            } else {
                currentStructure = stack.peek();
            }
        } catch (JSONException e) {
            throw new JSONWriterException(e, getLocation());
        } catch (EmptyStackException e) {
            throw new JSONWriterException(e, getLocation());
        }
        return this;
    }

    /**
     * End an object. This method most be called to balance calls to
     * <code>object</code>.
     * @return this
     * @throws JSONException If incorrectly nested.
     */
    public JSONWriter endObject() throws JSONException {
        if(done || currentStructure.getStructureType() != 'o') {
            throw new JSONWriterException("Misplaced endObject.", getLocation());
        }
        try {
            done = currentStructure.endStructure(writer);
            stack.pop();
            if (stack.isEmpty()) {
                currentStructure = null;
                done = true;
            } else {
                currentStructure = stack.peek();
            }
        } catch (JSONException e) {
            throw new JSONWriterException(e, getLocation());
        } catch (EmptyStackException e) {
            throw new JSONWriterException(e, getLocation());
        }
        return this;
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
        if(done) {
            throw new JSONWriterException("Misplaced key.", getLocation());
        }
        try {
            currentStructure.key(string, writer);
        } catch (JSONException e) {
            throw new JSONWriterException(e, getLocation());
        }
        return this;
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
        if(done) {
            throw new JSONWriterException("Misplaced object.", getLocation());
        }
        try {
            currentStructure.subValue(writer);
            currentStructure = factory.newObjectWriter(currentStructure);
            stack.push(currentStructure);
            currentStructure.beginStructure(writer);
        } catch (JSONException e) {
            throw new JSONWriterException(e, getLocation());
        }
        return this;
    }


    /**
     * Append a <code>null</code> value.
     * @return this
     * @throws JSONException there was a problem writing the null value
     */
    public JSONWriter nullValue() throws JSONException {
        if (done) {
            throw new JSONWriterException("Misplaced value.", getLocation());
        }
        try {
            done = currentStructure.nullValue(writer);
        } catch (JSONException e) {
            throw new JSONWriterException(e, getLocation());
        }
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
        if (done) {
            throw new JSONWriterException("Misplaced value.", getLocation());
        }
        try {
            done = currentStructure.booleanValue(b, writer);
        } catch (JSONException e) {
            throw new JSONWriterException(e.getMessage(), e, getLocation());
        }
        return this;
    }

    /**
     * Append a double value.
     * @param d A double.
     * @return this
     * @throws JSONException If the number is not finite.
     */
    public JSONWriter value(double d) throws JSONException {
        if (done) {
            throw new JSONWriterException("Misplaced value.", getLocation());
        }
        try {
            done = currentStructure.doubleValue(d, writer);
        } catch (JSONException e) {
            throw new JSONWriterException(e.getMessage(), e, getLocation());
        }
        return this;
    }

    /**
     * Append a long value.
     * @param l A long.
     * @return this
     * @throws JSONException there was a problem writing the long value
     */
    public JSONWriter value(long l) throws JSONException {
        if (done) {
            throw new JSONWriterException("Misplaced value.", getLocation());
        }
        try {
            done = currentStructure.longValue(l, writer);
        } catch (JSONException e) {
            throw new JSONWriterException(e, getLocation());
        }
        return this;
    }

    /**
     * Append an object value.
     * @param object The object to appendString. It can be null, or a Boolean, Number,
     *   String, JSONObject, or JSONArray, or an object that implements JSONString.
     * @return this
     * @throws JSONException If the value is out of sequence.
     */
    public JSONWriter value(Object object) throws JSONException {
        if (done) {
            throw new JSONWriterException("Misplaced value.", getLocation());
        }
        try {
            done = writeValue(object);
        } catch (JSONWriterException e) {
            throw e; // propagate
        } catch (JSONException e) {
            throw new JSONWriterException(e, getLocation());
        }
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
        if (done) {
            throw new JSONWriterException("Misplaced value.", getLocation());
        }
        try {
            for(Object obj : values) {
                writeValue(obj);
            }
        } catch (JSONWriterException e) {
            throw e; // propagate
        } catch (JSONException e) {
            throw new JSONWriterException(e, getLocation());
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
        if (done) {
            throw new JSONWriterException("Misplaced value.", getLocation());
        }
        try {
            for(Entry<?, ?> entry : kvPairs.entrySet()) {
                currentStructure.key(String.valueOf(entry.getKey()), writer);
                writeValue(entry.getValue());
            }
        } catch (JSONWriterException e) {
            throw e; // propagate
        } catch (JSONException e) {
            throw new JSONWriterException(e, getLocation());
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
        if(!done) {
            throw new IOException("JSON stack is not empty");
        }
        if(writer instanceof Closeable) {
            ((Closeable)writer).close();
        }
    }


    /**
     * Write the contents of the Object as JSON text to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param value
     *            The value to be written
     * @throws JSONException there was a problem writing the Object
     */
    private boolean writeValue(Object value) throws JSONException {

        if(WriterUtil.isSimpleValue(value)) {
            return currentStructure.simpleValue(value, writer);
        }
        if (value instanceof JSONAppendable) {
            return jsonAppendableValue((JSONAppendable) value);
        }
        if (value instanceof JSONString) {
            return jsonStringValue((JSONString) value);
        }
        if (value instanceof JSONObject) {
            return jsonObjectValue((JSONObject) value);
        }
        if (value instanceof JSONArray) {
            return jsonArrayValue((JSONArray) value);
        }
        if (value instanceof Map) {
            return mapValue((Map<?, ?>) value);
        }
        if (value instanceof Iterable) {
            return iterableValue((Iterable<?>) value);
        }
        if (value.getClass().isArray()) {
            return arrayValue(value);
        }
        if (JSONObject.objectIsBean(value)) {
            return beanValue(value);
        }
        return currentStructure.simpleValue(value.toString(), writer);
    }

    /**
     * Write the JSONAppendable as a JSON value to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param value
     *            The JSONString to be written
     * @throws JSONException there was a problem writing the JSONAppendable
     */
    private boolean jsonAppendableValue(JSONAppendable value) throws JSONException {
        try {
            return currentStructure.jsonAppendableValue(value, writer);
        } catch(JSONException e) {
            // Propagate directly, because JSONException is a RuntimeException
            throw e;
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
     * @throws JSONException there was a problem writing the JSONString
     */
    private boolean jsonStringValue(JSONString value) throws JSONException {
        try {
            return currentStructure.jsonStringValue(value, writer);
        } catch(JSONException e) {
            // Propagate directly, because JSONException is a RuntimeException
            throw e;
        } catch (RuntimeException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Write the contents of the JSONObject as JSON object to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param object
     *            The JSONObject to be written
     * @throws JSONException there was a problem writing the JSONObject
     */
    private boolean jsonObjectValue(JSONObject object) throws JSONException {
        Iterator<String> keys = object.keys();
        object();
        while (keys.hasNext()) {
            String key = keys.next();
            key(key).writeValue(object.opt(key));
        }
        endObject();
        return stack.isEmpty();
    }

    /**
     * Write the contents of the JSONArray as JSON array to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param array
     *            The JSONArray to be written
     * @throws JSONException there was a problem writing the JSONArray
     */
    private boolean jsonArrayValue(JSONArray array) throws JSONException {
        final int length = array.length();
        array();
        if (length != 0) {
            for (int i = 0; i < length; i += 1) {
                writeValue(array.get(i));
            }
        }
        endArray();
        return stack.isEmpty();
    }

    /**
     * Write the contents of the Map as JSON object to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param map
     *            The Map to be written
     * @throws JSONException there was a problem writing the Map
     */
    private boolean mapValue(Map<?, ?> map) throws JSONException {
        final int length = map.size();
        object();

        if (length != 0) {
            Iterator<?> keys = map.keySet().iterator();
            while (keys.hasNext()) {
                Object key = keys.next();
                key(String.valueOf(key)).writeValue(map.get(key));
            }
        }
        endObject();
        return stack.isEmpty();
    }

    /**
     * Write the contents of the Iterable as JSON array to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param collection
     *            The Iterable to be written
     * @throws JSONException there was a problem writing the Iterable
     */
    private boolean iterableValue(Iterable<?> collection) throws JSONException {
        Iterator<?> iterator = collection.iterator();
        array();
        while (iterator.hasNext()) {
            writeValue(iterator.next());
        }
        endArray();
        return stack.isEmpty();
    }

    /**
     * Write the contents of the array as JSON array to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param array
     *            The array to be written
     * @throws JSONException there was a problem writing the array
     */
    private boolean arrayValue(Object array) throws JSONException {
        try {
            final int length = Array.getLength(array);
            array();
            for (int i = 0; i < length; i += 1) {
                writeValue(Array.get(array, i));
            }
            endArray();
            return stack.isEmpty();
        } catch (IllegalArgumentException e) {
            throw new JSONWriterException(e, getLocation());
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JSONWriterException(e, getLocation());
        }
    }

    /**
     * Write the contents of the JavaBean as JSON object to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param bean
     *            The bean to be written
     * @throws JSONException there was a problem writing the bean
     */
    private boolean beanValue(Object bean) throws JSONException {
        try {
            // If klass is a System class then set includeSuperClass to false.
            Class<?> klass = bean.getClass();
            boolean includeSuperClass = klass.getClassLoader() != null;
            Method[] methods = includeSuperClass ? klass.getMethods()
                    : klass.getDeclaredMethods();

            object();
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
                                key(String.valueOf(key)).writeValue(result);
                            }
                        }
                    }
                } catch (Exception ignore) {
                }
            }
            endObject();
            return stack.isEmpty();
        } catch (JSONException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new JSONException(exception);
        }
    }
}
