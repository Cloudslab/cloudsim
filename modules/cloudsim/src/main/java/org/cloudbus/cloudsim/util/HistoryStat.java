package org.cloudbus.cloudsim.util;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.PriorityQueue;

public class HistoryStat extends ArrayDeque<Double> {
    private final int max_size;
    private final PriorityQueue<Double> lower;
    private final PriorityQueue<Double> higher;
    private double sum = 0.0;

    public HistoryStat(int max_size) {
        super(max_size);
        assert(max_size >= 2);
        this.max_size = max_size;
        lower = new PriorityQueue<>((max_size + 1) / 2, Comparator.reverseOrder());
        higher  = new PriorityQueue<>((max_size + 1) / 2, Comparator.naturalOrder());
    }

    @Override
    public boolean offer(Double val) {
        if (size() == max_size) {
            double oldest = poll();
            sum -= oldest;
            if (oldest <= lower.peek())
                lower.remove(oldest);
            else
                higher.remove(oldest);
        }
        boolean rv = super.offer(val);
        sum += val;
        double low = Double.MAX_VALUE;
        if (!lower.isEmpty())
            low = lower.peek();
        if (val <= low) {
            lower.add(val);
            if (lower.size() >= higher.size() + 2)
                higher.add(lower.poll());
        } else {
            higher.add(val);
            if (higher.size() >= lower.size() + 2)
                lower.add(higher.poll());
        }
        return rv;
    }

    public double getMedian() {
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
