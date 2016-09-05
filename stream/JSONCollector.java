package org.json.stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Structure collector that creates {@code JSONObject}, {@code JSONArray}, and
 * {@code JSONObject.NULL} instances.
 */
public final class JSONCollector implements StructureCollector
        <JSONObject, JSONArray, JSONObject, JSONArray> {
    public static final JSONCollector INSTANCE = new JSONCollector();

    private JSONCollector() {
    }

    /**
     * Create a new {@code JSONObject} into which JSON object entries are added.
     *
     * @param params limits imposed by the object builder
     * @return a new {@code JSONObject} instance
     */
    @Override
    public JSONObject createObjectAccumulator(BuilderLimits params) {
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
     * @param params limits imposed by the object builder
     * @return a new {@code JSONArray} instance
     */
    @Override
    public JSONArray createArrayAccumulator(BuilderLimits params) {
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
}
