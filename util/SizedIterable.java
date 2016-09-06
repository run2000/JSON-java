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

/**
 * Adds size information to an {@code Iterable}. For when a {@code Collection}
 * has too big a contract.
 *
 * @author JSON.org
 * @version 2016-08-07
 */
public interface SizedIterable<T> extends Iterable<T> {

    /**
     * Returns the number of elements in this {@code Iterable}.
     *
     * @return the number of elements in the {@code Iterable}
     */
    int size();

    /**
     * Determine whether the {@code Iterable} has any elements.
     *
     * @return {@code true} if this {@code Iterable} contains no elements,
     * otherwise {@code false}
     */
    boolean isEmpty();

}
