package org.cloudbus.cloudsim.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 
 * A composite generator, that delegates to a list of other generators.
 * 
 * @author nikolay.grozev
 * 
 * @param <T>
 *            - the type of the element generated by the nested generators
 */
public class CompositeGenerator<T> implements IGenerator<Collection<T>> {

    private final Collection<IGenerator<T>> generators;

    /**
     * Constr.
     * 
     * @param generators
     *            - the generators to delegate to. Must not be null, must not
     *            contain nulls and must not be empty.
     */
    public CompositeGenerator(final Collection<IGenerator<T>> generators) {
        this.generators = generators;
    }

    /**
     * Constr.
     * 
     * @param generators
     *            - the generators to delegate to. Must not be null, must not
     *            contain nulls and must not be empty.
     */
    @SafeVarargs
    public CompositeGenerator(final IGenerator<T>... generators) {
        this(Arrays.asList(generators));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cloudbus.cloudsim.incubator.web.IGenerator#peek()
     */
    @Override
    public Collection<T> peek() {
        List<T> result = new ArrayList<>();
        for (IGenerator<T> gen : generators) {
            T peeked = gen.peek();
            if (peeked != null) {
                result.add(peeked);
            }
        }
        return result.isEmpty() ? null : result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cloudbus.cloudsim.incubator.web.IGenerator#poll()
     */
    @Override
    public Collection<T> poll() {
        List<T> result = new ArrayList<>();
        for (IGenerator<T> gen : generators) {
            T polled = gen.poll();
            if (polled != null) {
                result.add(polled);
            }
        }
        return result.isEmpty() ? null : result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cloudbus.cloudsim.incubator.web.IGenerator#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        for (IGenerator<T> gen : generators) {
            if (!gen.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cloudbus.cloudsim.incubator.web.IGenerator#notifyOfTime(double)
     */
    @Override
    public void notifyOfTime(final double time) {
        for (IGenerator<T> gen : generators) {
            gen.notifyOfTime(time);
        }
    }

}
