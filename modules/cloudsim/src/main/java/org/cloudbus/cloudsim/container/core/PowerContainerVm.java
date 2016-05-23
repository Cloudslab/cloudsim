package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.container.containerProvisioners.ContainerBwProvisioner;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPe;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerRamProvisioner;
import org.cloudbus.cloudsim.container.schedulers.ContainerScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.MathUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by sareh on 14/07/15.
 */
public class PowerContainerVm extends ContainerVm {

    /**
     * The Constant HISTORY_LENGTH.
     */
    public static final int HISTORY_LENGTH = 30;

    /**
     * The utilization history.
     */
    private final List<Double> utilizationHistory = new LinkedList<Double>();

    /**
     * The previous time.
     */
    private double previousTime;

    /**
     * The scheduling interval.
     */
    private double schedulingInterval;

    /**
     * Instantiates a new power vm.
     *
     * @param id                 the id
     * @param userId             the user id
     * @param mips               the mips
     * @param ram                the ram
     * @param bw                 the bw
     * @param size               the size
     * @param vmm                the vmm
     * @param containerScheduler the cloudlet scheduler
     * @param schedulingInterval the scheduling interval
     */
    public PowerContainerVm(
            final int id,
            final int userId,
            final double mips,
            final float ram,
            final long bw,
            final long size,
            final String vmm,
            final ContainerScheduler containerScheduler,
            final ContainerRamProvisioner containerRamProvisioner,
            final ContainerBwProvisioner containerBwProvisioner,
            List<? extends ContainerPe> peList,
            final double schedulingInterval) {
        super(id, userId, mips, ram, bw, size, vmm, containerScheduler, containerRamProvisioner, containerBwProvisioner, peList);
        setSchedulingInterval(schedulingInterval);
    }

    /**
     * Updates the processing of cloudlets running on this VM.
     *
     * @param currentTime current simulation time
     * @param mipsShare   array with MIPS share of each Pe available to the scheduler
     * @return time predicted completion time of the earliest finishing cloudlet, or 0 if there is
     * no next events
     * @pre currentTime >= 0
     * @post $none
     */




    @Override
    public double updateVmProcessing(final double currentTime, final List<Double> mipsShare) {
        double time = super.updateVmProcessing(currentTime, mipsShare);
        if (currentTime > getPreviousTime() && (currentTime - 0.2) % getSchedulingInterval() == 0) {
            double utilization = 0;

            for (Container container : getContainerList()) {
                // The containers which are going to migrate to the vm shouldn't be added to the utilization
                if(!getContainersMigratingIn().contains(container)) {
                    time = container.getContainerCloudletScheduler().getPreviousTime();
                    utilization += container.getTotalUtilizationOfCpu(time);
                }
                }

            if (CloudSim.clock() != 0 || utilization != 0) {
                addUtilizationHistoryValue(utilization);
            }
            setPreviousTime(currentTime);
        }
        return time;
    }

    /**
     * Gets the utilization MAD in MIPS.
     *
     * @return the utilization mean in MIPS
     */
    public double getUtilizationMad() {
        double mad = 0;
        if (!getUtilizationHistory().isEmpty()) {
            int n = HISTORY_LENGTH;
            if (HISTORY_LENGTH > getUtilizationHistory().size()) {
                n = getUtilizationHistory().size();
            }
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
    public double getUtilizationMean() {
        double mean = 0;
        if (!getUtilizationHistory().isEmpty()) {
            int n = HISTORY_LENGTH;
            if (HISTORY_LENGTH > getUtilizationHistory().size()) {
                n = getUtilizationHistory().size();
            }
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
    public double getUtilizationVariance() {
        double mean = getUtilizationMean();
        double variance = 0;
        if (!getUtilizationHistory().isEmpty()) {
            int n = HISTORY_LENGTH;
            if (HISTORY_LENGTH > getUtilizationHistory().size()) {
                n = getUtilizationHistory().size();
            }
            for (int i = 0; i < n; i++) {
                double tmp = getUtilizationHistory().get(i) * getMips() - mean;
                variance += tmp * tmp;
            }
            variance /= n;
        }
        return variance;
    }

    /**
     * Adds the utilization history value.
     *
     * @param utilization the utilization
     */
    public void addUtilizationHistoryValue(final double utilization) {
        getUtilizationHistory().add(0, utilization);
        if (getUtilizationHistory().size() > HISTORY_LENGTH) {
            getUtilizationHistory().remove(HISTORY_LENGTH);
        }
    }

    /**
     * Gets the utilization history.
     *
     * @return the utilization history
     */
    public List<Double> getUtilizationHistory() {
        return utilizationHistory;
    }

    /**
     * Gets the previous time.
     *
     * @return the previous time
     */
    public double getPreviousTime() {
        return previousTime;
    }

    /**
     * Sets the previous time.
     *
     * @param previousTime the new previous time
     */
    public void setPreviousTime(final double previousTime) {
        this.previousTime = previousTime;
    }

    /**
     * Gets the scheduling interval.
     *
     * @return the schedulingInterval
     */
    public double getSchedulingInterval() {
        return schedulingInterval;
    }

    /**
     * Sets the scheduling interval.
     *
     * @param schedulingInterval the schedulingInterval to set
     */
    protected void setSchedulingInterval(final double schedulingInterval) {
        this.schedulingInterval = schedulingInterval;
    }

    public double[] getUtilizationHistoryList(){
        double[] utilizationHistoryList = new double[PowerContainerVm.HISTORY_LENGTH];
//            if any thing happens check if you need to have mips and the trim
        for (int i = 0; i < getUtilizationHistory().size(); i++) {
            utilizationHistoryList[i] += getUtilizationHistory().get(i) * getMips();
        }

        return MathUtil.trimZeroTail(utilizationHistoryList);
    }
}


