package org.json;

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

import java.io.IOException;

/**
 * The {@code JSONAppendable} interface provides a {@code appendJSON()}
 * method so that a class can change the behavior of
 * {@code JSONObject.toString()}, {@code JSONArray.toString()},
 * and {@code JSONWriter.value(Object)}. The
 * {@code appendJSON()} method will be used instead of the default behavior
 * of using the Object's {@code toString()} method and quoting the result.
 * <p>
 * The classes {@link JSONWriter} and {@link org.json.util.BufferedAppendable}
 * may be used to help generate a valid JSON value.
 * </p>
 * <p>
 * Prefer this interface over the {@link JSONString} interface whenever a
 * large value needs to be written. This can avoid excessive buffering or
 * memory usage.
 * </p>
 * @author JSON.org
 * @version 2016-08-28
 * @see JSONString
 */
public interface JSONAppendable {

    /**
     * The {@code appendJSON()} method allows a class to produce its own JSON
     * serialization. If called, a valid JSON value must be written, even if
     * it is an empty object {@code {}}, empty array {@code []}, or a null
     * value {@code null}.
     * <p>
     * If a runtime exception is thrown, this will be caught and propagated
     * as a {@code JSONException}.</p>
     *
     * @param appender an {@code Appendable} to which a strictly syntactically
     * correct JSON value must be written
     */
    void appendJSON(Appendable appender) throws IOException, JSONException;

}
