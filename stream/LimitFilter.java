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
import org.json.util.JSONPointerUtils;
import org.json.util.SizedIterable;
import org.json.util.StructureIdentifier;

/**
 * Filtering for structures during a parse. Provided by the {@link BuilderLimits}
 * class to the {@link StructureBuilder} implementations.
 *
 * @author JSON.org
 * @version 2016-08-02
 */
public interface LimitFilter {

    /**
     * Determine whether to accept or reject a value within an array.
     *
     * @param index the array index within the current array
     * @param state the type of value that will be built
     * @param ids can be used to construct a JSON Pointer expression, or
     *              determine the depth of the current structure
     * @return {@code true} to allow the value to be created, otherwise
     * {@code false} to skip the value
     * @see JSONPointerUtils#toJSONPointer(Iterable)
     */
    boolean acceptIndex(int index, ParseState state, SizedIterable<StructureIdentifier> ids);

    /**
     * Determine whether to accept or reject a value within an object.
     *
     * @param fieldName the key name within the current object
     * @param state the type of value that will be built
     * @param ids can be used to construct a JSON Pointer expression, or
     *              determine the depth of the current structure
     * @return {@code true} to allow the value to be created, otherwise
     * {@code false} to skip the value
     * @see JSONPointerUtils#toJSONPointer(Iterable)
     */
    boolean acceptField(String fieldName, ParseState state, SizedIterable<StructureIdentifier> ids);

}
