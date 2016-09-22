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

import java.io.IOException;

/**
 * Initial structure writer for when a {@code JSONWriter} is only writing a
 * simple value.
 *
 * @author JSON.org
 * @version 2016-09-18
 */
public final class SimpleStructureWriter implements StructureWriter {
    /** A singleton instance for zero indent. */
    private static final SimpleStructureWriter INSTANCE = new SimpleStructureWriter(0);

    /**
     * Factory method for {@code SimpleStructureWriter}.
     *
     * @return a {@code SimpleStructureWriter}
     */
    public static SimpleStructureWriter getInstance(int indent) {
        if(Math.max(0, indent) == 0) {
            return INSTANCE;
        }
        return new SimpleStructureWriter(indent);
    }

    private final int indent;

    private SimpleStructureWriter(int indent) {
        this.indent = Math.max(0, indent);
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
        //
    }

    /**
     * Write a simple value to the given {@code Appendable}.
     *
     * @param value the value to be written
     * @param writer the Appendable to which the JSON value will be written
     * @return {@code true} to indicate JSON writing is complete
     * @throws JSONException there was a problem writing the value
     */
    @Override
    public boolean simpleValue(Object value, Appendable writer) throws JSONException {
        if(indent > 0) {
            try {
                WriterUtil.indent(indent, writer);
                WriterUtil.writeSimpleValue(value, writer);
            } catch (IOException e) {
                throw new JSONException(e);
            }
        } else {
            WriterUtil.writeSimpleValue(value, writer);
        }
        return true;
    }

    /**
     * Write a {@code JSONString} value to the given {@code Appendable}.
     *
     * @param value the value to be written
     * @param writer the Appendable to which the JSON value will be written
     * @return {@code true} to indicate JSON writing is complete
     * @throws JSONException there was a problem writing the value
     */
    @Override
    public boolean jsonStringValue(JSONString value, Appendable writer) throws JSONException {
        if(indent > 0) {
            try {
                WriterUtil.indent(indent, writer);
                WriterUtil.writeJSONString(value, writer);
            } catch (IOException e) {
                throw new JSONException(e);
            }
        } else {
            WriterUtil.writeJSONString(value, writer);
        }
        return true;
    }

    /**
     * Write a {@code JSONAppendable} value to the given {@code Appendable}.
     *
     * @param value the value to be written
     * @param writer the Appendable to which the JSON value will be written
     * @return {@code true} to indicate JSON writing is complete
     * @throws JSONException there was a problem writing the value
     */
    @Override
    public boolean jsonAppendableValue(JSONAppendable value, Appendable writer) throws JSONException {
        if(indent > 0) {
            try {
                WriterUtil.indent(indent, writer);
                WriterUtil.writeJSONAppendable(value, writer);
            } catch (IOException e) {
                throw new JSONException(e);
            }
        } else {
            WriterUtil.writeJSONAppendable(value, writer);
        }
        return true;
    }

    /**
     * Write a JSON {@code null} value to the given {@code Appendable}.
     *
     * @param writer the Appendable to which the JSON value will be written
     * @return {@code true} to indicate JSON writing is complete
     * @throws JSONException there was a problem writing the value
     */
    @Override
    public boolean nullValue(Appendable writer) throws JSONException {
        if(indent > 0) {
            try {
                WriterUtil.indent(indent, writer);
                WriterUtil.writeNull(writer);
            } catch (IOException e) {
                throw new JSONException(e);
            }
        } else {
            WriterUtil.writeNull(writer);
        }
        return true;
    }

    /**
     * Write a {@code boolean} value to the given {@code Appendable}.
     *
     * @param value the value to be written
     * @param writer the Appendable to which the JSON value will be written
     * @return {@code true} to indicate JSON writing is complete
     * @throws JSONException there was a problem writing the value
     */
    @Override
    public boolean booleanValue(boolean value, Appendable writer) throws JSONException {
        if(indent > 0) {
            try {
                WriterUtil.indent(indent, writer);
                WriterUtil.writeBoolean(value, writer);
            } catch (IOException e) {
                throw new JSONException(e);
            }
        } else {
            WriterUtil.writeBoolean(value, writer);
        }
        return true;
    }

    /**
     * Write a {@code double} value to the given {@code Appendable}.
     *
     * @param value the value to be written
     * @param writer the Appendable to which the JSON value will be written
     * @return {@code true} to indicate JSON writing is complete
     * @throws JSONException there was a problem writing the value
     */
    @Override
    public boolean doubleValue(double value, Appendable writer) throws JSONException {
        if (indent > 0) {
            try {
                WriterUtil.indent(indent, writer);
                WriterUtil.writeDouble(value, writer);
            } catch (IOException e) {
                throw new JSONException(e);
            }
        } else {
            WriterUtil.writeDouble(value, writer);
        }
        return true;
    }

    /**
     * Write a {@code long} value to the given {@code Appendable}.
     *
     * @param value the value to be written
     * @param writer the Appendable to which the JSON value will be written
     * @return {@code true} to indicate JSON writing is complete
     * @throws JSONException there was a problem writing the value
     */
    @Override
    public boolean longValue(long value, Appendable writer) throws JSONException {
        if (indent > 0) {
            try {
                WriterUtil.indent(indent, writer);
                WriterUtil.writeLong(value, writer);
            } catch (IOException e) {
                throw new JSONException(e);
            }
        } else {
            WriterUtil.writeLong(value, writer);
        }
        return true;
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
        throw new JSONException("No structure.");
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
