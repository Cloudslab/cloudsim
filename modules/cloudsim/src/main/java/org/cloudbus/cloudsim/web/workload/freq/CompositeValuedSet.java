package org.cloudbus.cloudsim.web.workload.freq;

import org.uncommons.maths.random.SeedGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * A set consisting of smaller finite continuous intervals, associated with
 * normal distributions.
 * 
 * @author nikolay.grozev
 * 
 */
public class CompositeValuedSet {

    private final List<FiniteValuedInterval> subIntervals;

    /**
     * Constr.
     * 
     * @param subintervals
     *            - the intervals that make up the set.
     */
    public CompositeValuedSet(List<FiniteValuedInterval> subintervals) {
        this.subIntervals = subintervals;
    }

    /**
     * Returns the value for the x element from its subsets.
     * 
     * @param x
     *            - the value.
     * @return the value for the x element from its subsets.
     */
    public double getValue(double x) {
        for (FiniteValuedInterval i : subIntervals) {
            if (i.contains(x)) {
                return i.getValue();
            }
        }
        throw new IllegalArgumentException("X=" + x + " is not contained in " + this);
    }

    /**
     * Returns if x is contained withing any of the subsets/subintervals.
     * 
     * @param x
     *            - the x to search for.
     * @return if x is contained withing any of the subsets/subintervals.
     */
    public boolean contains(double x) {
        for (FiniteValuedInterval i : subIntervals) {
            if (i.contains(x)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new instance based on the specified textual representations.
     * 
     * @param intervals
     *            - the intervals' texts.
     * @return a new instance based on the specified textual representations.
     */
    public static CompositeValuedSet createCompositeValuedSet(String... intervals) {
        return createCompositeValuedSet((byte[]) null, intervals);
    }

    /**
     * Creates a new instance based on the specified textual representations.
     * 
     * @param seed
     *            - the seed to use.
     * @param intervals
     *            - the intervals' texts.
     * @return a new instance based on the specified textual representations.
     */
    public static CompositeValuedSet createCompositeValuedSet(byte[] seed, String... intervals) {
        List<FiniteValuedInterval> subIntervals = new ArrayList<>();
        for (String i : intervals) {
            subIntervals.add(FiniteValuedInterval.createInterval(i, seed));
        }
        return new CompositeValuedSet(subIntervals);
    }

    /**
     * Creates a new instance based on the specified textual representations.
     * 
     * @param seedGen
     *            - the seed generator to use.
     * @param intervals
     *            - the intervals' texts.
     * @return a new instance based on the specified textual representations.
     */
    public static CompositeValuedSet createCompositeValuedSet(SeedGenerator seedGen, String... intervals) {
        List<FiniteValuedInterval> subIntervals = new ArrayList<>();
        for (String i : intervals) {
            subIntervals.add(FiniteValuedInterval.createInterval(i, seedGen));
        }
        return new CompositeValuedSet(subIntervals);
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        for (FiniteValuedInterval interval : subIntervals) {
            buff.append(interval.toString());
        }
        return buff.toString();
    }
}
