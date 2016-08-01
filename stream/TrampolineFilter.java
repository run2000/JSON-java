package org.json.stream;

import org.json.stream.JSONStreamReader.ParseState;

/**
 * Created by run2000 on 8/1/2016.
 */
public interface TrampolineFilter {

    boolean acceptIndex(int index, ParseState state, int stackDepth, Iterable<StructureBuilder> stack);

    boolean acceptField(String fieldName, ParseState state, int stackDepth, Iterable<StructureBuilder> stack);

}
