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

/**
 * Utilities for conversion an iterable of {@link org.json.stream.StructureBuilder} objects
 * into a JSON Pointer expression. See RFC 6901 for details of JSON Pointer.
 * <p>
 * A Java 8 implementation might fold these methods directly into the
 * {@code StructureIdentifier} interface.</p>
 *
 * @author JSON.org
 * @version 2016-08-02
 * @see StructureIdentifier
 */
public final class JSONPointerUtils {

    private JSONPointerUtils() {
    }

    /**
     * Encode a key according to the JSON Pointer syntax. See RFC 6901
     * section 3 for details.
     *
     * @param builder the string builder to which the encoded value will
     *                be appended
     * @param name the key to be encoded
     */
    private static void encodePointer(StringBuilder builder, String name) {
        final int len = (name == null) ? 0 : name.length();
        if(len == 0) {
            return;
        }

        int prev = 0;
        int curr;

        for(curr = 0; curr < len; curr++) {
            char c = name.charAt(curr);
            switch(c) {
                case '~':
                    if(prev < curr) {
                        builder.append(name, prev, curr);
                    }
                    builder.append("~0");
                    prev = curr + 1;
                    break;
                case '/':
                    if(prev < curr) {
                        builder.append(name, prev, curr);
                    }
                    builder.append("~1");
                    prev = curr + 1;
                    break;
            }
        }
        if(prev == 0) {
            builder.append(name);
        } else if(prev < curr) {
            builder.append(name, prev, curr);
        }
    }

    /**
     * Given a {@link org.json.stream.StructureBuilder} stack as an {@code Iterable},
     * create a JSON Pointer string.
     *
     * @param stack a stack of StructureBuilder objects, from which the
     *              JSON Pointer is created
     * @return an encoded JSON Pointer
     */
    public static String toJSONPointer(Iterable<StructureIdentifier> stack) {
        if(stack == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for(StructureIdentifier item : stack) {
            builder.append('/');
            encodePointer(builder, item.getIdentifier());
        }
        return builder.toString();
    }

}
