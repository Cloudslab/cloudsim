package org.cloudbus.cloudsim.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.VmStateHistoryEntry;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.core.PowerGuestEntity;
import org.junit.Test;

public class HistoryStatTest {
    private void addUtilizationHistoryValue(List<Double> history, final double utilization) {
        history.addFirst(utilization);
        if (history.size() > PowerGuestEntity.HISTORY_LENGTH) {
            history.remove(PowerGuestEntity.HISTORY_LENGTH);
        }
    }

    @Test
    public void testSimple() {
        HistoryStat s = new HistoryStat(PowerGuestEntity.HISTORY_LENGTH);
        assertEquals(0.0, s.getMedian(), 0);
        s.offer(10.0);
        assertEquals(10.0, s.getMedian(), 0);
        s.offer(20.0);
        assertEquals(15.0, s.getMedian(), 0);
        s.offer(10.0);
        assertEquals(10.0, s.getMedian(), 0);
    }
    @Test
    public void testCompared() {
        HistoryStat s = new HistoryStat(PowerGuestEntity.HISTORY_LENGTH);
        PowerGuestEntity e = new PowerGuestEntity() {
            @Override
            public HistoryStat getUtilizationHistory() { return null; }
            @Override
            public double updateCloudletsProcessing(double currentTime, List<Double> mipsShare) {
                throw new UnsupportedOperationException("Unimplemented method 'updateCloudletsProcessing'");
            }
            @Override
            public List<Double> getCurrentRequestedMips() {
                throw new UnsupportedOperationException("Unimplemented method 'getCurrentRequestedMips'");
            }
            @Override
            public long getCurrentRequestedBw() {
                throw new UnsupportedOperationException("Unimplemented method 'getCurrentRequestedBw'");
            }
            @Override
            public int getCurrentRequestedRam() {
                throw new UnsupportedOperationException("Unimplemented method 'getCurrentRequestedRam'");
            }
            @Override
            public double getTotalUtilizationOfCpu(double time) {
                throw new UnsupportedOperationException("Unimplemented method 'getTotalUtilizationOfCpu'");
            }
            @Override
            public <T extends HostEntity> T getHost() {
                throw new UnsupportedOperationException("Unimplemented method 'getHost'");
            }
            @Override
            public long getBw() {
                throw new UnsupportedOperationException("Unimplemented method 'getBw'");
            }
            @Override
            public int getRam() {
                throw new UnsupportedOperationException("Unimplemented method 'getRam'");
            }
            @Override
            public double getMips() {
                throw new UnsupportedOperationException("Unimplemented method 'getMips'");
            }
            @Override
            public double getTotalUtilizationOfCpuMips(double time) {
                throw new UnsupportedOperationException("Unimplemented method 'getTotalUtilizationOfCpuMips'");
            }
            @Override
            public long getSize() {
                throw new UnsupportedOperationException("Unimplemented method 'getSize'");
            }
            @Override
            public int getId() {
                throw new UnsupportedOperationException("Unimplemented method 'getId'");
            }
            @Override
            public int getUserId() {
                throw new UnsupportedOperationException("Unimplemented method 'getUserId'");
            }
            @Override
            public String getUid() {
                throw new UnsupportedOperationException("Unimplemented method 'getUid'");
            }
            @Override
            public List<VmStateHistoryEntry> getStateHistory() {
                throw new UnsupportedOperationException("Unimplemented method 'getStateHistory'");
            }
            @Override
            public CloudletScheduler getCloudletScheduler() {
                throw new UnsupportedOperationException("Unimplemented method 'getCloudletScheduler'");
            }
            @Override
            public <T extends HostEntity> void setHost(T host) {
                throw new UnsupportedOperationException("Unimplemented method 'setHost'");
            }
            @Override
            public void setCurrentAllocatedRam(int currentAllocatedRam) {
                throw new UnsupportedOperationException("Unimplemented method 'setCurrentAllocatedRam'");
            }
            @Override
            public void setCurrentAllocatedBw(long currentAllocatedBw) {
                throw new UnsupportedOperationException("Unimplemented method 'setCurrentAllocatedBw'");
            }
            @Override
            public void setCurrentAllocatedMips(List<Double> mips) {
                throw new UnsupportedOperationException("Unimplemented method 'setCurrentAllocatedMips'");
            }
            @Override
            public void setInMigration(boolean inMigration) {
                throw new UnsupportedOperationException("Unimplemented method 'setInMigration'");
            }
            @Override
            public void setBeingInstantiated(boolean beingInstantiated) {
                throw new UnsupportedOperationException("Unimplemented method 'setBeingInstantiated'");
            }
            @Override
            public boolean isInMigration() {
                throw new UnsupportedOperationException("Unimplemented method 'isInMigration'");
            }
            @Override
            public int getNumberOfPes() {
                throw new UnsupportedOperationException("Unimplemented method 'getNumberOfPes'");
            }
            @Override
            public double getTotalMips() {
                throw new UnsupportedOperationException("Unimplemented method 'getTotalMips'");
            }
            @Override
            public boolean isBeingInstantiated() {
                throw new UnsupportedOperationException("Unimplemented method 'isBeingInstantiated'");
            };
            
        };

        Random gen = new Random();
        List<Double> history = new ArrayList<>(PowerGuestEntity.HISTORY_LENGTH);

        for (int i = 0; i < 1000; i++) {
            double val = gen.nextDouble();
            s.offer(val);
            addUtilizationHistoryValue(history, val);
            assertEquals(s.getMedian(), MathUtil.median(history), 1e-6);
            assertEquals(s.getMean(), MathUtil.mean(history), 1e-6);
        }

        for (int step = 0; step < 100; step++) {
            final int N = 1000;
            double samples[] = new double[N];
            for (int i = 0; i < N; i++)
                samples[i] = gen.nextDouble();

            double medianSum = 0.0;
            double meanSum = 0.0;
            long t0 = System.nanoTime();
            for (int i = 0; i < N; i++) {
                s.offer(samples[i]);
                medianSum += s.getMedian();
                meanSum += s.getMean();
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
