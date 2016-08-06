package org.json.util;

/**
 * Adds size information to an {@code Iterable}. For when a {@code Collection}
 * has too big a contract.
 *
 * @author JSON.org
 * @version 2016-08-07
 */
public interface SizedIterable<T> extends Iterable<T> {

    int size();

    boolean isEmpty();

}
