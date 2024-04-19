package org.cloudbus.cloudsim.web;

import java.util.Arrays;
import java.util.Iterator;

/**
 * A generator with a collection of in-memory pregenerated objects.
 * 
 * @author nikolay.grozev
 * 
 * @param <T>
 *            - the type of the generated elements.
 */
public class IterableGenerator<T> implements IGenerator<T> {

    private final Iterator<T> iterator;
    private T peeked;

    /**
     * Creates the iterable generator with the list of prefetched instances.
     * 
     * @param collection
     */
    @SafeVarargs
    public IterableGenerator(T... collection) {
        this(Arrays.asList(collection));
    }

    /**
     * Creates the iterable generator with the list of prefetched instances.
     * 
     * @param collection
     */
    public IterableGenerator(final Iterable<T> collection) {
        this.iterator = collection.iterator();
    }

    /**
     * (non-Javadoc)
     * 
     * @see IGenerator#peek()
     */
    @Override
    public T peek() {
        peeked = peeked == null && iterator.hasNext() ? iterator.next() : peeked;
        return peeked;
    }

    /**
     * (non-Javadoc)
     * 
     * @see IGenerator#poll()
     */
    @Override
    public T poll() {
        T result = peeked;
        if (peeked != null) {
            peeked = null;
        } else if (iterator.hasNext()) {
            result = iterator.next();
        }
        return result;
    }

    /**
     * (non-Javadoc)
     * 
     * @see IGenerator#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return peek() == null;
    }

    /**
     * (non-Javadoc)
     * 
     * @see IGenerator#notifyOfTime(double)
     */
    @Override
    public void notifyOfTime(final double time) {
        // Do nothing
    }

}
