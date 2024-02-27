package org.cloudbus.cloudsim.container.core;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.container.schedulers.ContainerCloudletScheduler;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.util.MathUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sareh on 9/07/15.
 */
public class Container {

    /**
     * The id.
     */
    @Setter(AccessLevel.PROTECTED) @Getter private int id;

    /**
     * The user id.
     */
    @Setter(AccessLevel.PROTECTED)
    @Getter
    private int userId;

    /**
     * The uid.
     */
    @Getter @Setter private String uid;

    /**
     * The size.
     */
    @Getter
    @Setter
    private long size;

    /**
     * The MIPS.
     */
    @Setter(AccessLevel.PROTECTED)
    @Getter
    private double mips;

    /**
     * The workloadMips.
     */
    @Setter
    @Getter
    private double workloadMips;

    /**
     * The number of PEs.
     */
    @Getter @Setter private int numberOfPes;

    /**
     * The ram.
     */
    @Getter @Setter private float ram;

    /**
     * The bw.
     */
    @Getter
    @Setter
    private long bw;

    /**
     * The containerManager.
     */
    @Setter(AccessLevel.PROTECTED)
    @Getter
    private String containerManager;

    /**
     * The ContainerCloudlet scheduler.
     */
    @Setter(AccessLevel.PROTECTED)
    @Getter
    private ContainerCloudletScheduler containerCloudletScheduler;

    /**
     * The ContainerVm.
     */
    @Getter
    @Setter
    private ContainerVm vm;

    /**
     * In migration flag.
     */
    @Setter
    @Getter
    private boolean inMigration;

    /**
     * The current allocated size.
     */
    @Setter(AccessLevel.PROTECTED)
    @Getter
    private long currentAllocatedSize;

    /**
     * The current allocated ram.
     */
    @Setter
    @Getter
    private float currentAllocatedRam;

    /**
     * The current allocated bw.
     */
    @Setter
    @Getter
    private long currentAllocatedBw;

    /**
     * The current allocated mips.
     */
    @Setter
    private List<Double> currentAllocatedMips;

    /**
     * The VM is being instantiated.
     */
    @Getter
    private boolean beingInstantiated;

    /**
     * The mips allocation history.
     */
    @Getter
    private final List<VmStateHistoryEntry> stateHistory = new LinkedList<>();

//    added from the power Vm
    /**
     * The Constant HISTORY_LENGTH.
     */
    public static final int HISTORY_LENGTH = 30;

    /**
     * The utilization history.
     */
    @Getter(AccessLevel.PROTECTED)
    private final List<Double> utilizationHistory = new LinkedList<>();

    /**
     * The previous time.
     */
    @Setter
    @Getter
    private double previousTime;

    /**
     * The scheduling interval.
     */
    @Setter(AccessLevel.PROTECTED)
    @Getter
    private double schedulingInterval;


    /**
     * Creates a new Container object.
     * @param id
     * @param userId
     * @param mips
     * @param numberOfPes
     * @param ram
     * @param bw
     * @param size
     * @param containerManager
     * @param containerCloudletScheduler
     * @param schedulingInterval
     */
    public Container(
            int id,
            int userId,
            double mips,
            int numberOfPes,
            int ram,
            long bw,
            long size,
            String containerManager,
            ContainerCloudletScheduler containerCloudletScheduler, double schedulingInterval) {
        setWorkloadMips(mips);
        setId(id);
        setUserId(userId);
        setUid(getUid(userId, id));
        setMips(mips);
        setNumberOfPes(numberOfPes);
        setRam(ram);
        setBw(bw);
        setSize(size);
        setContainerManager(containerManager);
        setContainerCloudletScheduler(containerCloudletScheduler);
        setInMigration(false);
        setBeingInstantiated(true);
        setCurrentAllocatedBw(0);
        setCurrentAllocatedMips(null);
        setCurrentAllocatedRam(0);
        setCurrentAllocatedSize(0);
        setSchedulingInterval(schedulingInterval);
    }

    /**
     * Updates the processing of cloudlets running on this Container.
     *
     * @param currentTime current simulation time
     * @param mipsShare   array with MIPS share of each Pe available to the scheduler
     * @return time predicted completion time of the earliest finishing cloudlet, or 0 if there is no
     * next events
     * @pre currentTime >= 0
     * @post $none
     */
    public double updateContainerProcessing(double currentTime, List<Double> mipsShare) {
        if (mipsShare != null) {
            return getContainerCloudletScheduler().updateContainerProcessing(currentTime, mipsShare);
        }
        return 0.0;
    }


