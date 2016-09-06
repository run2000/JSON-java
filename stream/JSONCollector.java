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

/**
 * Structure collector that creates {@code JSONObject}, {@code JSONArray}, and
 * {@code JSONObject.NULL} instances.
 * <p>
 * This collector holds no state, so a singleton is provided.</p>
 *
 * @author JSON.org
 * @version 2016-09-05
 */
public final class JSONCollector implements StructureCollector
        <JSONObject, JSONArray, JSONObject, JSONArray> {

    /** The singleton instance of this collector. */
    public static final JSONCollector INSTANCE = new JSONCollector();

    private JSONCollector() {
    }

    /**
     * Create a new {@code JSONObject} into which JSON object entries are added.
     *
     * @return a new {@code JSONObject} instance
     */
    @Override
    public JSONObject createObjectAccumulator() {
        return new JSONObject();
    }

    @Override
    public void addValue(JSONObject object, String key, Object value) throws JSONException {
        object.putOnce(key, value);
    }

    /**
     * Adds a {@code JSONObject.NULL} value an an entry to the given JSONObject.
     *
     * @param object the target to which the entry will be added
     * @param key the key of the entry
     * @throws JSONException a duplicate key is encountered
     */
    @Override
    public void addNull(JSONObject object, String key) throws JSONException {
        object.putOnce(key, JSONObject.NULL);
    }

    @Override
    public JSONObject finishObject(JSONObject accumulator) {
        return accumulator;
    }

    /**
     * Create a new {@code JSONArray} into which JSON array entries are added.
     *
     * @return a new {@code JSONArray} instance
     */
    @Override
    public JSONArray createArrayAccumulator() {
        return new JSONArray();
    }

    @Override
    public void addValue(JSONArray array, Object value) throws JSONException {
        array.put(value);
    }

    /**
     * Adds a {@code JSONObject.NULL} value to the given JSON array.
     *
     * @param array the target to which the null value will be added
     */
    @Override
    public void addNull(JSONArray array) throws JSONException {
        array.put(JSONObject.NULL);
    }

    @Override
    public JSONArray finishArray(JSONArray accumulator) {
        return accumulator;
    }

    /**
     * Provide a null value for circumstances where null is the complete result
     * of a JSON parse.
     *
     * @return The {@code JSONObject.NULL} value
     */
    @Override
    public Object nullValue() {
        return JSONObject.NULL;
    }
}
