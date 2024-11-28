package org.cloudbus.cloudsim.util;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.cloudbus.cloudsim.core.PowerGuestEntity.HISTORY_LENGTH;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HistoryStatTest {
    HistoryStat stat;
    List<Double> history;
    private void addUtilizationHistoryValue(List<Double> history, final double utilization) {
        history.addFirst(utilization);
        if (history.size() > HISTORY_LENGTH) {
            history.remove(HISTORY_LENGTH);
        }
    }

    @BeforeEach
    public void setUp() {
        stat = new HistoryStat(HISTORY_LENGTH);
        history = new ArrayList<>(HISTORY_LENGTH);
    }

    @Test
    public void testSimple() {
        assertEquals(0.0, stat.getMedian(), 0);
        stat.offer(10.0);
        assertEquals(10.0, stat.getMedian(), 0);
        stat.offer(20.0);
        assertEquals(15.0, stat.getMedian(), 0);
        stat.offer(10.0);
        assertEquals(10.0, stat.getMedian(), 0);
    }

    @Test
    public void testExpunge() {
        for (double val = 0.0; val < 2 * HISTORY_LENGTH; val += 1.0) {
            stat.offer(val);
            addUtilizationHistoryValue(history, val);
            assertEquals(stat.getMedian(), MathUtil.median(history), 1e-6);
            if (stat.size() <= HISTORY_LENGTH-1)
                assertEquals(val/2.0, stat.getMedian(), 1e-6);
            else
                assertEquals((val+val-(HISTORY_LENGTH-1))/2.0, stat.getMedian(), 1e-6);
        }
    }

    @Test
    public void testCompared() {
        Random gen = new Random();
        for (int i = 0; i < 1000; i++) {
            double val = gen.nextDouble();
            stat.offer(val);
            addUtilizationHistoryValue(history, val);
            assertEquals(stat.getMedian(), MathUtil.median(history), 1e-6);
            assertEquals(stat.getMean(), MathUtil.mean(history), 1e-6);
        }

        for (int step = 0; step < 100; step++) {
            final int N = 1000;
            double[] samples = new double[N];
            for (int i = 0; i < N; i++)
                samples[i] = gen.nextDouble();

            double medianSum = 0.0;
            double meanSum = 0.0;
            long t0 = System.nanoTime();
            for (int i = 0; i < N; i++) {
                stat.offer(samples[i]);
                medianSum += stat.getMedian();
                meanSum += stat.getMean();
            }

            double medianSum2 = 0.0;
            double meanSum2 = 0.0;
            long t1 = System.nanoTime();
            for (int i = 0; i < N; i++) {
                addUtilizationHistoryValue(history, samples[i]);
                medianSum2 += MathUtil.median(history);
                meanSum2 += MathUtil.mean(history);
            }
            long t2 = System.nanoTime();

            assertEquals(medianSum, medianSum2, 1e-6);
            assertEquals(meanSum, meanSum2, 1e-6);
            System.out.println("HistoryStat: " + (t1 - t0) + ", utilizationHistory: " + (t2 - t1));
        }
    }
}
