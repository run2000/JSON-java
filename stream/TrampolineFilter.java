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

import org.json.stream.JSONStreamReader.ParseState;

/**
 * Filtering for structures during a parse.
 *
 * @author JSON.org
 * @version 2016-08-02
 */
public interface TrampolineFilter {

    /**
     * Determine whether to accept or reject a value within an array.
     *
     * @param index the array index within the current array
     * @param state the type of value that will be built
     * @param stackDepth the depth of the object or array
     * @param stack can be used to construct a JSON Pointer expression, see
     *              {@link JSONPointerUtils#toJSONPointer(Iterable)}.
     * @return {@code true} to allow the value to be created, otherwise
     * {@code false} to skip the value
     */
    boolean acceptIndex(int index, ParseState state, int stackDepth, Iterable<StructureBuilder> stack);

    /**
     * Determine whether to accept or reject a value within an object.
     *
     * @param fieldName the key name within the current object
     * @param state the type of value that will be built
     * @param stackDepth the depth of the object or array
     * @param stack can be used to construct a JSON Pointer expression, see
     *              {@link JSONPointerUtils#toJSONPointer(Iterable)}.
     * @return {@code true} to allow the value to be created, otherwise
     * {@code false} to skip the value
     */
    boolean acceptField(String fieldName, ParseState state, int stackDepth, Iterable<StructureBuilder> stack);

}