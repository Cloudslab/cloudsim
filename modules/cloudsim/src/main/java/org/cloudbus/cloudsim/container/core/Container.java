package org.cloudbus.cloudsim.container.core;

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
    private int id;

    /**
     * The user id.
     */
    private int userId;

    /**
     * The uid.
     */
    private String uid;

    /**
     * The size.
     */
    private long size;

    /**
     * The MIPS.
     */
    private double mips;

    /**
     * The workloadMips.
     */
    private double workloadMips;

    /**
     * The number of PEs.
     */
    private int numberOfPes;

    /**
     * The ram.
     */
    private float ram;

    /**
     * The bw.
     */
    private long bw;

    /**
     * The containerManager.
     */
    private String containerManager;

    /**
     * The ContainerCloudlet scheduler.
     */
    private ContainerCloudletScheduler containerCloudletScheduler;

    /**
     * The ContainerVm.
     */
    private ContainerVm vm;

    /**
     * In migration flag.
     */
    private boolean inMigration;

    /**
     * The current allocated size.
     */
    private long currentAllocatedSize;

    /**
     * The current allocated ram.
     */
    private float currentAllocatedRam;

    /**
     * The current allocated bw.
     */
    private long currentAllocatedBw;

    /**
     * The current allocated mips.
     */
    private List<Double> currentAllocatedMips;

    /**
     * The VM is being instantiated.
     */
    private boolean beingInstantiated;

    /**
     * The mips allocation history.
     */
    private final List<VmStateHistoryEntry> stateHistory = new LinkedList<VmStateHistoryEntry>();

