/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core;

import org.cloudbus.cloudsim.util.HistoryStat;
import org.cloudbus.cloudsim.util.MathUtil;

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
    HistoryStat getUtilizationHistory();

    default double[] getUtilizationHistoryList(){
        HistoryStat utilHistStat = getUtilizationHistory();
        double[] utilizationHistoryList = new double[utilHistStat.size()];
        int i = 0;
        for (double u : utilHistStat)
            utilizationHistoryList[i++] = u * getMips();

        return utilizationHistoryList;
    }

    /**
     * Adds a CPU utilization percentage history value.
     *
     * @param utilization the CPU utilization percentage to add
     */
    default void addUtilizationHistoryValue(final double utilization) {
        getUtilizationHistory().offer(utilization);
    }

    /**
     * Gets the utilization MAD in MIPS.
     *
     * @return the utilization MAD in MIPS
     */
    default double getUtilizationMad() {
        double mad = 0;
        HistoryStat hist = getUtilizationHistory();
        if (!hist.isEmpty()) {
            int n = hist.size();
            double median = hist.getMedian();
            double[] deviationSum = new double[n];
            int i = 0;
            for (double u : hist) {
                deviationSum[i++] = Math.abs(median - u);
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
        return getUtilizationHistory().getMean() * getMips();
    }

    /**
     * Gets the utilization variance in MIPS.
     *
     * @return the utilization variance in MIPS
     */
    default double getUtilizationVariance() {
        HistoryStat hist = getUtilizationHistory();
        double mean = hist.getMean();
        double variance = 0;
        if (!hist.isEmpty()) {
            int n = hist.size();
            for (double u : hist) {
                double tmp = u * getMips() - mean;
                variance += tmp * tmp;
            }
            variance /= n;
        }
        return variance;
    }
}
