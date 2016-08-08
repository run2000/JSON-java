package org.json.util;

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
     * @return the number of element
     */
    int size();

    /**
     * Determine whether the {@code Iterable} has any elements.
     *
     * @return {@code true} if this collection contains no elements, otherwise
     * {@code false}
     */
    boolean isEmpty();

}
