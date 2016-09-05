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
import org.json.stream.JSONStreamReader.ParseState;
import org.json.util.ALStack;

/**
 * Structure builder interface, used by the {@link JSONLimitBuilder}
 * class to create arrays and objects without requiring a recursive algorithm.
 *
 * @param <T> the result type of structure to be built
 * @author JSON.org
 * @version 2016-08-02
 */
public interface StructureBuilder<T> {

    /**
     * Accepts a structure to be parsed. Nested structures are pushed and
     * popped on the stack as required.
     *
     * @param state the current ParseState
     * @param stack the stack with which to push and pop nested structures
     * @param reader the stream reader from which to read the structure content
     * @throws JSONException there was a problem constructing the structure
     */
    StructureBuilder<?> accept(ParseState state, ALStack<StructureBuilder<?>> stack, JSONStreamReader reader) throws JSONException;

    /**
     * Accept the value result constructed by the child builder, and
     * populate the current field or index with the value.
     */
    void acceptChildValue(Object childValue) throws JSONException;

    /**
     * Returns an identifier fragment for this structure. This fragment
     * can then be used to construct a JSON Pointer value.
     *
     * @return an identifier fragment for the current structure, as a String
     * @see JSONPointerUtils#toJSONPointer(Iterable)
     */
    String getIdentifier();

    /**
     * Return the resulting structure result being built by this StructureBuilder.
     *
     * @return the structure being built
     */
    T getResult();

}
