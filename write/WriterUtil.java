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
import org.json.JSONObject;
import org.json.JSONString;

import java.io.IOException;
import java.util.Arrays;

/**
 * Static writer methods.
 *
 * @author JSON.org
 * @version 2016-09-14
 */
public final class WriterUtil {

    // 120 spaces, divides by 1, 2, 3, 4, 5, 6, 8, 10, 12, 15, 20, ...
    private static final String PADDING_SPACES = makePaddingSpaces();
    private static final int PADDING_LENGTH = 120;

    private static String makePaddingSpaces() {
        char[] padding = new char[PADDING_LENGTH];
        Arrays.fill(padding, ' ');
        return String.valueOf(padding);
    }

    private WriterUtil() {
    }

    /**
     * Indent by the given number of spaces.
     *
     * @param indent
     *            the number of character to indent.
     * @param writer
     *            the writer.
     * @throws IOException there was a problem writing the indentation
     */
    public static void indent(int indent, Appendable writer) throws IOException {

        while(indent >= PADDING_LENGTH) {
            writer.append(PADDING_SPACES);
            indent -= PADDING_LENGTH;
        }
        if(indent > 0) {
            writer.append(PADDING_SPACES, 0, indent);
        }
    }

    /**
     * Is this value a simple value for the purposes of writing a JSON structure.
     *
     * @param value the Object value to be tested
     * @return {@code true} if this value is a simple JSON value, otherwise
     * {@code false}
     */
    public static boolean isSimpleValue(Object value) {
        if ((value instanceof JSONAppendable) || (value instanceof JSONString)) {
            return false;
        }
        return ((value == null) ||
                (value instanceof Number) ||
                (value instanceof Boolean) ||
                (value instanceof CharSequence) ||
                (value instanceof Enum<?>) ||
                JSONObject.NULL.equals(value));
    }

    /**
     * Write the contents of the {@code Object} as JSON text to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param value
     *            The value to be written
     * @param writer
     *            Writes the serialized JSON
     * @throws JSONException there was a problem writing the {@code Object}
     */
    public static void writeSimpleValue(Object value, Appendable writer)
            throws JSONException {

        if (value == null) {
            writeNull(writer);
        } else if (value instanceof JSONAppendable) {
            writeJSONAppendable((JSONAppendable) value, writer);
        } else if (value instanceof JSONString) {
            writeJSONString((JSONString) value, writer);
        } else if (value instanceof Number) {
            writeNumber((Number) value, writer);
        } else if (value instanceof Boolean) {
            writeBoolean((Boolean) value, writer);
        } else if (value instanceof CharSequence) {
            writeString((CharSequence) value, writer);
        } else if (value instanceof Enum<?>) {
            writeEnum((Enum<?>) value, writer);
        } else if (JSONObject.NULL.equals(value)) {
            writeNull(writer);
        } else {
            writeString(value.toString(), writer);
        }
    }

