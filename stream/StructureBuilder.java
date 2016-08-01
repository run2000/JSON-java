package org.json.stream;

import org.json.JSONException;
import org.json.stream.JSONStreamReader.ParseState;

/**
 * Structure builder interface, used by the {@link TrampolineObjectBuilder}
 * class to create arrays and objects without requiring a recursive algorithm.
 *
 * @author NCULL
 * @version 1/08/2016 3:34 PM.
 */
public interface StructureBuilder {

    void accept(ParseState state, ALStack<StructureBuilder> stack, JSONStreamReader reader) throws JSONException;

    String jsonPointerFragment();

}
