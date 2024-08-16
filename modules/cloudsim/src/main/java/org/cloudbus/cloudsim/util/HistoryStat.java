package org.cloudbus.cloudsim.util;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Variant of ArrayDeque that provides fast computation of the mean and median of a moving window of last offer()ed samples.
 * The median uses two heaps that are not used until the first invocation of the getMedian() method.
 * 
 * @TODO The implementation needs to be completed with all methods from the ArrayDeque interface.
 */
public class HistoryStat extends ArrayDeque<Double> {
    private final int max_size;
    private double sum = 0.0;
    private PriorityQueue<Double> lower;
    private PriorityQueue<Double> higher;

    public HistoryStat(int max_size) {
        super(max_size);
        assert(max_size >= 2);
        this.max_size = max_size;
    }

    private void enableFastMedian() {
        lower = new PriorityQueue<>((max_size + 1) / 2, Comparator.reverseOrder());
        higher  = new PriorityQueue<>((max_size + 1) / 2, Comparator.naturalOrder());
        for (double val : this)
            addHeaps(val);
    }

    private void addHeaps(double val) {
        double low = Double.MAX_VALUE;
        if (!lower.isEmpty())
            low = lower.peek();
        if (val <= low) {
            lower.add(val);
            if (lower.size() > higher.size() + 1)
                higher.add(lower.poll());
        } else {
            higher.add(val);
            if (higher.size() > lower.size() + 1)
                lower.add(higher.poll());
        }
    }

    private void delHeaps(double val) {
        if (!lower.isEmpty() && val <= lower.peek())
            lower.remove(val);
        else
            higher.remove(val);
    }

    @Override
    public boolean offer(Double val) {
        if (size() == max_size) {
            double oldest = poll();
            sum -= oldest;
            if (lower != null)
                delHeaps(oldest);
        }
        boolean rv = super.offer(val);
        sum += val;
        if (lower != null)
            addHeaps(val);
        return rv;
    }

    public double getMedian() {
        if (lower == null) {
            enableFastMedian();
        }
        if (lower.isEmpty() && higher.isEmpty())
            return 0.0;
        if (lower.isEmpty() || lower.size() < higher.size())
            return higher.peek();
        if (higher.isEmpty() || higher.size() < lower.size())
            return lower.peek();
        if (lower.size() == higher.size())
            return (lower.peek() + higher.peek()) / 2.0;
        assert(false);
        return 0.0;
    }

    public double getMean() {
        return sum / size();
    }
}