    /**
     * Write the {@code JSONAppendable} as a JSON value to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param value
     *            The {@code JSONAppendable} to be written
     * @param writer
     *            Writes the serialized JSON
     * @throws JSONException there was a problem writing the {@code JSONAppendable}
     */
    public static void writeJSONAppendable(JSONAppendable value,
            Appendable writer) throws JSONException {
        try {
            value.appendJSON(writer);
        } catch(JSONException e) {
            // Propagate directly, because JSONException is a RuntimeException
            throw e;
        } catch(IOException e) {
            throw new JSONException(e);
        } catch(RuntimeException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Write the contents of the {@code JSONString} as a JSON value to a writer.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param value
     *            The {@code JSONString} to be written
     * @param writer
     *            Writes the serialized JSON
     * @throws JSONException there was a problem writing the {@code JSONString}
     */
    public static void writeJSONString(JSONString value, Appendable writer)
            throws JSONException {
        try {
            String o = value.toJSONString();
            if (o != null) {
                writer.append(o);
            } else {
                writeString(value.toString(), writer);
            }
        } catch(JSONException e) {
            // Propagate directly, because JSONException is a RuntimeException
            throw e;
        } catch (IOException e) {
            throw new JSONException(e);
        } catch (RuntimeException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Write the given {@code Enum} to the given {@code Appendable} as a
     * {@code String}.
     *
     * @param value
     *            An {@code Enum}
     * @param writer
     *            The {@code Appendable} to which the {@code Enum} value is
     *            written
     * @throws JSONException there was a problem writing the {@code Enum}
     */
    public static void writeEnum(Enum<?> value, Appendable writer)
            throws JSONException {
        writeString(value.name(), writer);
    }

    /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places. A backslash will be inserted within &lt;/, producing &lt;\/,
     * allowing JSON text to be delivered in HTML. In JSON text, a string cannot
     * contain a control character or an unescaped quote or backslash.
     *
     * @param string
     *            A character sequence to be quoted
     * @param w
     *            the {@code Appendable} to which the quoted character sequence
     *            will be written
     * @throws JSONException there was a problem writing to the {@code Appendable}
     */
    public static void writeString(CharSequence string, Appendable w)
            throws JSONException {
        try {
            if (string == null || string.length() == 0) {
                w.append("\"\"");
                return;
            }

            char b;
            char c = 0;
            String hhhh;
            int i;
            int len = string.length();
            int prev = 0;

            w.append('"');
            for (i = 0; i < len; i += 1) {
                b = c;
                c = string.charAt(i);
                switch (c) {
                    case '\\':
                    case '"':
                        if(prev < i) {
                            w.append(string, prev, i);
                        }
                        w.append('\\');
                        prev = i;
                        break;
                    case '/':
                        if (b == '<') {
                            if(prev < i) {
                                w.append(string, prev, i);
                            }
                            w.append('\\');
                            prev = i;
                        }
                        break;
                    case '\b':
                        if(prev < i) {
                            w.append(string, prev, i);
                        }
                        w.append("\\b");
                        prev = i + 1;
                        break;
                    case '\t':
                        if(prev < i) {
                            w.append(string, prev, i);
                        }
                        w.append("\\t");
                        prev = i + 1;
                        break;
                    case '\n':
                        if(prev < i) {
                            w.append(string, prev, i);
                        }
                        w.append("\\n");
                        prev = i + 1;
                        break;
                    case '\f':
                        if(prev < i) {
                            w.append(string, prev, i);
                        }
                        w.append("\\f");
                        prev = i + 1;
                        break;
                    case '\r':
                        if(prev < i) {
                            w.append(string, prev, i);
                        }
                        w.append("\\r");
                        prev = i + 1;
                        break;
                    default:
                        if (c < ' ' || (c >= '\u0080' && c < '\u00a0')
                                || (c >= '\u2000' && c < '\u2100')) {
                            if(prev < i) {
                                w.append(string, prev, i);
                            }
                            hhhh = Integer.toHexString(c);
                            w.append("\\u0000", 0, 6 - hhhh.length());
                            w.append(hhhh);
                            prev = i + 1;
                        }
                }
            }
            if(prev == 0) {
                w.append(string);
            } else if(prev < len) {
                w.append(string, prev, len);
            }
            w.append('"');
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Write the given {@code double} to the given {@code Appendable}.
     *
     * @param d
     *            A double
     * @param writer
     *            The {@code Appendable} to which the double value is written
     * @throws JSONException there was a problem writing the double
     */
    public static void writeDouble(double d, Appendable writer)
            throws JSONException {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            writeNull(writer);
        } else {
            // Shave off trailing zeros and decimal point, if possible.
            String string = Double.toString(d);
            writeNumberDigits(string, writer);
        }
    }

    /**
     * Write the given {@code long} to the given {@code Appendable}.
     *
     * @param number
     *            A long
     * @param writer
     *            The {@code Appendable} to which the number value is written
     * @throws JSONException there was a problem writing the long
     */
    public static void writeLong(long number, Appendable writer)
            throws JSONException {
        try {
            // Shave off trailing zeros and decimal point, if possible.
            writer.append(Long.toString(number));
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Write the given {@code Number} to the given {@code Appendable}.
     *
     * @param number
     *            A {@code Number}
     * @param writer
     *            The {@code Appendable} to which the number value is written
     * @throws JSONException there was a problem writing the {@code Number}
     */
    public static void writeNumber(Number number, Appendable writer)
            throws JSONException {

        if ((number instanceof Double) || (number instanceof Float)) {
            double d = number.doubleValue();
            if (Double.isInfinite(d) || Double.isNaN(d)) {
                writeNull(writer);
                return;
            }
        }

        // Shave off trailing zeros and decimal point, if possible.
        String string = number.toString();
        writeNumberDigits(string, writer);
    }

    /**
     * Write a number value, trimming any trailing zero digits from a real number.
     * If all zeros appear immediately after a decimal, the decimal is omitted
     * as well.
     *
     * @param string the string of digits
     * @param writer the {@code Appendable} to which the digits will be written
     * @throws JSONException there was a problem writing to the {@code Appendable}
     */
    private static void writeNumberDigits(String string,
            Appendable writer) throws JSONException {
        try {
            int decimal = string.indexOf('.');
            if (decimal > 0 && string.indexOf('e', decimal) < 0
                    && string.indexOf('E', decimal) < 0) {
                final int len = string.length();
                int last = len;
                while ((last > 0) && (string.charAt(last - 1) == '0')) {
                    last--;
                }
                if (decimal == (last - 1)) {
                    last--;
                }
                if (last < len) {
                    writer.append(string, 0, last);
                    return;
                }
            }

            writer.append(string);
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Write the given {@code Boolean} to the given {@code Appendable}.
     *
     * @param value
     *            A {@code Boolean}
     * @param writer
     *            The {@code Appendable} to which the boolean value is written
     * @throws JSONException there was a problem writing the {@code Boolean}
     */
    public static void writeBoolean(Boolean value, Appendable writer)
            throws JSONException {
        try {
            writer.append(value.toString());
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Write the given {@code boolean} to the given {@code Appendable}.
     *
     * @param value
     *            A boolean
     * @param writer
     *            The {@code Appendable} to which the boolean value is written
     * @throws JSONException there was a problem writing the boolean
     */
    public static void writeBoolean(boolean value, Appendable writer)
            throws JSONException {
        try {
            writer.append(Boolean.toString(value));
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Write the value {@code null} to the given {@code Appendable}.
     *
     * @param writer
     *            The {@code Appendable} to which the null value is written
     * @throws JSONException there was a problem writing null
     */
    public static void writeNull(Appendable writer) throws JSONException {
        try {
            writer.append("null");
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }
}
