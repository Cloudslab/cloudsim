package org.cloudbus.cloudsim.core;

import org.cloudbus.cloudsim.util.MathUtil;

import java.util.List;

/**
 * Represents a Virtual Machine (VM), or a container, that stores its CPU utilization percentage history for power
 * consumption purposes.
 *
 * @author Remo Andreoli
 * @since CloudSim Toolkit 7.0
 */
public interface PowerGuestEntity extends GuestEntity {
    int HISTORY_LENGTH = 30; // static and final by default

    /**
     * Gets the CPU utilization percentage history.
     *
     * @return the CPU utilization percentage history
     */
    List<Double> getUtilizationHistory();

    default double[] getUtilizationHistoryList(){
        double[] utilizationHistoryList = new double[PowerGuestEntity.HISTORY_LENGTH];

        // if any thing happens check if you need to have mips and the trim
        for (int i = 0; i < getUtilizationHistory().size(); i++) {
            utilizationHistoryList[i] += getUtilizationHistory().get(i) * getMips();
        }

        return MathUtil.trimZeroTail(utilizationHistoryList);
    }

    /**
     * Adds a CPU utilization percentage history value.
     *
     * @param utilization the CPU utilization percentage to add
     */
    default void addUtilizationHistoryValue(final double utilization) {
        getUtilizationHistory().add(0, utilization);
        if (getUtilizationHistory().size() > HISTORY_LENGTH) {
            getUtilizationHistory().remove(HISTORY_LENGTH);
        }
    }

    /**
     * Gets the utilization MAD in MIPS.
     *
     * @return the utilization MAD in MIPS
     */
    default double getUtilizationMad() {
        double mad = 0;
        if (!getUtilizationHistory().isEmpty()) {
            int n = Math.min(HISTORY_LENGTH, getUtilizationHistory().size());
            double median = MathUtil.median(getUtilizationHistory());
            double[] deviationSum = new double[n];
            for (int i = 0; i < n; i++) {
                deviationSum[i] = Math.abs(median - getUtilizationHistory().get(i));
            }
            mad = MathUtil.median(deviationSum);
        }
        return mad;
    }

    /**
     * Gets the utilization mean in percents.
     *
     * @return the utilization mean in MIPS
     */
    default double getUtilizationMean() {
        double mean = 0;
        if (!getUtilizationHistory().isEmpty()) {
            int n = Math.min(HISTORY_LENGTH, getUtilizationHistory().size());
            for (int i = 0; i < n; i++) {
                mean += getUtilizationHistory().get(i);
            }
            mean /= n;
        }
        return mean * getMips();
    }

    /**
     * Gets the utilization variance in MIPS.
     *
     * @return the utilization variance in MIPS
     */
    default double getUtilizationVariance() {
        double mean = getUtilizationMean();
        double variance = 0;
        if (!getUtilizationHistory().isEmpty()) {
            int n = Math.min(HISTORY_LENGTH, getUtilizationHistory().size());
            for (int i = 0; i < n; i++) {
                double tmp = getUtilizationHistory().get(i) * getMips() - mean;
                variance += tmp * tmp;
            }
            variance /= n;
        }
        return variance;
    }
}
