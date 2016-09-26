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
import java.io.OutputStream;
import java.nio.CharBuffer;

/**
 * Adapts an {@code java.io.OutputStream} of Latin1 bytes onto an {@code Appendable}
 * sink. A modest buffer is used to batch up append operations.
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
 * @version 2016-09-03.
 */
public final class Latin1AppendableOutputStream extends OutputStream {
    /** The default buffer size of 1024 characters, if none is specified. */
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    private final CharBuffer buffer;
    private Appendable appendable;

    /**
     * Adapt a given {@code Appendable} with the default buffer size.
     */
    public Latin1AppendableOutputStream() {
        buffer = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Adapt a given {@code Appendable} with the given buffer size.
     * The size must be at least 16.
     *
     * @param buffSize   the buffer size, must be &gt;= 16
     * @throws NullPointerException the supplied Appendable is null
     */
    public Latin1AppendableOutputStream(int buffSize) {
        if (buffSize < 16) {
            throw new IllegalArgumentException("Buffsize should be at least 16");
        }
        this.buffer = CharBuffer.allocate(buffSize);
    }

    /**
     * Check that this buffer is currently open.
     *
     * @throws IOException the buffered appendable is in a closed state
     */
    private void assertOpen() throws IOException {
        if (appendable == null) {
            throw new IOException("Latin1 appendable is not open");
        }
    }

    /**
     * Reset this latin1 appendable, setting it to buffer the given
     * appendable. Any existing buffered output is flushed to the old
     * {@code Appendable} first.
     * <p>
     * This resets the state of the buffered reader to the open state
     * if the new appendable is non-{@code null}, otherwise resets the state to
     * closed.</p>
     *
     * @param newAppendable the new {@code Appendable} to buffer
     * @return this {@code Latin1AppendableOutputStream}
     */
    public Latin1AppendableOutputStream with(Appendable newAppendable) {
        try {
            flushBuffer();
        } catch (IOException e) {
            // don't care, just set the new appender
        }
        this.appendable = newAppendable;
        return this;
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
     * Writes the specified byte to the underlying {@code Appendable} as a
     * Latin1 character.
     *
     * @param    b  the {@code byte}.
     * @throws   IOException  if an I/O error occurs. In particular,
     *           an {@code IOException} may be thrown if the
     *           underlying {@code Appendable} has been closed.
     */
    @Override
    public void write(int b) throws IOException {
        if (appendable == null) {
            throw new IOException("Latin1 appendable is not open");
        }
        if (buffer.remaining() < 1) {
            flushBuffer();
        }
        buffer.put((char)(b & 255));
    }

    /**
     * Writes {@code len} bytes from the specified byte array
     * starting at offset {@code off} to the underlying {@code Appendable}
     * as Latin1 characters.
     * <p>
     * If {@code b} is {@code null}, a {@code NullPointerException} is thrown.
     * <p>
     * If {@code off} is negative, or <code>len</code> is negative, or
     * {@code off+len} is greater than the length of the array
     * {@code b}, then an {@code IndexOutOfBoundsException} is thrown.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @throws     IOException  if an I/O error occurs. In particular,
     *             an {@code IOException} is thrown if the underlying
     *             {@code Appendable} is closed.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if ((off > b.length) || (off < 0) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        assertOpen();

        int count = Math.min(len, buffer.remaining());
        while(len > 0) {
            for (int i = 0; i < count; i++) {
                buffer.put((char) ((b[off + i] + 256) & 255));
            }
            if (buffer.remaining() < 1) {
                flushBuffer();
            }
            len -= count;
            off += count;
            count = Math.min(len, buffer.remaining());
        }
    }

    /**
     * Flush the buffer of this {@code Latin1AppendableOutputStream}.
     *
     * @throws IOException there was a problem appending to the underlying
     *                     {@code Appendable}
     */
    @Override
    public void flush() throws IOException {
        flushBuffer();
    }

    /**
     * Flush and close the buffer of this {@code Latin1AppendableOutputStream}.
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