package org.json.stream;

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
 * This collector holds no state, so a singleton is used.</p>
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

    @Override
    public List<Object> finishArray(List<Object> accumulator) {
        return Collections.unmodifiableList(accumulator);
    }
}
