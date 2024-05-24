package org.cloudbus.cloudsim.EX.vm;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.tuple.MutablePair;
import org.cloudbus.cloudsim.CloudletScheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A type of virtual machine, which keeps track of its performance. This VM
 * relies on an external entity (e.g. a broker) to notify it of its observed
 * utilisation. The VM keeps track of the notifications and then defines its
 * utilisation for a given time as the mean of the observations for the last
 * <strong>summaryLength</strong> seconds. The <strong>summaryLength</strong>
 * parameter is specified in the constructors.
 * 
 * @author nikolay.grozev
 * 
 */
public class MonitoredVmEX extends VmEX {

    private final double summaryPeriodLength;

    private final MonitoredData data = new MonitoredData();

    private double[] lastUtilMeasurement = new double[] { 0, 0, 0 };
    private boolean newPerfDataAvailableFlag = false;

    /**
     * Constr.
     * 
     * @param name
     *            - a short readable description of the VM - e.g. DB-server.
     * @param userId
     * @param mips
     * @param numberOfPes
     * @param ram
     * @param bw
     * @param size
     * @param vmm
     * @param cloudletScheduler
     * @param summaryPeriodLength
     *            - the historical period, used to determine the utilisation at
     *            runtime.
     */
    public MonitoredVmEX(final String name, final int userId, final double mips, final int numberOfPes, int ram,
                         final long bw, final long size, final String vmm, final CloudletScheduler cloudletScheduler,
                         final double summaryPeriodLength) {
        super(name, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
        this.summaryPeriodLength = summaryPeriodLength;
    }

    /**
     * Constr.
     * 
     * @param name
     *            - a short readable description of the VM - e.g. DB-server.
     * @param userId
     * @param mips
     * @param numberOfPes
     * @param ram
     * @param bw
     * @param size
     * @param vmm
     * @param cloudletScheduler
     * @param metadata
     * @param summaryPeriodLength
     *            - the historical period, used to determine the utilisation at
     *            runtime.
     */
    public MonitoredVmEX(final String name, final int userId, final double mips, final int numberOfPes, final int ram,
                         final long bw, final long size, final String vmm, final CloudletScheduler cloudletScheduler,
                         final VMMetadata metadata, final double summaryPeriodLength) {
        super(name, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler, metadata);
        this.summaryPeriodLength = summaryPeriodLength;
    }

    protected double getSummaryPeriodLength() {
        return summaryPeriodLength;
    }

    /**
     * Notifies this VM of its utilisation.
     * 
     * @param cpuUtil
     *            - the CPU utilisation. Must be in the range [0,1];
     * @param ramUtil
     *            - the RAM utilisation. Must be in the range [0,1];
     * @param diskUtil
     *            - the Disk utilisation. Must be in the range [0,1];
     */
    public void updatePerformance(final double cpuUtil, final double ramUtil, final double diskUtil) {

        if (summaryPeriodLength >= 0) {
            double currTime = getCurrentTime();

            this.newPerfDataAvailableFlag = newPerfDataAvailableFlag || this.lastUtilMeasurement[0] != cpuUtil
                    || this.lastUtilMeasurement[1] != ramUtil || this.lastUtilMeasurement[2] != diskUtil;

            data.put(currTime, cpuUtil, ramUtil, diskUtil);
            cleanupOldData(currTime);
        }
    }

    /**
     * Returns the current CPU utilisation as a number in the range [0,1].
     * 
     * @return the current CPU utilisation as a number in the range [0,1].
     */
    public double getCPUUtil() {
        return getAveragedPerformance(getCurrentTime())[0];
    }

    /**
     * Returns the current RAM utilisation as a number in the range [0,1].
     * 
     * @return the current RAM utilisation as a number in the range [0,1].
     */
    public double getRAMUtil() {
        return getAveragedPerformance(getCurrentTime())[1];
    }

    /**
     * Returns the current Disk utilisation as a number in the range [0,1].
     * 
     * @return the current Disk utilisation as a number in the range [0,1].
     */
    public double getDiskUtil() {
        return getAveragedPerformance(getCurrentTime())[2];
    }

    /**
     * Returns the current utilisation as a array of numbers in the range [0,1]
     * in the from [cp_util, ram_util, disk_util]. <strong>NOTE</strong> calling
     * methods should not modify the resulting array, as a shallow copy may be
     * returned!
     * 
     * @return the current utilisation as a array of numbers in the range [0,1]
     *         in the from [cp_util, ram_util, disk_util].
     */
    public double[] getAveragedUtil() {
        return getAveragedPerformance(getCurrentTime());
    }

    private double[] getAveragedPerformance(final double currTime) {
        // If there has not been any update - return the cached value
        if (!newPerfDataAvailableFlag) {
            return this.lastUtilMeasurement;
        } else {
            cleanupOldData(currTime);
            double[] result = computerAvgData();

            // Cache the value for further usage
            newPerfDataAvailableFlag = false;
            lastUtilMeasurement = result;

            return result;
        }
    }

    private double[] computerAvgData() {
        double[] result = new double[] { 0, 0, 0 };
        if (summaryPeriodLength >= 0) {
            result = data.computerAvgData();
        }
        return result;
    }

    private void cleanupOldData(final double currTime) {
        data.cleanUp(currTime, summaryPeriodLength);
    }

    @Override
    public MonitoredVmEX clone(final CloudletScheduler scheduler) {
        if (!getClass().equals(MonitoredVmEX.class)) {
            throw new IllegalStateException("The operation is undefined for subclass: " + getClass().getCanonicalName());
        }

        MonitoredVmEX result = new MonitoredVmEX(getName(), getUserId(), getMips(), getNumberOfPes(), getRam(),
                getBw(), getSize(), getVmm(), scheduler, getMetadata().clone(), getSummaryPeriodLength());
        return result;
    }

    /**
     * Returns the internal data structure, containing the monitored utilisation
     * data. Use only for testing purposes!
     * 
     * @return the internal data structure, containing the monitored utilisation
     *         data.
     */
    public MonitoredData getMonitoredData() {
        return data;
    }

    /**
     * Represents the monitored utilisation data. This class should be used
     * outside this VM only for testing purposes.
     * 
     * @author nikolay.grozev
     * 
     */
    public static class MonitoredData {

        private static final int MAX_POOL_SIZE = 500;

        /**
         * We don't want to create new arrays every time - this creates too much
         * garbage. That's why we need to keep a pool of unused array objects,
         * which we reuse.
         */
        private final List<double[]> arrayPool = new ArrayList<>();
        private final List<MutableDouble> doublePool = new ArrayList<>();

        private final ArrayList<MutablePair<MutableDouble, double[]>> data = new ArrayList<>();
        private int startIdx = 0;
        private int endIdx = 0;
        /** If there is not monitoring data. */
        private boolean empty = true;

        /**
         * Keeping the sums of all observations, to avoid excessive looping over
         * the observations.
         */
        private final double[] measurementsSums = new double[] { 0, 0, 0 };
        /**
         * The number/count of all observations. We keep it in a variable to
         * avoid looping.
         */
        private int measurementsCount = 0;

        public void put(double time, final double cpuUtil, final double ramUtil, final double diskUtil) {
            double[] util = getPooledArray(cpuUtil, ramUtil, diskUtil);
            MutableDouble pooledTime = getPooledDouble(time);
            if (empty) {
                data.add(startIdx, new MutablePair<>(pooledTime, util));
                endIdx = startIdx;

                // If startIdx is in the beginning of the list and endIdx is in
                // the end...
            } else if (data.isEmpty() || (startIdx == 0 && endIdx == data.size() - 1)) {
                data.add(new MutablePair<>(pooledTime, util));
                endIdx = data.size() - 1;

                // If endIdx is not at the end or just before startIdx
            } else if (endIdx < data.size() - 1 && (startIdx < endIdx || endIdx + 1 < startIdx)) {
                endIdx++;

                MutablePair<MutableDouble, double[]> entry = data.get(endIdx);
                entry.setLeft(pooledTime);
                entry.setRight(util);

                // If endIdx is at the end and startIdx is not in the beginning
            } else if (startIdx > 0 && endIdx == data.size() - 1) {
                endIdx = 0;

                MutablePair<MutableDouble, double[]> entry = data.get(endIdx);
                entry.setLeft(pooledTime);
                entry.setRight(util);

                // If endIdx is is just before startIdx
            } else if (endIdx + 1 == startIdx) {
                endIdx++;
                startIdx++;
                data.add(endIdx, new MutablePair<>(pooledTime, util));
            } else {
                // Something went wrong...
                throw new IllegalStateException("Ivalid state of counters: start=" + startIdx + " end=" + endIdx);
            }

            for (int i = 0; i < util.length; i++) {
                measurementsSums[i] += util[i];
            }
            this.measurementsCount++;
            empty = false;
        }

        public void cleanUp(double currTime, double summaryPeriodLength) {
            if (empty || summaryPeriodLength < 0) {
                return;
            }

            while (data.get(startIdx).getLeft().doubleValue() < currTime - summaryPeriodLength && startIdx != endIdx) {
                for (int i = 0; i < data.get(startIdx).getRight().length; i++) {
                    measurementsSums[i] -= data.get(startIdx).getRight()[i];
                }
                measurementsCount--;

                // Pool the objects, so we can reuse them.
                arrayPool.add(data.get(startIdx).getRight());
                doublePool.add(data.get(startIdx).getLeft());

                startIdx = (startIdx + 1) % data.size();
            }

            if (startIdx == endIdx && data.get(startIdx).getLeft().doubleValue() < currTime - summaryPeriodLength) {
                Arrays.fill(measurementsSums, 0);
                measurementsCount = 0;

                // Pool the objects, so we can reuse them.
                arrayPool.add(data.get(startIdx).getRight());
                doublePool.add(data.get(startIdx).getLeft());

                empty = true;
                startIdx = 0;
                endIdx = 0;
            }
        }

        public double[] computerAvgData() {
            double[] result = new double[] { 0, 0, 0 };
            if (measurementsCount > 0) {
                for (int i = 0; i < result.length; i++) {
                    result[i] = measurementsSums[i] / measurementsCount;
                }
            }
            return result;
        }

        /**
         * Return the number of utilisation records.
         * 
         * @return the number of utilisation records.
         */
        public int size() {
            if (endIdx == startIdx) {
                return empty ? 0 : 1;
            } else if (endIdx > startIdx) {
                return endIdx - startIdx + 1;
            } else {
                return data.size() - (startIdx - endIdx) + 1;
            }
        }

        private double[] getPooledArray(final double cpuUtil, final double ramUtil, final double diskUtil) {
            if (arrayPool.isEmpty()) {
                return new double[] { cpuUtil, ramUtil, diskUtil };
            } else {
                double[] res = arrayPool.remove(arrayPool.size() - 1);
                res[0] = cpuUtil;
                res[1] = ramUtil;
                res[2] = diskUtil;

                if (arrayPool.size() > MAX_POOL_SIZE) {
                    arrayPool.clear();
                }

                return res;
            }
        }

        private MutableDouble getPooledDouble(final double dbl) {
            if (doublePool.isEmpty()) {
                return new MutableDouble(dbl);
            } else {
                MutableDouble res = doublePool.remove(doublePool.size() - 1);
                res.setValue(dbl);

                if (doublePool.size() > MAX_POOL_SIZE) {
                    doublePool.clear();
                }

                return res;
            }
        }

        /**
         * Returns the size of the used undrlying data structure. Used for
         * testing purposes.
         * 
         * @return the size of the used undrlying data structure.
         */
        public int dataSize() {
            return data.size();
        }
    }

}
