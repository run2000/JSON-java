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

import java.util.ArrayList;
import java.util.EmptyStackException;

/**
 * A simple unsynchronized stack implementation, used by {@link JSONStreamReader}.
 * The stack is backed by an array list.
 *
 * @author JSON.org
 * @version 2016-6-30
 */
public final class ALStack<E> {
    private final ArrayList<E> elements;

    public ALStack() {
        elements = new ArrayList<E>();
    }

    public void push(E elem) {
        elements.add(elem);
    }

    public E pop() throws EmptyStackException {
        final int size = elements.size();
        if(size == 0) {
            throw new EmptyStackException();
        }
        return elements.remove(size - 1);
    }

    public E peek() throws EmptyStackException {
        final int size = elements.size();
        if(size == 0) {
            throw new EmptyStackException();
        }
        return elements.get(size - 1);
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public int size() {
        return elements.size();
    }

    @Override
    public String toString() {
        return elements.toString();
    }
}
