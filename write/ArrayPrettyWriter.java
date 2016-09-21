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
 * Writes a JSON Array in a pretty-printed format.
 *
 * @author JSON.org
 * @version 2016-09-14
 */
final class ArrayPrettyWriter implements StructureWriter, IndentingWriter {
    private int arrayIndex = -1;
    private int lastSubValue = -1;
    private int indentFactor;
    private int indent;
    private char parentStructure;

    ArrayPrettyWriter(int indentFactor, int indent) {
        this.indentFactor = indentFactor;
        this.indent = indent;
        this.parentStructure = 'i';
    }

    ArrayPrettyWriter(StructureWriter parentWriter) {
        if (parentWriter instanceof IndentingWriter) {
            this.indentFactor = ((IndentingWriter) parentWriter).getIndentFactor();
            this.indent = ((IndentingWriter) parentWriter).getIndent();
            this.parentStructure = parentWriter.getStructureType();
        } else {
            this.parentStructure = 'i';
        }
    }

    @Override
    public void beginStructure(Appendable writer) throws JSONException {
        try {
            switch (parentStructure) {
                case 'i':
                    WriterUtil.indent(indent, writer);
                    this.indent += this.indentFactor;
                    break;
                case 'a':
                    writer.append('\n');
                    WriterUtil.indent(indent, writer);
                    this.indent += this.indentFactor;
                    break;

                default:
                    this.indent += this.indentFactor;
            }
            writer.append('[');
        } catch (IOException e) {
            throw new JSONException(e);
        }
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

    @Override
    public void subValue(Appendable writer) throws JSONException {
        try {
            if (arrayIndex >= 0) {
                writer.append(", ");
            }
        } catch (IOException e) {
            throw new JSONException(e);
        }
        arrayIndex++;
        lastSubValue = arrayIndex;
    }

    private void writeComma(Appendable writer) {
        try {
            if (arrayIndex >= 0) {
                writer.append(",\n");
            } else {
                writer.append('\n');
            }
            WriterUtil.indent(indent, writer);
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    @Override
    public boolean simpleValue(Object value, Appendable writer) throws JSONException {
        writeComma(writer);
        arrayIndex++;
        WriterUtil.writeSimpleValue(value, writer);
        return false;
    }

    @Override
    public boolean jsonStringValue(JSONString value, Appendable writer) throws JSONException {
        writeComma(writer);
        arrayIndex++;
        WriterUtil.writeJSONString(value, writer);
        return false;
    }

    @Override
    public boolean jsonAppendableValue(JSONAppendable value, Appendable writer) throws JSONException {
        writeComma(writer);
        arrayIndex++;
        WriterUtil.writeJSONAppendable(value, writer);
        return false;
    }

    @Override
    public boolean nullValue(Appendable writer) throws JSONException {
        writeComma(writer);
        arrayIndex++;
        WriterUtil.writeNull(writer);
        return false;
    }

    @Override
    public boolean booleanValue(boolean value, Appendable writer) throws JSONException {
        writeComma(writer);
        arrayIndex++;
        WriterUtil.writeBoolean(value, writer);
        return false;
    }

    @Override
    public boolean doubleValue(double value, Appendable writer) throws JSONException {
        writeComma(writer);
        arrayIndex++;
        WriterUtil.writeDouble(value, writer);
        return false;
    }

    @Override
    public boolean longValue(long value, Appendable writer) throws JSONException {
        writeComma(writer);
        arrayIndex++;
        WriterUtil.writeLong(value, writer);
        return false;
    }

    @Override
    public boolean endStructure(Appendable writer) throws JSONException {
        try {
            writer.append('\n');
            indent -= indentFactor;
            WriterUtil.indent(indent, writer);
            writer.append(']');
            return parentStructure == 'i';
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    @Override
    public String getIdentifier() {
        return (arrayIndex < 0) ? null : String.valueOf(arrayIndex);
    }

    @Override
    public String toString() {
        return String.valueOf(getIdentifier());
    }

    /**
     * Returns {@code 'a'} to indicate a JSON Array.
     *
     * @return the type of this structure, as a char value
     */
    @Override
    public char getStructureType() {
        return 'a';
    }

    @Override
    public int getIndentFactor() {
        return indentFactor;
    }

    @Override
    public int getIndent() {
        return indent;
    }
}