//    added from the power Vm
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
        double totalRequestedMips = 0;
        for (double mips : getCurrentRequestedMips()) {
            totalRequestedMips += mips;
        }
        return totalRequestedMips;
    }

    /**
     * Gets the current requested max mips among all virtual PEs.
     *
     * @return the current requested max mips
     */
    public double getCurrentRequestedMaxMips() {
        double maxMips = 0;
        for (double mips : getCurrentRequestedMips()) {
            if (mips > maxMips) {
                maxMips = mips;
            }
        }
        return maxMips;
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
     * Sets the uid.
     *
     * @param uid the new uid
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Get unique string identificator of the container.
     *
     * @return string uid
     */
    public String getUid() {
        return uid;
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
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    protected void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the user id.
     *
     * @param userId the new user id
     */
    protected void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Gets the ID of the owner of the container.
     *
     * @return container's owner ID
     * @pre $none
     * @post $none
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the mips.
     *
     * @return the mips
     */
    public double getMips() {
        return mips;
    }

    /**
     * Sets the mips.
     *
     * @param mips the new mips
     */
    protected void setMips(double mips) {
        this.mips = mips;
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
     * Gets the number of pes.
     *
     * @return the number of pes
     */
    public int getNumberOfPes() {
        return numberOfPes;
    }

    /**
     * Sets the number of pes.
     *
     * @param numberOfPes the new number of pes
     */
    protected void setNumberOfPes(int numberOfPes) {
        this.numberOfPes = numberOfPes;
    }

    /**
     * Gets the amount of ram.
     *
     * @return amount of ram
     * @pre $none
     * @post $none
     */
    public float getRam() {
        return ram;
    }

    /**
     * Sets the amount of ram.
     *
     * @param ram new amount of ram
     * @pre ram > 0
     * @post $none
     */
    public void setRam(int ram) {
        this.ram = ram;
    }

    /**
     * Gets the amount of bandwidth.
     *
     * @return amount of bandwidth
     * @pre $none
     * @post $none
     */
    public long getBw() {
        return bw;
    }

    /**
     * Sets the amount of bandwidth.
     *
     * @param bw new amount of bandwidth
     * @pre bw > 0
     * @post $none
     */
    public void setBw(long bw) {
        this.bw = bw;
    }

    /**
     * Gets the amount of storage.
     *
     * @return amount of storage
     * @pre $none
     * @post $none
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the amount of storage.
     *
     * @param size new amount of storage
     * @pre size > 0
     * @post $none
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * Gets the VMM.
     *
     * @return VMM
     * @pre $none
     * @post $none
     */
    public String getContainerManager() {
        return containerManager;
    }

    /**
     * Sets the VMM.
     *
     * @param containerManager the new containerManager
     */
    protected void setContainerManager(String containerManager) {
        this.containerManager = containerManager;
    }

    /**
     * Gets the vm.
     *
     * @return the vm
     */
    public ContainerVm getVm() {
        return vm;
    }

    /**
     * Gets the containerCloudletScheduler.
     *
     * @return the containerCloudletScheduler
     */
    public ContainerCloudletScheduler getContainerCloudletScheduler() {
        return containerCloudletScheduler;
    }

    /**
     * Sets the VM that runs this container.
     *
     * @param vm VM running the container
     * @pre vm != $null
     * @post $none
     */
    public void setVm(ContainerVm vm) {
        this.vm = vm;
    }

    /**
     * Sets the Container Cloudlet Scheduler.
     *
     * @param containerCloudletScheduler the new Container Cloudlet Scheduler
     */
    protected void setContainerCloudletScheduler(ContainerCloudletScheduler containerCloudletScheduler) {
        this.containerCloudletScheduler = containerCloudletScheduler;
    }

    /**
     * Checks if is in migration.
     *
     * @return true, if is in migration
     */
    public boolean isInMigration() {
        return inMigration;
    }

    /**
     * Sets the in migration.
     *
     * @param inMigration the new in migration
     */
    public void setInMigration(boolean inMigration) {
        this.inMigration = inMigration;
    }

    /**
     * Gets the current allocated size.
     *
     * @return the current allocated size
     */
    public long getCurrentAllocatedSize() {
        return currentAllocatedSize;
    }

    /**
     * Sets the current allocated size.
     *
     * @param currentAllocatedSize the new current allocated size
     */
    protected void setCurrentAllocatedSize(long currentAllocatedSize) {
        this.currentAllocatedSize = currentAllocatedSize;
    }

    /**
     * Gets the current allocated ram.
     *
     * @return the current allocated ram
     */
    public float getCurrentAllocatedRam() {
        return currentAllocatedRam;
    }

    /**
     * Sets the current allocated ram.
     *
     * @param currentAllocatedRam the new current allocated ram
     */
    public void setCurrentAllocatedRam(float currentAllocatedRam) {
        this.currentAllocatedRam = currentAllocatedRam;
    }

    /**
     * Gets the current allocated bw.
     *
     * @return the current allocated bw
     */
    public long getCurrentAllocatedBw() {
        return currentAllocatedBw;
    }

    /**
     * Sets the current allocated bw.
     *
     * @param currentAllocatedBw the new current allocated bw
     */
    public void setCurrentAllocatedBw(long currentAllocatedBw) {
        this.currentAllocatedBw = currentAllocatedBw;
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
     * Sets the current allocated mips.
     *
     * @param currentAllocatedMips the new current allocated mips
     */
    public void setCurrentAllocatedMips(List<Double> currentAllocatedMips) {
        this.currentAllocatedMips = currentAllocatedMips;
    }

    /**
     * Checks if is being instantiated.
     *
     * @return true, if is being instantiated
     */
    public boolean isBeingInstantiated() {
        return beingInstantiated;
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
     * Gets the state history.
     *
     * @return the state history
     */
    public List<VmStateHistoryEntry> getStateHistory() {
        return stateHistory;
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
    protected List<Double> getUtilizationHistory() {
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


    protected void setSchedulingInterval(double schedulingInterval) {
        this.schedulingInterval = schedulingInterval;
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

    public double getWorkloadMips() {
        return workloadMips;
    }

    public void setWorkloadMips(double workloadMips) {
        this.workloadMips = workloadMips;
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
