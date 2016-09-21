package org.json.write;

/*
Copyright (c) 2006 JSON.org

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

import org.json.JSONAppendable;
import org.json.JSONException;
import org.json.JSONString;
import org.json.util.StructureIdentifier;

/**
 * Strategy interface for writing a JSON structure, either a JSON Object or
 * a JSON Array. A strategy class can implement a compact or a pretty-print
 * output.
 *
 * @author JSON.org
 * @version 2016-09-14
 */
public interface StructureWriter extends StructureIdentifier {

    /**
     * Begin the structure represented by this object, either a JSON Object
     * or a JSON Array.
     *
     * @param writer the Appendable to write any beginning structure content
     * @throws JSONException there was a problem beginning the structure
     */
    void beginStructure(Appendable writer) throws JSONException;

    /**
     * Write a key for a JSON Object.
     *
     * @param key the key to be written
     * @param writer the Appendable to write the key
     * @throws JSONException this structure is not a JSON Object, the key is
     * {@code null}, or there was another problem writing the key
     */
    void key(String key, Appendable writer) throws JSONException;

    /**
     * Indicates that a sub-structure is about to be written. Write any
     * JSON content necessary prior to beginning the sub-structure.
     *
     * @param writer the Appendable to write any delimiters or separators
     * @throws JSONException there was a problem writing the delimiter
     */
    void subValue(Appendable writer) throws JSONException;

    /**
     * Write a simple value to the given {@code Appendable}.
     *
     * @param value the value to be written
     * @param writer the Appendable to which the JSON value will be written
     * @return {@code true} to indicate JSON writing has been completed, and
     * no more content may be written, otherwise {@code false} to indicate
     * more JSON data may be written
     * @throws JSONException there was a problem writing the value
     * @see WriterUtil#isSimpleValue(Object)
     */
    boolean simpleValue(Object value, Appendable writer) throws JSONException;

    /**
     * Write a {@code JSONString} value to the given {@code Appendable}.
     *
     * @param value the value to be written
     * @param writer the Appendable to which the JSON value will be written
     * @return {@code true} to indicate JSON writing has been completed, and
     * no more content may be written, otherwise {@code false} to indicate
     * more JSON data may be written
     * @throws JSONException there was a problem writing the value
     */
    boolean jsonStringValue(JSONString value, Appendable writer) throws JSONException;

    /**
     * Write a {@code JSONAppendable} value to the given {@code Appendable}.
     *
     * @param value the value to be written
     * @param writer the Appendable to which the JSON value will be written
     * @return {@code true} to indicate JSON writing has been completed, and
     * no more content may be written, otherwise {@code false} to indicate
     * more JSON data may be written
     * @throws JSONException there was a problem writing the value
     */
    boolean jsonAppendableValue(JSONAppendable value, Appendable writer) throws JSONException;

    /**
     * Write a JSON {@code null} value to the given {@code Appendable}.
     *
     * @param writer the Appendable to which the JSON value will be written
     * @return {@code true} to indicate JSON writing has been completed, and
     * no more content may be written, otherwise {@code false} to indicate
     * more JSON data may be written
     * @throws JSONException there was a problem writing the value
     */
    boolean nullValue(Appendable writer) throws JSONException;

    /**
     * Write a {@code boolean} value to the given {@code Appendable}.
     *
     * @param value the value to be written
     * @param writer the Appendable to which the JSON value will be written
     * @return {@code true} to indicate JSON writing has been completed, and
     * no more content may be written, otherwise {@code false} to indicate
     * more JSON data may be written
     * @throws JSONException there was a problem writing the value
     */
    boolean booleanValue(boolean value, Appendable writer) throws JSONException;

    /**
     * Write a {@code double} value to the given {@code Appendable}.
     *
     * @param value the value to be written
     * @param writer the Appendable to which the JSON value will be written
     * @return {@code true} to indicate JSON writing has been completed, and
     * no more content may be written, otherwise {@code false} to indicate
     * more JSON data may be written
     * @throws JSONException there was a problem writing the value
     */
    boolean doubleValue(double value, Appendable writer) throws JSONException;

    /**
     * Write a {@code long} value to the given {@code Appendable}.
     *
     * @param value the value to be written
     * @param writer the Appendable to which the JSON value will be written
     * @return {@code true} to indicate JSON writing has been completed, and
     * no more content may be written, otherwise {@code false} to indicate
     * more JSON data may be written
     * @throws JSONException there was a problem writing the value
     */
    boolean longValue(long value, Appendable writer) throws JSONException;

    /**
     * End the structure represented by this object, either a JSON Object
     * or a JSON Array.
     *
     * @param writer the Appendable to write any end structure content
     * @return {@code true} to indicate JSON writing has been completed, and
     * no more content may be written, otherwise {@code false} to indicate
     * more JSON data may be written
     * @throws JSONException there was a problem ending the structure
     */
    boolean endStructure(Appendable writer) throws JSONException;

    /**
     * Returns the type of this structure as a char value. The values, by
     * convention, are:
     * <ul>
     *     <li>{@code 'i'} &mdash; the initial structure, before any JSON Object or
     *     JSON Array has been written</li>
     *     <li>{@code 'o'} &mdash; a JSON Object</li>
     *     <li>{@code 'a'} &mdash; a JSON Array</li>
     * </ul>
     * @return the type of this structure, as a char value
     */
    char getStructureType();

}
