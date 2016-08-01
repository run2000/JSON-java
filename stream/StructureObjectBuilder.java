package org.json.stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONParseException;

/**
 * Build values onto a given JSONObject.
 *
 * @author NCULL
 * @version 1/08/2016 3:46 PM.
 */
final class StructureObjectBuilder implements StructureBuilder {
    private final JSONObject object;
    private final TrampolineFilter filter;
    private String key = null;

    public StructureObjectBuilder(JSONObject object) {
        this.object = object;
        this.filter = null;
    }

    public StructureObjectBuilder(JSONObject object, TrampolineFilter filter) {
        this.object = object;
        this.filter = filter;
    }

    @Override
    public void accept(JSONStreamReader.ParseState state, ALStack<StructureBuilder> stack, JSONStreamReader reader) throws JSONException {

        switch(state) {
            case KEY:
                key = reader.nextKey();
                break;
            case NULL_VALUE:
            case BOOLEAN_VALUE:
            case NUMBER_VALUE:
            case STRING_VALUE:
                if((filter == null) || (filter.acceptField(key, state, stack.size(), stack))) {
                    Object value = reader.nextValue();
                    object.putOnce(key, value);
                }
                break;
            case ARRAY:
                if((filter == null) || (filter.acceptField(key, state, stack.size(), stack))) {
                    JSONArray newArray = new JSONArray();
                    object.putOnce(key, newArray);
                    stack.push(new StructureArrayBuilder(newArray, filter));
                } else {
                    reader.skipToEndStructure();
                }
                break;
            case OBJECT:
                if((filter == null) || (filter.acceptField(key, state, stack.size(), stack))) {
                    JSONObject newObject = new JSONObject();
                    object.putOnce(key, newObject);
                    stack.push(new StructureObjectBuilder(newObject, filter));
                } else {
                    reader.skipToEndStructure();
                }
                break;
            case END_OBJECT:
                stack.pop();
                break;
            default:
                throw new JSONParseException("Expected value", reader.getParsePosition());
        }
    }

    public String jsonPointerFragment() {
        String item = null;
        if(key != null) {
            item = key.replaceAll("~", "~0").replaceAll("/", "~1");
        }
        return '/' + item;
    }
}
