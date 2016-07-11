package org.json.stream;

/**
 * A dummy sink into which {@code Appendable} content may be sent. This is used
 * for skipping over value content which needs to be parsed, but not emitted.
 *
 * @author JSON.org
 * @version 2016-06-08
 */
public final class NullAppendable implements Appendable {

    /** The single instance of {@code NullAppendable}. */
    public static final NullAppendable INSTANCE = new NullAppendable();

    private NullAppendable() {
    }

    /**
     * Ignores the given character sequence.
     *
     * @param  csq
     *         The character sequence to append.
     *
     * @return  A reference to this {@code NullAppendable}
     */
    @Override
    public Appendable append(CharSequence csq) {
        return this;
    }

    /**
     * Ignores the given character sequence.
     *
     * @param  csq
     *         The character sequence from which a subsequence will be
     *         appended.
     *
     * @param  start
     *         The index of the first character in the subsequence
     *
     * @param  end
     *         The index of the character following the last character in the
     *         subsequence
     *
     * @return  A reference to this {@code NullAppendable}
     *
     */
    @Override
    public Appendable append(CharSequence csq, int start, int end) {
        return this;
    }

    /**
     * Ignores the specified character.
     *
     * @param  c
     *         The character to append
     *
     * @return  A reference to this {@code NullAppendable}
     */
    @Override
    public Appendable append(char c) {
        return this;
    }
}
