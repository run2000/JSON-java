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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONParseException;
import org.json.stream.JSONStreamReader.ParseState;
import org.json.util.ALStack;

/**
 * Build values onto a given JSONObject.
 *
 * @author JSON.org
 * @version 2016-08-02
 */
final class StructureLimitObjectBuilder implements StructureBuilder {
    private final JSONObject object;
    private final BuilderLimits params;
    private String key = null;
    private int index;

    public StructureLimitObjectBuilder(JSONObject object, BuilderLimits params) {
        this.object = object;
        this.params = params;
        this.index = -1;
    }

    @Override
    public void accept(ParseState state, ALStack<StructureBuilder> stack, JSONStreamReader reader) throws JSONException {
        final LimitFilter filter = params.getFilter();

        if(state == ParseState.KEY) {
            key = reader.nextKey();
            state = reader.nextState();
        }

        switch(state) {
            case NULL_VALUE:
            case BOOLEAN_VALUE:
            case NUMBER_VALUE:
            case STRING_VALUE:
                ++index;
                if (index >= params.getContentNodes()) {
                    throw new JSONParseException("Too many content nodes", reader.getParsePosition());
                } else if((filter == null) || (filter.acceptField(key, state, stack))) {
                    Object value = reader.nextValue();
                    object.putOnce(key, value);
                }
                break;
            case ARRAY:
                ++index;
                if (index >= params.getContentNodes()) {
                    throw new JSONParseException("Too many content nodes", reader.getParsePosition());
                } else if (stack.size() >= params.getNestingDepth()) {
                    throw new JSONParseException("Object nesting too deep", reader.getParsePosition());
                } else if((filter == null) || (filter.acceptField(key, state, stack))) {
                    JSONArray newArray = new JSONArray();
                    object.putOnce(key, newArray);
                    stack.push(new StructureLimitArrayBuilder(newArray, params));
                } else {
                    reader.skipToEndStructure();
                }
                break;
            case OBJECT:
                ++index;
                if (index >= params.getContentNodes()) {
                    throw new JSONParseException("Too many content nodes", reader.getParsePosition());
                } else if (stack.size() >= params.getNestingDepth()) {
                    throw new JSONParseException("Object nesting too deep", reader.getParsePosition());
                } else if((filter == null) || (filter.acceptField(key, state, stack))) {
                    JSONObject newObject = new JSONObject();
                    object.putOnce(key, newObject);
                    stack.push(new StructureLimitObjectBuilder(newObject, params));
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

    public String getIdentifier() {
        return String.valueOf(key);
    }

    @Override
    public String toString() {
        return getIdentifier();
    }
}
