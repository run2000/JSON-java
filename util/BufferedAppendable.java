package org.json.util;

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
import java.io.Writer;
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
 * to the wrapped {@code Appendable}.</p>
 *
 * @author JSON.org
 * @version 2016-07-08
 */
public final class BufferedAppendable extends Writer {
    /** The default buffer size of 1024 characters, if none is specified. */
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final String NULL_SEQ = "null";
    private final CharBuffer buffer;
    private Appendable appendable;

    /**
     * Buffer a given {@code Appendable} with the default buffer size.
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
     * {@code Appendable} first.
     * <p>
     * This resets the state of the buffered reader to the open state
     * if the new appendable is non-{@code null}, otherwise resets the state to
     * closed.</p>
     *
     * @param newAppendable the new {@code Appendable} to buffer
     * @return this {@code BufferedAppendable}
     */
    public BufferedAppendable with(Appendable newAppendable) {
        try {
            flushBuffer();
        } catch (IOException e) {
            // don't care, just set the new appender
        } catch (RuntimeException e) {
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
     * @return this {@code BufferedAppendable}
     * @throws IOException there was a problem appending to the underlying
     *                     appendable
     */
    @Override
    public BufferedAppendable append(CharSequence csq) throws IOException {
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
     * @return this {@code BufferedAppendable}
     * @throws IOException there was a problem appending to the underlying
     *                     {@code Appendable}
     */
    @Override
    public BufferedAppendable append(CharSequence csq, int start, int end) throws IOException {
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
     * @return this {@code BufferedAppendable}
     * @throws IOException there was a problem appending to the underlying
     *                     {@code Appendable}
     */
    @Override
    public BufferedAppendable append(char c) throws IOException {
        if (appendable == null) {
            throw new IOException("Buffered appendable is not open");
        }
        if (! buffer.hasRemaining()) {
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
                buffer.flip();
                appendable.append(buffer, 0, pos);
            } finally {
                buffer.clear();
            }
        }
    }

    // -- Fulfill the Writer contract in terms of Appendable, above --

    /**
     * Writes a single character.  The character to be written is contained in
     * the 16 low-order bits of the given integer value; the 16 high-order bits
     * are ignored.
     *
     * @param  c int specifying a character to be written
     * @throws  IOException If an I/O error occurs
     */
    @Override
    public void write(int c) throws IOException {
        // inline for single char append
        if (appendable == null) {
            throw new IOException("Buffered appendable is not open");
        }
        if (! buffer.hasRemaining()) {
            flushBuffer();
        }
        buffer.append((char)c);
    }

    /**
     * Writes an array of characters.
     *
     * @param  cbuf Array of characters to be written
     *
     * @throws  IOException If an I/O error occurs
     * @throws  NullPointerException cbuf is {@code null}
     */
    @Override
    public void write(char[] cbuf) throws IOException {
        if(cbuf.length > 0) {
            append(CharBuffer.wrap(cbuf));
        }
    }

    /**
     * Writes a portion of an array of characters.
     *
     * @param  cbuf Array of characters
     * @param  off Offset from which to start writing characters
     * @param  len Number of characters to write
     * @throws  IOException If an I/O error occurs
     * @throws  NullPointerException cbuf is {@code null}
     * @throws  IndexOutOfBoundsException off or len are out of bounds
     */
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        append(CharBuffer.wrap(cbuf, off, len));
    }

    /**
     * Writes a string.
     *
     * @param  str String to be written
     * @throws  IOException If an I/O error occurs
     * @throws  NullPointerException str is {@code null}
     */
    @Override
    public void write(String str) throws IOException {
        if(str.length() > 0) {
            append(str);
        }
    }

    /**
     * Writes a portion of a string.
     *
     * @param  str A String
     * @param  off Offset from which to start writing characters
     * @param  len Number of characters to write
     * @throws  IndexOutOfBoundsException
     *          If {@code off} is negative, or {@code len} is negative,
     *          or {@code off+len} is negative or greater than the length
     *          of the given string
     * @throws  IOException If an I/O error occurs
     * @throws  NullPointerException str is {@code null}
     */
    @Override
    public void write(String str, int off, int len) throws IOException {
        if ((off < 0) || (off > str.length()) || (len < 0) ||
                ((off + len) > str.length()) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        append(str, off, off + len);
    }

    /**
     * Flush the buffer of this {@code BufferedAppendable}.
     *
     * @throws IOException there was a problem appending to the underlying
     *                     {@code Appendable}
     */
    @Override
    public void flush() throws IOException {
        flushBuffer();
    }

    /**
     * Flush and close the buffer of this {@code BufferedAppendable}.
     *
     * @throws IOException there was a problem appending to the underlying
     *                     {@code Appendable}
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
