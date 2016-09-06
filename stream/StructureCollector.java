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

/**
 * Allows {@code JSONLimitBuilder} to parameterize the creation and population
 * of object and array structures, {@code null} values, and creation of
 * a "finished" result type. Similar to {@code Collector} in Java 8's stream
 * package.
 * <p>
 * For instance, structures may be created as {@code JSONObject} and
 * {@code JSONArray}, with {@code JSONObject.NULL} for JSON null. Another
 * collector may create structures as {@code HashMap} and {@code ArrayList}, and
 * Java's {@code null} value for JSON null.
 * </p>
 * @param <OA> the JSON object accumulator type
 * @param <AA> the JSON array accumulator type
 * @param <OR> the JSON object result type
 * @param <AR> the JSON array result type
 * @author JSON.org
 * @version 2016-09-05
 */
public interface StructureCollector<OA, AA, OR, AR> {

    /**
     * Create an instance of an object into which JSON object entries are
     * added.
     *
     * @return a new object instance
     */
    OA createObjectAccumulator();

    /**
     * Adds the given entry to the given JSON object.
     *
     * @param object the JSON object accumulator to which the entry will be added
     * @param key the key of the entry
     * @param value the value of the entry
     * @throws JSONException may be thrown if a duplicate key is encountered
     */
    void addValue(OA object, String key, Object value) throws JSONException;

    /**
     * Adds a JSON null entry to the given JSON object. Implementations may
     * choose to add a sentinel value, or omit the entry entirely.
     *
     * @param object the JSON object accumulator to which the entry will be added
     * @param key the key of the entry
     * @throws JSONException may be thrown if a duplicate key is encountered
     */
    void addNull(OA object, String key) throws JSONException;

    /**
     * Finish the given JSON object accumulator, returning the object result.
     * This gives the collector an opportunity to create an immutable result.
     *
     * @param accumulator the JSON object accumulator to be finished
     * @return the finished object result
     * @throws JSONException there was a problem finishing the object
     */
    OR finishObject(OA accumulator) throws JSONException;

    /**
     * Create an instance of an object into which JSON array entries are
     * added.
     *
     * @return a new array instance
     */
    AA createArrayAccumulator();

    /**
     * Adds the given value to the given JSON array accumulator.
     *
     * @param array the JSON array accumulator to which the value will be added
     * @param value the value to be added
     */
    void addValue(AA array, Object value) throws JSONException;

    /**
     * Adds a JSON null value to the given JSON array accumulator.
     * Implementations can choose the sentinel value to be added.
     *
     * @param array the JSON array accumulator to which the null value
     * will be added
     */
    void addNull(AA array) throws JSONException;

    /**
     * Finish the given JSON array accumulator, returning the array result.
     * This gives the collector an opportunity to create an immutable result.
     *
     * @param accumulator the JSON array accumulator to be finished
     * @return the finished array result
     * @throws JSONException there was a problem finishing the array
     */
    AR finishArray(AA accumulator) throws JSONException;

    /**
     * Provide a null value for circumstances where JSON null is the complete
     * result of a JSON parse.
     *
     * @return a JSON null value
     */
    Object nullValue();

}
