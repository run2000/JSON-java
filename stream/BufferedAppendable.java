package org.json.stream;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.nio.CharBuffer;

/**
 * A simple buffering {@code Appendable}, without the synchronization of
 * {@code java.io.BufferedWriter}. In addition, the Appendable is supplied
 * using the {@link #with(Appendable)} method, rather than at construction
 * time. This allows the buffer to be reused for several different
 * operations requiring buffering.
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
    /** The default buffer size. */
    public static final int BUFFER_SIZE = 2048;
    private static final String NULL_SEQ = "null";
    private final CharBuffer buffer;
    private Appendable appendable;

    /**
     * Buffer a given {@code Appendable} with the default buffer size.
     *
     * @throws NullPointerException the supplied Appendable is null
     */
    public BufferedAppendable() {
        this.buffer = CharBuffer.allocate(BUFFER_SIZE);
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
     * This resets the state of thie buffered reader to the open state
     * if the new appendable is non-null, otherwise resets the state to
     * closed.</p>
     *
     * @param newAppendable the new Appendable to buffer
     * @return this BufferedAppendable
     */
    public BufferedAppendable with(Appendable newAppendable) {
        try {
            close();
        } catch (IOException e) {
            // don't care, just set the new appender
        }
        this.appendable = newAppendable;
        return this;
    }

    /**
     * Appends the specified character sequence to this buffered appendable.
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
                final int pos = buffer.position();
                if (pos > 0) {
                    buffer.rewind();
                    appendable.append(buffer, 0, pos);
                    buffer.clear();
                }
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
     * buffered appendable.
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
                final int pos = buffer.position();
                if (pos > 0) {
                    buffer.rewind();
                    appendable.append(buffer, 0, pos);
                    buffer.clear();
                }
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
     * Appends the specified character to this buffered appendable.
     *
     * @param c The character to append
     * @return this BufferedAppendable
     * @throws IOException there was a problem appending to the underlying
     *                     appendable
     */
    @Override
    public Appendable append(char c) throws IOException {
        assertOpen();
        if (buffer.remaining() >= 1) {
            buffer.append(c);
        } else {
            final int pos = buffer.position();
            if (pos > 0) {
                buffer.rewind();
                appendable.append(buffer, 0, pos);
                buffer.clear();
            }
            buffer.append(c);
        }
        return this;
    }

    /**
     * Check that this buffer has not been closed.
     *
     * @throws IOException the buffered appendable has been closed
     */
    private void assertOpen() throws IOException {
        if (appendable == null) {
            throw new IOException("Buffered appendable is not open");
        }
    }

    /**
     * Flush the buffer of this BufferedAppendable.
     *
     * @throws IOException there was a problem appending to the underlying
     *                     appendable
     */
    @Override
    public void flush() throws IOException {
        final int pos = buffer.position();
        if (pos > 0) {
            assertOpen();
            buffer.rewind();
            appendable.append(buffer, 0, pos);
            buffer.clear();
        }
    }

    /**
     * Flush and close the buffer of this buffered appendable.
     *
     * @throws IOException there was a problem appending to the underlying
     *                     appendable
     */
    @Override
    public void close() throws IOException {
        if (appendable != null) {
            try {
                flush();
            } finally {
                appendable = null;
            }
        }
    }
}
