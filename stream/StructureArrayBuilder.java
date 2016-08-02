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

/**
 * Build values onto a given JSONArray.
 *
 * @author JSON.org
 * @version 2016-08-02
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
                ++index;
                if((filter == null) || (filter.acceptIndex(index, state, stack.size(), stack))) {
                    Object value = reader.nextValue();
                    array.put(value);
                }
                break;
            case ARRAY:
                ++index;
                if((filter == null) || (filter.acceptIndex(index, state, stack.size(), stack))) {
                    JSONArray newArray = new JSONArray();
                    array.put(newArray);
                    stack.push(new StructureArrayBuilder(newArray, filter));
                } else {
                    reader.skipToEndStructure();
                }
                break;
            case OBJECT:
                ++index;
                if((filter == null) || (filter.acceptIndex(index, state, stack.size(), stack))) {
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

    public String getIdentifier() {
        return String.valueOf(index);
    }

    @Override
    public String toString() {
        return getIdentifier();
    }
}
