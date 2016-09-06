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
 * Build values onto a given JSON object.
 *
 * @author JSON.org
 * @version 2016-08-02
 */
final class StructureObjectBuilder<OA, AA, OR, AR> implements StructureBuilder<OR> {
    private final OA objectAccumulator;
    private final StructureBuilder<?> parentBuilder;
    private final BuilderLimits limits;
    private final StructureCollector<OA, AA, OR, AR> collector;
    private String key = null;
    private int index;

    public StructureObjectBuilder(StructureBuilder<?> parentBuilder,
            BuilderLimits limits, StructureCollector<OA, AA, OR, AR> collector) {
        this.parentBuilder = parentBuilder;
        this.objectAccumulator = collector.createObjectAccumulator();
        this.limits = limits;
        this.index = -1;
        this.collector = collector;
    }

    @Override
    public StructureBuilder<?> accept(ParseState state,
            ALStack<StructureIdentifier> stack, JSONStreamReader reader)
            throws JSONException {
        final LimitFilter filter = limits.getFilter();

        if(state == ParseState.KEY) {
            key = reader.nextKey();
            state = reader.nextState();
        }

        switch(state) {
            case NULL_VALUE:
                ++index;
                if (index >= limits.getContentNodes()) {
                    throw new JSONParseException("Too many content nodes", reader.getParsePosition());
                } else if((filter == null) || (filter.acceptField(key, state, stack))) {
                    collector.addNull(objectAccumulator, key);
                }
                break;
            case BOOLEAN_VALUE:
            case NUMBER_VALUE:
            case STRING_VALUE:
                ++index;
                if (index >= limits.getContentNodes()) {
                    throw new JSONParseException("Too many content nodes", reader.getParsePosition());
                } else if((filter == null) || (filter.acceptField(key, state, stack))) {
                    Object value = reader.nextValue();
                    collector.addValue(objectAccumulator, key, value);
                }
                break;
            case ARRAY:
                ++index;
                if (index >= limits.getContentNodes()) {
                    throw new JSONParseException("Too many content nodes", reader.getParsePosition());
                } else if (stack.size() >= limits.getNestingDepth()) {
                    throw new JSONParseException("Object nesting too deep", reader.getParsePosition());
                } else if((filter == null) || (filter.acceptField(key, state, stack))) {
                    StructureArrayBuilder<OA, AA, OR, AR> builder = new StructureArrayBuilder<OA, AA, OR, AR>(this, limits, collector);
                    stack.push(builder);
                    return builder;
                } else {
                    reader.skipToEndStructure();
                }
                break;
            case OBJECT:
                ++index;
                if (index >= limits.getContentNodes()) {
                    throw new JSONParseException("Too many content nodes", reader.getParsePosition());
                } else if (stack.size() >= limits.getNestingDepth()) {
                    throw new JSONParseException("Object nesting too deep", reader.getParsePosition());
                } else if((filter == null) || (filter.acceptField(key, state, stack))) {
                    StructureObjectBuilder<OA, AA, OR, AR> builder = new StructureObjectBuilder<OA, AA, OR, AR>(this, limits, collector);
                    stack.push(builder);
                    return builder;
                } else {
                    reader.skipToEndStructure();
                }
                break;
            case END_OBJECT:
                stack.pop();
                if(parentBuilder != null) {
                    parentBuilder.acceptChildValue(collector.finishObject(objectAccumulator));
                }
                return parentBuilder;
            default:
                throw new JSONParseException("Expected value", reader.getParsePosition());
        }
        return this;
    }

    @Override
    public void acceptChildValue(Object childValue) throws JSONException {
        collector.addValue(objectAccumulator, key, childValue);
    }

    public String getIdentifier() {
        return String.valueOf(key);
    }

    @Override
    public String toString() {
        return getIdentifier();
    }

    @Override
    public OR getResult() {
        return collector.finishObject(objectAccumulator);
    }
}
