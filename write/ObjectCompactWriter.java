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
import java.util.HashSet;
import java.util.Set;

/**
 * Writes a JSON Object in a compact format.
 *
 * @author JSON.org
 * @version 2016-09-14
 */
final class ObjectCompactWriter implements StructureWriter {
    private final Set<String> keys = new HashSet<String>();
    private final boolean topLevel;
    private String currentKey = null;
    private boolean keyWritten = false;

    ObjectCompactWriter(boolean topLevel) {
        this.topLevel = topLevel;
    }

    @Override
    public void beginStructure(Appendable writer) throws JSONException {
        try {
            writer.append('{');
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    @Override
    public void key(String key, Appendable writer) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key.");
        }
        if (keyWritten) {
            throw new JSONException("Misplaced key.");
        } else {
            try {
                if (!keys.add(key)) {
                    throw new JSONException("Duplicate key \"" + key + "\"");
                }
                if (currentKey != null) {
                    writer.append(',');
                }
                currentKey = key;
                keyWritten = true;
                WriterUtil.writeString(key, writer);
                writer.append(':');
            } catch (IOException e) {
                throw new JSONException(e);
            }
        }
    }

    @Override
    public void subValue(Appendable writer) throws JSONException {
        if (!keyWritten) {
            throw new JSONException("Misplaced value.");
        }
        keyWritten = false;
    }

    @Override
    public boolean simpleValue(Object value, Appendable writer) throws JSONException {
        if (!keyWritten) {
            throw new JSONException("Misplaced value.");
        }
        keyWritten = false;
        WriterUtil.writeSimpleValue(value, writer);
        return false;
    }

    @Override
    public boolean jsonStringValue(JSONString value, Appendable writer) throws JSONException {
        if (!keyWritten) {
            throw new JSONException("Misplaced value.");
        }
        keyWritten = false;
        WriterUtil.writeJSONString(value, writer);
        return false;
    }

    @Override
    public boolean jsonAppendableValue(JSONAppendable value, Appendable writer) throws JSONException {
        if (!keyWritten) {
            throw new JSONException("Misplaced value.");
        }
        keyWritten = false;
        WriterUtil.writeJSONAppendable(value, writer);
        return false;
    }

    @Override
    public boolean nullValue(Appendable writer) throws JSONException {
        if (!keyWritten) {
            throw new JSONException("Misplaced value.");
        }
        keyWritten = false;
        WriterUtil.writeNull(writer);
        return false;
    }

    @Override
    public boolean booleanValue(boolean value, Appendable writer) throws JSONException {
        if (!keyWritten) {
            throw new JSONException("Misplaced value.");
        }
        keyWritten = false;
        WriterUtil.writeBoolean(value, writer);
        return false;
    }

    @Override
    public boolean doubleValue(double value, Appendable writer) throws JSONException {
        if (!keyWritten) {
            throw new JSONException("Misplaced value.");
        }
        keyWritten = false;
        WriterUtil.writeDouble(value, writer);
        return false;
    }

    @Override
    public boolean longValue(long value, Appendable writer) throws JSONException {
        if (!keyWritten) {
            throw new JSONException("Misplaced value.");
        }
        keyWritten = false;
        WriterUtil.writeLong(value, writer);
        return false;
    }

    @Override
    public boolean endStructure(Appendable writer) throws JSONException {
        try {
            writer.append('}');
            return topLevel;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    @Override
    public String getIdentifier() {
        return (currentKey == null) ? null : currentKey;
    }

    @Override
    public String toString() {
        return String.valueOf(getIdentifier());
    }

    /**
     * Returns {@code 'o'} to indicate a JSON Object.
     *
     * @return the type of this structure, as a char value
     */
    @Override
    public char getStructureType() {
        return 'o';
    }
}