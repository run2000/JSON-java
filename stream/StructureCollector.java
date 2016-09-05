package org.json.stream;

import org.json.JSONException;

/**
 * Allows {@code JSONLimitBuilder} to parameterise the creation and population
 * of object and array structures, {@code null} values, and creation of
 * a "finished" result type. Similar to {@code Collector} in Java 8's stream
 * package.
 * <p>
 * For instance, structures may be created as {@code JSONObject} and
 * {@code JSONArray}, with {@code JSONObject.NULL} for JSON null. Another
 * factory may create structures as {@code HashMap} and {@code ArrayList}, and
 * Java's {@code null} value for JSON null.
 * </p>
 * @param <OA> the JSON object accumulator type created by the factory
 * @param <AA> the JSON array accumulator type created by the factory
 * @param <OR> the JSON object result type
 * @param <AR> the JSON array result type
 */
public interface StructureCollector<OA, AA, OR, AR> {

    /**
     * Create an instance of an object into which JSON object entries are
     * added.
     *
     * @param params limits imposed by the object builder
     * @return a new object instance
     */
    OA createObjectAccumulator(BuilderLimits params);

    /**
     * Adds the given entry to the given JSON object.
     *
     * @param object the target to which the entry will be added
     * @param key the key of the entry
     * @param value the value of the entry
     * @throws JSONException may be thrown if a duplicate key is encountered
     */
    void addValue(OA object, String key, Object value) throws JSONException;

    /**
     * Adds a JSON null entry to the given JSON object. Implementations may
     * choose to add a sentinel value, or omit the entry entirely.
     *
     * @param object the target to which the entry will be added
     * @param key the key of the entry
     * @throws JSONException may be thrown if a duplicate key is encountered
     */
    void addNull(OA object, String key) throws JSONException;

    /**
     * Finish the given Object accumulator, returning the Object result.
     * This gives the collector an opportunity to create an immutable result.
     *
     * @param accumulator the Object accumulator to be finished
     * @return the finished Object result
     * @throws JSONException there was a problem finishing the Object
     */
    OR finishObject(OA accumulator) throws JSONException;

    /**
     * Create an instance of an object into which JSON array entries are
     * added.
     *
     * @param params limits imposed by the object builder
     * @return a new array instance
     */
    AA createArrayAccumulator(BuilderLimits params);

    /**
     * Adds the given value to the given JSON array.
     *
     * @param array the target to which the value will be added
     * @param value the value to be added
     */
    void addValue(AA array, Object value) throws JSONException;

    /**
     * Adds a JSON null value to the given JSON array. Implementations can
     * choose the sentinel value to be added.
     *
     * @param array the target to which the null value will be added
     */
    void addNull(AA array) throws JSONException;

    /**
     * Finish the given Array accumulator, returning the Array result.
     * This gives the collector an opportunity to create an immutable result.
     *
     * @param accumulator the Array accumulator to be finished
     * @return the finished Array result
     * @throws JSONException there was a problem finishing the Array
     */
    AR finishArray(AA accumulator) throws JSONException;

}
