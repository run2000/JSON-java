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

/**
 * Null structure writer for only allowing JSON Object or JSON Array as a
 * root value. This enforces the I-JSON recommendation in section 4.1 of
 * <a href="http://tools.ietf.org/html/rfc7493">RFC 7493</a>.
 *
 * @author JSON.org
 * @version 2016-09-18
 */
public final class NullStructureWriter implements StructureWriter {
    /** The singleton instance. */
    public static final NullStructureWriter INSTANCE = new NullStructureWriter();

    private NullStructureWriter() {
    }

    /**
     * Throws a {@code JSONException} to indicate this writer can't write
     * a structure itself.
     *
     * @param writer the Appendable to write any beginning structure content
     * @throws JSONException no structure can be written by this writer
     */
    @Override
    public void beginStructure(Appendable writer) throws JSONException {
        throw new JSONException("No structure.");
    }

    /**
     * Throws a {@code JSONException} to indicate this writer can't write
     * a key.
     *
     * @param key the key to be written
     * @param writer the Appendable to write the key
     * @throws JSONException no key can be written by this writer
     */
    @Override
    public void key(String key, Appendable writer) throws JSONException {
        throw new JSONException("Misplaced key.");
    }

    /**
     * This method does nothing itself, but allows a sub-structure to be
     * written.
     *
     * @param writer the Appendable to write any delimiters or separators
     */
    @Override
    public void subValue(Appendable writer) {
        // do nothing
    }

    /**
     * Throws a {@code JSONException} to indicate this writer can't write
     * a value.
     *
     * @param value the value to be written
     * @param writer the Appendable to which the JSON value will be written
     * @throws JSONException a value cannot be written by this writer
     */
    @Override
    public boolean simpleValue(Object value, Appendable writer) throws JSONException {
        throw new JSONException("Value out of sequence.");
    }

    /**
     * Throws a {@code JSONException} to indicate this writer can't write
     * a value.
     *
     * @param value the value to be written
     * @param writer the Appendable to which the JSON value will be written
     * @throws JSONException a value cannot be written by this writer
     */
    @Override
    public boolean jsonStringValue(JSONString value, Appendable writer) throws JSONException {
        throw new JSONException("Value out of sequence.");
    }

    /**
     * Throws a {@code JSONException} to indicate this writer can't write
     * a value.
     *
     * @param value the value to be written
     * @param writer the Appendable to which the JSON value will be written
     * @throws JSONException a value cannot be written by this writer
     */
    @Override
    public boolean jsonAppendableValue(JSONAppendable value, Appendable writer) throws JSONException {
        throw new JSONException("Value out of sequence.");
    }

    /**
     * Throws a {@code JSONException} to indicate this writer can't write
     * a value.
     *
     * @param writer the Appendable to which the JSON value will be written
     * @throws JSONException a value cannot be written by this writer
     */
    @Override
    public boolean nullValue(Appendable writer) throws JSONException {
        throw new JSONException("Value out of sequence.");
    }

    /**
     * Throws a {@code JSONException} to indicate this writer can't write
     * a value.
     *
     * @param value the value to be written
     * @param writer the Appendable to which the JSON value will be written
     * @throws JSONException a value cannot be written by this writer
     */
    @Override
    public boolean booleanValue(boolean value, Appendable writer) throws JSONException {
        throw new JSONException("Value out of sequence.");
    }

    /**
     * Throws a {@code JSONException} to indicate this writer can't write
     * a value.
     *
     * @param value the value to be written
     * @param writer the Appendable to which the JSON value will be written
     * @throws JSONException a value cannot be written by this writer
     */
    @Override
    public boolean doubleValue(double value, Appendable writer) throws JSONException {
        throw new JSONException("Value out of sequence.");
    }

    /**
     * Throws a {@code JSONException} to indicate this writer can't write
     * a value.
     *
     * @param value the value to be written
     * @param writer the Appendable to which the JSON value will be written
     * @throws JSONException a value cannot be written by this writer
     */
    @Override
    public boolean longValue(long value, Appendable writer) throws JSONException {
        throw new JSONException("Value out of sequence.");
    }

    /**
     * Throws a {@code JSONException} to indicate this writer can't write
     * an end structure.
     *
     * @param writer the Appendable to write any end structure content
     * @throws JSONException no end structure can be written by this writer
     */
    @Override
    public boolean endStructure(Appendable writer) throws JSONException {
        throw new JSONException("Value out of sequence.");
    }

    /**
     * Returns {@code null} for no identifier.
     *
     * @return {@code null}
     */
    @Override
    public String getIdentifier() {
        return null;
    }

    /**
     * Returns {@code 'i'} to indicate the initial structure.
     *
     * @return the type of this structure, as a char value
     */
    @Override
    public char getStructureType() {
        return 'i';
    }
}
