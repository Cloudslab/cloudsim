package org.cloudbus.cloudsim.web.workload.freq;

import org.uncommons.maths.random.GaussianGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.SeedException;
import org.uncommons.maths.random.SeedGenerator;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A finite interval of values with an associated normal distribution or a
 * constant.
 * 
 * @author nikolay.grozev
 * 
 */
public class FiniteValuedInterval {

    // (\(|\[)(\d+\.?\d*),(\d+\.?\d*)(\)|\])m=(\d+\.?\d*)std=(\d+\.?\d*)
    private static final Pattern INTERVAL_PATTERN = Pattern
            .compile("(\\(|\\[)(\\d+\\.?\\d*),(\\d+\\.?\\d*)(\\)|\\])m=(\\d+\\.?\\d*)std=(\\d+\\.?\\d*)");

    private final double start;
    private final boolean startIncluded;
    private final double end;
    private final boolean endIncluded;

    private double value;
    private GaussianGenerator generator;

    /**
     * Constr.
     * 
     * @param start
     *            - the start of the interval.
     * @param startIncluded
     *            - whether the start is included in the interval.
     * @param end
     *            - the end of the interval. Must be greater than the start.
     * @param endIncluded
     *            - if the end is included in the interval.
     * @param mean
     *            - the mean of the generated values.
     * @param stDev
     *            - the standard deviation. If 0, then only constants with value
     *            mean are generated.
     * @param seed
     *            - the seed. If null no seed is used.
     */
    public FiniteValuedInterval(double start, boolean startIncluded, double end, boolean endIncluded, double mean,
            double stDev, byte[] seed) {
        this(start, startIncluded, end, endIncluded, mean, stDev, seed, null);
    }

    /**
     * Constr.
     * 
     * @param start
     *            - the start of the interval.
     * @param startIncluded
     *            - whether the start is included in the interval.
     * @param end
     *            - the end of the interval. Must be greater than the start.
     * @param endIncluded
     *            - if the end is included in the interval.
     * @param mean
     *            - the mean of the generated values.
     * @param stDev
     *            - the standard deviation. If 0, then only constants with value
     *            mean are generated.
     * @param seedGen
     *            - the seed generator. If null or erronous a default seed gen
     *            is used.
     */
    public FiniteValuedInterval(double start, boolean startIncluded, double end, boolean endIncluded, double mean,
            double stDev, SeedGenerator seedGen) {
        this(start, startIncluded, end, endIncluded, mean, stDev, null, seedGen);
    }

    /**
     * Constr.
     * 
     * @param start
     *            - the start of the interval.
     * @param startIncluded
     *            - whether the start is included in the interval.
     * @param end
     *            - the end of the interval. Must be greater than the start.
     * @param endIncluded
     *            - if the end is included in the interval.
     * @param mean
     *            - the mean of the generated values.
     * @param stDev
     *            - the standard deviation. If 0, then only constants with value
     *            mean are generated.
     * @param seed
     *            - the seed. If null no seed is used.
     */
    public FiniteValuedInterval(double start, boolean startIncluded, double end, boolean endIncluded, double mean,
            double stDev) {
        this(start, startIncluded, end, endIncluded, mean, stDev, (byte[]) null);
    }

    private FiniteValuedInterval(double start, boolean startIncluded, double end, boolean endIncluded, double mean,
            double stDev, byte[] seed, SeedGenerator seedGen) {
        super();
        if (start > end) {
            throw new IllegalArgumentException("The start of an interval should be smaller than the end.");
        }

        this.start = start;
        this.startIncluded = startIncluded;
        this.end = end;
        this.endIncluded = endIncluded;

        if (stDev > 0) {
            Random rng = null;
            if (seed == null) {
                try {
                    rng = seedGen == null ? new MersenneTwisterRNG() : new MersenneTwisterRNG(seedGen);
                } catch (SeedException e) {
                    rng = new MersenneTwisterRNG();
                }
            } else {
                rng = new MersenneTwisterRNG(seed);
            }

            generator = new GaussianGenerator(mean, stDev, rng);
        } else {
            value = mean;
        }
    }

    /**
     * Returns the value.
     * 
     * @return the value
     */
    public double getValue() {
        if (generator != null) {
            return generator.nextValue();
        } else {
            return value;
        }
    }

    /**
     * Returns if x is contained in the interval.
     * 
     * @param x
     *            - the value to check for.
     * @return if x is contained in the interval.
     */
    public boolean contains(double x) {
        boolean aboveStart = x > start || (x == start && startIncluded);
        boolean belowEnd = x < end || (x == end && endIncluded);
        return aboveStart && belowEnd;
    }

    /**
     * Creates a new interval from it's textual representation.
     * 
     * @param interval
     *            - the txt of the interval.
     * @return a new interval from it's textual representation.
     */
    public static FiniteValuedInterval createInterval(String interval) {
        return createInterval(interval, null, null);
    }

    /**
     * Creates a new interval from it's textual representation.
     * 
     * @param interval
     *            - the txt of the interval.
     * @param seed
     *            - the seed of the new interval
     * @return a new interval from it's textual representation.
     */
    public static FiniteValuedInterval createInterval(String interval, byte[] seed) {
        return createInterval(interval, seed, null);
    }

    /**
     * Creates a new interval from it's textual representation.
     * 
     * @param interval
     *            - the txt of the interval.
     * @param seedGen
     *            - the seed generator for the new interval.
     * @return a new interval from it's textual representation.
     */
    public static FiniteValuedInterval createInterval(String interval, SeedGenerator seedGen) {
        return createInterval(interval, null, seedGen);
    }

    private static FiniteValuedInterval createInterval(String interval, byte[] seed, SeedGenerator seedGen) {
        String cleanedInterval = interval.replaceAll("\\s*", "");
        Matcher matcher = INTERVAL_PATTERN.matcher(cleanedInterval);
        matcher.matches();

        boolean includeStart = "[".equals(matcher.group(1));
        double start = Double.parseDouble(matcher.group(2));
        double end = Double.parseDouble(matcher.group(3));
        boolean includeEnd = "]".equals(matcher.group(4));

        double mean = Double.parseDouble(matcher.group(5));
        double stDev = Double.parseDouble(matcher.group(6));

        return new FiniteValuedInterval(start, includeStart, end, includeEnd, mean, stDev, seed, seedGen);
    }

    @Override
    public String toString() {
        return String.format("%s%.2f,%.2f%s", startIncluded ? "[" : "(", start, end, endIncluded ? "]" : ")");
    }

}
