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

import org.json.JSONException;
import org.json.JSONParseException;
import org.json.stream.JSONStreamReader.ParseState;
import org.json.util.ALStack;

/**
 * Build values onto a given JSONArray.
 *
 * @author JSON.org
 * @version 2016-08-02
 */
final class StructureArrayBuilder<OA, AA, OR, AR> implements StructureBuilder<AR> {
    private final AA array;
    private final BuilderLimits params;
    private final StructureCollector<OA, AA, OR, AR> factory;
    private int index;

    public StructureArrayBuilder(BuilderLimits params, StructureCollector<OA, AA, OR, AR> factory) {
        this.array = factory.createArrayAccumulator(params);
        this.params = params;
        this.index = -1;
        this.factory = factory;
    }

    @Override
    public StructureBuilder<?> accept(ParseState state, ALStack<StructureBuilder<?>> stack, JSONStreamReader reader) throws JSONException {
        final LimitFilter filter = params.getFilter();

        switch(state) {
            case NULL_VALUE:
                ++index;
                if (index >= params.getContentNodes()) {
                    throw new JSONParseException("Too many content nodes", reader.getParsePosition());
                } else if (filter == null || filter.acceptIndex(index, state, stack)) {
                    factory.addNull(array);
                }
                break;
            case BOOLEAN_VALUE:
            case NUMBER_VALUE:
            case STRING_VALUE:
                ++index;
                if (index >= params.getContentNodes()) {
                    throw new JSONParseException("Too many content nodes", reader.getParsePosition());
                } else if (filter == null || filter.acceptIndex(index, state, stack)) {
                    Object value = reader.nextValue();
                    factory.addValue(array, value);
                }
                break;
            case ARRAY:
                ++index;
                if (index >= params.getContentNodes()) {
                    throw new JSONParseException("Too many content nodes", reader.getParsePosition());
                } else if (stack.size() >= params.getNestingDepth()) {
                    throw new JSONParseException("Object nesting too deep", reader.getParsePosition());
                } else if (filter == null || filter.acceptIndex(index, state, stack)) {
                    StructureArrayBuilder<OA, AA, OR, AR> builder = new StructureArrayBuilder<OA, AA, OR, AR>(params, factory);
                    stack.push(builder);
                    return builder;
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
                } else if (filter == null || filter.acceptIndex(index, state, stack)) {
                    StructureObjectBuilder<OA, AA, OR, AR> builder = new StructureObjectBuilder<OA, AA, OR, AR>(params, factory);
                    stack.push(builder);
                    return builder;
                } else {
                    reader.skipToEndStructure();
                }
                break;
            case END_ARRAY:
                stack.pop();
                if(stack.isEmpty()) {
                    return null;
                } else {
                    StructureBuilder<?> parent = stack.peek();
                    parent.acceptChildValue(factory.finishArray(array));
                    return parent;
                }
            default:
                throw new JSONParseException("Expected value", reader.getParsePosition());
        }
        return this;
    }

    @Override
    public void acceptChildValue(Object childValue) throws JSONException {
        factory.addValue(array, childValue);
    }

    public String getIdentifier() {
        return String.valueOf(index);
    }

    @Override
    public String toString() {
        return getIdentifier();
    }

    @Override
    public AR getResult() {
        return factory.finishArray(array);
    }
}
