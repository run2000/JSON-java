package org.json.stream;

import java.io.IOException;

/**
 * A dummy sink into which Appendable content may be sent. This is used
 * for skipping over value content which needs to be parsed, but not emitted.
 *
 * @author JSON.org
 * @version 2016-06-08
 */
public final class NullAppendable implements Appendable {

    public static final NullAppendable INSTANCE = new NullAppendable();

    private NullAppendable() {
    }

    @Override
    public Appendable append(CharSequence csq) throws IOException {
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        return this;
    }

    @Override
    public Appendable append(char c) throws IOException {
        return this;
    }
}
