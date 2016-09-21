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
 * Factory interface for creating {@code StructureWriter} JSON Object and
 * JSON Array writers.
 *
 * @author JSON.org
 * @version 2016-09-14
 */
public interface StructureWriterFactory {

    /**
     * Returns a {@code StructureWriter} for writing JSON Array structures.
     *
     * @param parentWriter parameters passed in from the parent writer
     * @return a {@code StructureWriter} for writing a JSON Array structure
     */
    StructureWriter newArrayWriter(StructureWriter parentWriter);

    /**
     * Returns a {@code StructureWriter} for writing JSON Object structures.
     *
     * @param parentWriter parameters passed in from the parent writer
     * @return a {@code StructureWriter} for writing a JSON Object structure
     */
    StructureWriter newObjectWriter(StructureWriter parentWriter);

}
