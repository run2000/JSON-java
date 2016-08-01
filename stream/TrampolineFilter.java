package org.json.stream;

import org.json.stream.JSONStreamReader.ParseState;

/**
 * Created by run2000 on 8/1/2016.
 */
public interface TrampolineFilter {

    /**
     * Determine whether to accept or reject a value within an array.
     *
     * @param index the array index within the current array
     * @param state the type of value that will be built
     * @param stackDepth the depth of the object or array
     * @param stack can be used to construct a JSON Pointer expression, see
     *              {@link TrampolineObjectBuilder#toJSONPointer(Iterable)}.
     * @return {@code true} to allow the value to be created, otherwise
     * {@code false} to skip the value
     */
    boolean acceptIndex(int index, ParseState state, int stackDepth, Iterable<StructureBuilder> stack);

    /**
     * Determine whether to accept or reject a value within an object.
     *
     * @param fieldName the key name within the current object
     * @param state the type of value that will be built
     * @param stackDepth the depth of the object or array
     * @param stack can be used to construct a JSON Pointer expression, see
     *              {@link TrampolineObjectBuilder#toJSONPointer(Iterable)}.
     * @return {@code true} to allow the value to be created, otherwise
     * {@code false} to skip the value
     */
    boolean acceptField(String fieldName, ParseState state, int stackDepth, Iterable<StructureBuilder> stack);

}
