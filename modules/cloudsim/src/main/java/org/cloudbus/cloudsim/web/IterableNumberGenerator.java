package org.cloudbus.cloudsim.web;

import org.uncommons.maths.number.NumberGenerator;

import java.util.Iterator;

/**
 * A number generator that uses a collection as a source for the generated
 * numbers.
 * 
 * @author nikolay.grozev
 * 
 * @param <N>
 */
public class IterableNumberGenerator<N extends Number> implements NumberGenerator<N> {

    private final Iterator<N> iter;

    public IterableNumberGenerator(final Iterable<N> collection) {
        super();
        this.iter = collection.iterator();
    }

    @Override
    public N nextValue() {
        return iter.hasNext() ? iter.next() : null;
    }

}
