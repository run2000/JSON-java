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
import java.util.Iterator;

/**
 * A simple unsynchronized stack implementation, used by {@link JSONStreamReader}.
 * The stack is backed by an {@code ArrayList}.
 *
 * @author JSON.org
 * @version 2016-6-30
 */
public final class ALStack<E> implements Iterable<E> {
    private final ArrayList<E> elements;

    /**
     * Create a new stack object.
     */
    public ALStack() {
        elements = new ArrayList<E>();
    }

    /**
     * Push an element onto the stack.
     *
     * @param elem the element to be pushed
     */
    public void push(E elem) {
        elements.add(elem);
    }

    /**
     * Pop the most recent element off the stack.
     *
     * @return the most recent element
     * @throws EmptyStackException there are no elements on the stack
     */
    public E pop() throws EmptyStackException {
        final int size = elements.size();
        if(size == 0) {
            throw new EmptyStackException();
        }
        return elements.remove(size - 1);
    }

    /**
     * Returns the most recent element on the stack without removing it.
     *
     * @return the most recent element
     * @throws EmptyStackException there are no elements on the stack
     */
    public E peek() throws EmptyStackException {
        final int size = elements.size();
        if(size == 0) {
            throw new EmptyStackException();
        }
        return elements.get(size - 1);
    }

    /**
     * Determine whether the stack is currently empty.
     *
     * @return {@code true} if the stack is empty, otherwise {@code false}
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * Determine the number of elements on the stack.
     *
     * @return the number of elements on the stack
     */
    public int size() {
        return elements.size();
    }

    /**
     * Returns a string representing all the elements on the stack.
     *
     * @return a string value of the stack elements
     */
    @Override
    public String toString() {
        return elements.toString();
    }

    /**
     * Returns an unmodifiable iterator over all the elements on the stack.
     *
     * @return an Iterator of elements on the stack
     */
    @Override
    public Iterator<E> iterator() {
        return new ALIterator<>(elements);
    }

    /**
     * Iterator that operates over the backing array list. The list is
     * unmodifiable from the iterator. The iterator propagates any
     * concurrent modification exceptions thrown from the backing list.
     *
     * @param <E> the type of elements to be iterated over
     */
    private static final class ALIterator<E> implements Iterator<E> {
        private final Iterator<E> iterator;

        public ALIterator(ArrayList<E> backingList) {
            this.iterator = backingList.iterator();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public E next() {
            return iterator.next();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }
    }
}