    /**
     * Gets the current requested total mips.
     *
     * @return the current requested total mips
     */
    public double getCurrentRequestedTotalMips() {
        return getCurrentRequestedMips().stream().mapToDouble(mips -> mips).sum();
    }

    /**
     * Gets the current requested max mips among all virtual PEs.
     *
     * @return the current requested max mips
     */
    public double getCurrentRequestedMaxMips() {
        return getCurrentRequestedMips().stream().mapToDouble(mips -> mips).filter(mips -> mips >= 0).max().orElse(0);
    }

    /**
     * Gets the current requested bw.
     *
     * @return the current requested bw
     */
    public long getCurrentRequestedBw() {
        if (isBeingInstantiated()) {
            return getBw();
        }
        return (long) (getContainerCloudletScheduler().getCurrentRequestedUtilizationOfBw() * getBw());
    }

    /**
     * Gets the current requested ram.
     *
     * @return the current requested ram
     */
    public float getCurrentRequestedRam() {
        if (isBeingInstantiated()) {
            return getRam();
        }
        return (float) (getContainerCloudletScheduler().getCurrentRequestedUtilizationOfRam() * getRam());
    }

    /**
     * Get utilization created by all cloudlets running on this container.
     *
     * @param time the time
     * @return total utilization
     */
    public double getTotalUtilizationOfCpu(double time) {
        //Log.printLine("Container: get Current getTotalUtilizationOfCpu"+ getContainerCloudletScheduler().getTotalUtilizationOfCpu(time));
        return getContainerCloudletScheduler().getTotalUtilizationOfCpu(time);
    }

    /**
     * Get utilization created by all cloudlets running on this container in MIPS.
     *
     * @param time the time
     * @return total utilization
     */
    public double getTotalUtilizationOfCpuMips(double time) {
        //Log.printLine("Container: get Current getTotalUtilizationOfCpuMips"+getTotalUtilizationOfCpu(time) * getMips());
        return getTotalUtilizationOfCpu(time) * getMips();
    }


    /**
     * Generate unique string identificator of the container.
     *
     * @param userId the user id
     * @param containerId   the container id
     * @return string uid
     */
    public static String getUid(int userId, int containerId) {
        return userId + "-" + containerId;
    }

    /**
     * Sets the mips.
     *
     * @param mips the new mips
     */
    public void changeMips(double mips) {
        this.mips = mips;
    }


    /**
     * Gets the current allocated mips.
     *
     * @return the current allocated mips
     */
    public List<Double> getCurrentAllocatedMips() {
        return currentAllocatedMips;
    }

    /**
     * Sets the being instantiated.
     *
     * @param beingInstantiated the new being instantiated
     */
    public void setBeingInstantiated(boolean beingInstantiated) {
        this.beingInstantiated = beingInstantiated;
    }

    /**
     * Adds the state history entry.
     *
     * @param time          the time
     * @param allocatedMips the allocated mips
     * @param requestedMips the requested mips
     * @param isInMigration the is in migration
     */
    public void addStateHistoryEntry(
            double time,
            double allocatedMips,
            double requestedMips,
            boolean isInMigration) {
        VmStateHistoryEntry newState = new VmStateHistoryEntry(
                time,
                allocatedMips,
                requestedMips,
                isInMigration);
        if (!getStateHistory().isEmpty()) {
            VmStateHistoryEntry previousState = getStateHistory().get(getStateHistory().size() - 1);
            if (previousState.getTime() == time) {
                getStateHistory().set(getStateHistory().size() - 1, newState);
                return;
            }
        }
        getStateHistory().add(newState);
    }


    /**
     * Gets the utilization MAD in MIPS.
     *
     * @return the utilization mean in MIPS
     */
    public double getUtilizationMad() {
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
    public double getUtilizationMean() {
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
    public double getUtilizationVariance() {
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


    //

    public List<Double> getCurrentRequestedMips() {
        if (isBeingInstantiated()) {
            List<Double> currentRequestedMips = new ArrayList<>();

            for (int i = 0; i < getNumberOfPes(); i++) {
                currentRequestedMips.add(getMips());

            }

            return currentRequestedMips;
        }


        return getContainerCloudletScheduler().getCurrentRequestedMips();
    }

    /**
     * Gets the current requested total mips.
     *
     * @return the current requested total mips
     */
    public double getWorkloadTotalMips() {

        //Log.printLine("Container: get Current totalRequestedMips"+ totalRequestedMips);
        return getWorkloadMips() * getNumberOfPes();
    }


}
