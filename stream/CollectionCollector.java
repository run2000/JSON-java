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

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Structure collector that creates {@code HashMap}, {@code ArrayList}, and
 * {@code null} instances. The result types are finished using
 * {@code Collections.unmodifiableMap()} and {@code Collections.unmodifiableList()}.
 * <p>
 * This collector holds no state, so a singleton is provided.</p>
 *
 * @author JSON.org
 * @version 2016-09-05
 */
public final class CollectionCollector implements StructureCollector
        <Map<String, Object>, List<Object>, Map<String, Object>, List<Object>> {

    /** The singleton instance of this collector. */
    public static final CollectionCollector INSTANCE = new CollectionCollector();

    private CollectionCollector() {
    }

    /**
     * Create a new {@code HashMap} into which JSON object entries are added.
     *
     * @return a new {@code HashMap} instance
     */
    @Override
    public Map<String, Object> createObjectAccumulator() {
        return new HashMap<String, Object>();
    }

    @Override
    public void addValue(Map<String, Object> object, String key, Object value) throws JSONException {
        if(object.put(key, value) != null) {
            throw new JSONException("Duplicate key");
        }
    }

    /**
     * Adds a Java {@code null} value an an entry to the given JSON object.
     *
     * @param object the target to which the entry will be added
     * @param key the key of the entry
     * @throws JSONException a duplicate key is encountered
     */
    @Override
    public void addNull(Map<String, Object> object, String key) throws JSONException {
        if(object.put(key, null) != null) {
            throw new JSONException("Duplicate key");
        }
    }

    /**
     * Finish the given Object accumulator, returning the Object result.
     * The result is an unmodifiable {@code Map}.
     *
     * @param accumulator the Object accumulator to be finished
     * @return the finished Object result, as an unmodifiable {@code Map}
     */
    @Override
    public Map<String, Object> finishObject(Map<String, Object> accumulator) {
        return Collections.unmodifiableMap(accumulator);
    }

    /**
     * Create a new {@code ArrayList} into which JSON array entries are added.
     *
     * @return a new {@code ArrayList} instance
     */
    @Override
    public List<Object> createArrayAccumulator() {
        return new ArrayList<Object>();
    }

    @Override
    public void addValue(List<Object> array, Object value) {
        array.add(value);
    }

    /**
     * Adds a Java {@code null} value to the given JSON array.
     *
     * @param array the target to which the null value will be added
     */
    @Override
    public void addNull(List<Object> array) {
        array.add(null);
    }

    /**
     * Finish the given Array accumulator, returning the Array result.
     * The result in an unmodifiable {@code List}.
     *
     * @param accumulator the Array accumulator to be finished
     * @return the finished Array result, as an unmodifiable {@code List}
     */
    @Override
    public List<Object> finishArray(List<Object> accumulator) {
        return Collections.unmodifiableList(accumulator);
    }

    @Override
    public Object nullValue() {
        return null;
    }
}
