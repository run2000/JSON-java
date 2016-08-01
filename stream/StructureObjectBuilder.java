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
            item = encodePointer(key);
        }
        return '/' + item;
    }

    /**
     * Encode a key according to the JSON Pointer syntax. See RFC 6901
     * section 3 for details.
     *
     * @param name the key to be encoded
     * @return the encoded key
     */
    private static String encodePointer(String name) {
        final int len = (name == null) ? 0 : name.length();
        if(len == 0) {
            return name;
        }

        StringBuilder builder = null;
        int prev = 0;
        int curr;

        for(curr = 0; curr < len; curr++) {
            char c = name.charAt(curr);
            switch(c) {
                case '~':
                    if(builder == null) {
                        builder = new StringBuilder(len + 2);
                    }
                    if(prev < curr) {
                        builder.append(name.substring(prev, curr));
                        builder.append("~0");
                        prev = curr + 1;
                    }
                    break;
                case '/':
                    if(builder == null) {
                        builder = new StringBuilder(len + 2);
                    }
                    if(prev < curr) {
                        builder.append(name.substring(prev, curr));
                        builder.append("~1");
                        prev = curr + 1;
                    }
                    break;
            }
        }
        if(builder == null) {
            return name;
        }
        if(prev < curr) {
            builder.append(name.substring(prev));
        }
        return builder.toString();
    }
}
