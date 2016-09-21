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

/**
 * Factory class for creating {@code StructureWriter}s for pretty-print
 * JSON output. Parameterized by indent factor and initial indent.
 *
 * @author JSON.org
 * @version 2016-09-14
 */
public final class PrettyWriterFactory implements StructureWriterFactory {
    private final int indentFactor;
    private final int indent;

    /**
     * Create a new factory for pretty-printing with the given indent factor.
     *
     * @param indentFactor the number of spaces for each indent level
     */
    public PrettyWriterFactory(int indentFactor) {
        this.indentFactor = indentFactor;
        this.indent = 0;
    }

    /**
     * Create a new factory for pretty-printing with the given indent factor
     * and initial indentation.
     *
     * @param indentFactor the number of spaces for each indent level
     * @param indent the initial indentation level
     */
    public PrettyWriterFactory(int indentFactor, int indent) {
        this.indentFactor = indentFactor;
        this.indent = Math.max(0, indent);
    }

    /**
     * Returns a {@code StructureWriter} for writing pretty-printed JSON Array
     * structures.
     *
     * @param parentWriter parameters passed in from the parent writer
     * @return a {@code StructureWriter} for writing a pretty-print JSON Array
     * structure
     */
    @Override
    public StructureWriter newArrayWriter(StructureWriter parentWriter) {
        if (parentWriter instanceof IndentingWriter) {
            return new ArrayPrettyWriter(parentWriter);
        } else {
            return new ArrayPrettyWriter(indentFactor, indent);
        }
    }

    /**
     * Returns a {@code StructureWriter} for writing pretty-printed JSON Object
     * structures.
     *
     * @param parentWriter parameters passed in from the parent writer
     * @return a {@code StructureWriter} for writing a pretty-print JSON Object
     * structure
     */
    @Override
    public StructureWriter newObjectWriter(StructureWriter parentWriter) {
        if (parentWriter instanceof IndentingWriter) {
            return new ObjectPrettyWriter(parentWriter);
        } else {
            return new ObjectPrettyWriter(indentFactor, indent);
        }
    }
}
