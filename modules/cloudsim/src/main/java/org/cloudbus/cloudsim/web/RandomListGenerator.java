package org.cloudbus.cloudsim.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * Generates values randomly. An associative array of values and probabilities
 * is provided in the constructor.
 * 
 * @author nikolay.grozev
 * 
 * @param <T>
 */
public class RandomListGenerator<T> implements IGenerator<T> {

    private final Random rand = new Random();

    private final LinkedHashMap<T, Double> values = new LinkedHashMap<>();
    private final double maxValue;
    private T peeked;

    /**
     * Constr.
     * 
     * @param valuesAndFreqs
     *            - values mapped to probabilities. Must not be null and must
     *            not aggregate null values. The probabilities can be any
     *            positive double.
     */
    public RandomListGenerator(final Map<T, Double> valuesAndFreqs) {
        double sum = 0;
        for (Map.Entry<T, Double> e : valuesAndFreqs.entrySet()) {
            sum += e.getValue();
            values.put(e.getKey(), sum);
        }
        maxValue = sum;
    }

    /**
     * Constr.
     * 
     * @param valuesAndFreqs
     *            - values mapped to probabilities. Must not be null and must
     *            not aggregate null values. The probabilities can be any
     *            positive double.
     * @param seed
     *            - a seed.
     */
    public RandomListGenerator(final Map<T, Double> valuesAndFreqs, final long seed) {
        this(valuesAndFreqs);
        rand.setSeed(seed);
    }

    @Override
    public T peek() {
        if (peeked == null) {
            peeked = generateValue();
        }
        return peeked;
    }

    @Override
    public T poll() {
        T result = peeked;
        if (peeked != null) {
            peeked = generateValue();
        } else {
            result = generateValue();
        }
        return result;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void notifyOfTime(double time) {
        // Do nothing
    }

    private T generateValue() {
        double r = rand.nextDouble() * maxValue;
        T result = null;
        for (Map.Entry<T, Double> e : values.entrySet()) {
            if (r <= e.getValue()) {
                result = e.getKey();
                break;
            }
        }
        return result;
    }
}
