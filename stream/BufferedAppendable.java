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

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.nio.CharBuffer;

/**
 * A simple buffering {@code Appendable}, without the synchronization of
 * {@code java.io.BufferedWriter}. This means that all operations on a
 * {@code BufferedAppendable} object must be performed synchronously.
 * <p>
 * In addition, the {@code Appendable} to be buffered is supplied using the
 * {@link #with(Appendable)} method, rather than at construction time. This
 * allows the buffer to be reused for several different operations requiring
 * buffering.</p>
 * <p>
 * Uses {@code java.nio.CharBuffer.allocate()} to create the backing buffer.</p>
 * <p>
 * Does <em>not</em> propagate the {@code flush()} or {@code close()} methods
 * to the wrapped Appendable.</p>
 *
 * @author run2000
 * @version 2016-7-8
 */
public final class BufferedAppendable implements Appendable, Flushable, Closeable {
    /** The default buffer size of 1024 characters, if none is specified. */
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final String NULL_SEQ = "null";
    private final CharBuffer buffer;
    private Appendable appendable;

    /**
     * Buffer a given {@code Appendable} with the default buffer size.
     *
     * @throws NullPointerException the supplied Appendable is null
     */
    public BufferedAppendable() {
        this.buffer = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Buffer a given {@code Appendable} with the given buffer size.
     * The size must be at least 16.
     *
     * @param buffSize   the buffer size, must be &gt;= 16
     * @throws NullPointerException the supplied Appendable is null
     */
    public BufferedAppendable(int buffSize) {
        if (buffSize < 16) {
            throw new IllegalArgumentException("Buffsize should be at least 16");
        }
        this.buffer = CharBuffer.allocate(buffSize);
    }

    /**
     * Reset this buffered appendable, setting it to buffer the given
     * appendable. Any existing buffered output is flushed to the old
     * Appendable first.
     * <p>
     * This resets the state of the buffered reader to the open state
     * if the new appendable is non-{@code null}, otherwise resets the state to
     * closed.</p>
     *
     * @param newAppendable the new Appendable to buffer
     * @return this BufferedAppendable
     */
    public BufferedAppendable with(Appendable newAppendable) {
        try {
            flushBuffer();
        } catch (IOException e) {
            // don't care, just set the new appender
        }
        this.appendable = newAppendable;
        return this;
    }

    /**
     * Appends the specified character sequence to this
     * {@code BufferedAppendable}.
     *
     * @param csq the character sequence to be appended
     * @return this BufferedAppendable
     * @throws IOException there was a problem appending to the underlying
     *                     appendable
     */
    @Override
    public Appendable append(CharSequence csq) throws IOException {
        if (csq == null) {
            csq = NULL_SEQ;
        }
        if (csq.length() > 0) {
            assertOpen();
            if (csq.length() < buffer.remaining()) {
                buffer.append(csq);
            } else {
                flushBuffer();
                if (csq.length() < buffer.length()) {
                    buffer.append(csq);
                } else {
                    appendable.append(csq);
                }
            }
        }
        return this;
    }

    /**
     * Appends a subsequence of the specified character sequence to this
     * {@code BufferedAppendable}.
     *
     * @param csq   The character sequence from which a subsequence will be
     *              appended.
     * @param start The index of the first character in the subsequence
     * @param end   The index of the character following the last character in the
     *              subsequence
     * @return this BufferedAppendable
     * @throws IOException there was a problem appending to the underlying
     *                     appendable
     */
    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        final int len = end - start;

        if ((start < 0) || (len < 0)) {
            throw new IllegalArgumentException("start or end out of bounds");
        }
        if (csq == null) {
            csq = NULL_SEQ;
        }
        if (end > csq.length()) {
            throw new IllegalArgumentException("end index out of bounds");
        }
        if (len > 0) {
            assertOpen();
            if (len < buffer.remaining()) {
                buffer.append(csq, start, end);
            } else {
                flushBuffer();
                if (len < buffer.length()) {
                    buffer.append(csq, start, end);
                } else {
                    appendable.append(csq, start, end);
                }
            }
        }
        return this;
    }

    /**
     * Appends the specified character to this {@code BufferedAppendable}.
     *
     * @param c The character to append
     * @return this BufferedAppendable
     * @throws IOException there was a problem appending to the underlying
     *                     appendable
     */
    @Override
    public Appendable append(char c) throws IOException {
        assertOpen();
        if (buffer.remaining() < 1) {
            flushBuffer();
        }
        buffer.append(c);
        return this;
    }

    /**
     * Check that this buffer is currently open.
     *
     * @throws IOException the buffered appendable is in a closed state
     */
    private void assertOpen() throws IOException {
        if (appendable == null) {
            throw new IOException("Buffered appendable is not open");
        }
    }

    /**
     * Flushes and clears the internal buffer. An {@code assertOpen()} is
     * called if the buffer needs to be written to the current
     * {@code Appendable}.
     *
     * @throws IOException there was a problem flushing the buffer to the
     *                     current {@code Appendable}
     */
    private void flushBuffer() throws IOException {
        final int pos = buffer.position();
        if(pos > 0) {
            try {
                assertOpen();
                buffer.rewind();
                appendable.append(buffer, 0, pos);
            } finally {
                buffer.clear();
            }
        }
    }

    /**
     * Flush the buffer of this {@code BufferedAppendable}.
     *
     * @throws IOException there was a problem appending to the underlying
     *                     appendable
     */
    @Override
    public void flush() throws IOException {
        flushBuffer();
    }

    /**
     * Flush and close the buffer of this {@code BufferedAppendable}.
     *
     * @throws IOException there was a problem appending to the underlying
     *                     appendable
     */
    @Override
    public void close() throws IOException {
        if (appendable != null) {
            try {
                flushBuffer();
            } finally {
                appendable = null;
            }
        }
    }
}
