package org.json.stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONParseException;
import org.json.stream.JSONStreamReader.ParseState;

/**
 * Build values onto a given JSONArray.
 *
 * @author NCULL
 * @version 1/08/2016 3:35 PM.
 */
final class StructureArrayBuilder implements StructureBuilder {
    private final JSONArray array;
    private final TrampolineFilter filter;
    private int index;

    public StructureArrayBuilder(JSONArray array) {
        this.array = array;
        this.filter = null;
        this.index = -1;
    }

    public StructureArrayBuilder(JSONArray array, TrampolineFilter filter) {
        this.array = array;
        this.filter = filter;
        this.index = -1;
    }

    @Override
    public void accept(ParseState state, ALStack<StructureBuilder> stack, JSONStreamReader reader) throws JSONException {

        switch(state) {
            case NULL_VALUE:
            case BOOLEAN_VALUE:
            case NUMBER_VALUE:
            case STRING_VALUE:
                if((filter == null) || (filter.acceptIndex(++index, state, stack.size(), stack))) {
                    Object value = reader.nextValue();
                    array.put(value);
                }
                break;
            case ARRAY:
                if((filter == null) || (filter.acceptIndex(++index, state, stack.size(), stack))) {
                    JSONArray newArray = new JSONArray();
                    array.put(newArray);
                    stack.push(new StructureArrayBuilder(newArray, filter));
                } else {
                    reader.skipToEndStructure();
                }
                break;
            case OBJECT:
                if((filter == null) || (filter.acceptIndex(++index, state, stack.size(), stack))) {
                    JSONObject newObject = new JSONObject();
                    array.put(newObject);
                    stack.push(new StructureObjectBuilder(newObject, filter));
                } else {
                    reader.skipToEndStructure();
                }
                break;
            case END_ARRAY:
                stack.pop();
                break;
            default:
                throw new JSONParseException("Expected value", reader.getParsePosition());
        }
    }

    public String jsonPointerFragment() {
        return '/' + String.valueOf(index);
    }
}
