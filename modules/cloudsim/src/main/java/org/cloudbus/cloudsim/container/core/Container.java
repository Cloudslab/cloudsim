/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sareh on 9/07/15.
 * Modified by Remo Andreoli (March 2024)
 */
public class Container implements GuestEntity {

    /** The id. */
    private final int id;

    /** The user id. */
    private int userId;

    /** The uid. */
    private String uid;

    /** The size. */
    private long size;

    /** The MIPS. */
    private double mips;

    /** The number of PEs. */
    private int numberOfPes;

    /** The ram. */
    private int ram;

    /** The bandwidth. */
    private long bw;

    /** The containerManager. */
    private String containerManager;

    /**
     * The Cloudlet scheduler.
     */
    private CloudletScheduler cloudletScheduler;

    private HostEntity host;

    /** In migration flag. */
    private boolean inMigration;

    /** The current allocated size. */
    private long currentAllocatedSize;

    /** The current allocated ram. */
    private int currentAllocatedRam;

    /** The current allocated bw. */
    private long currentAllocatedBw;

    /** The current allocated mips. */
    private List<Double> currentAllocatedMips;

    /** The VM is being instantiated. */
    private boolean beingInstantiated;

    /** The mips allocation history. */
    private final List<VmStateHistoryEntry> stateHistory = new LinkedList<>();

    /** The previous time. */
    private double previousTime;

    private int virtualizationOverhead;

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
            CloudletScheduler containerCloudletScheduler) {
        this.id = id;
        setUserId(userId);
        setUid(GuestEntity.getUid(userId, id));
        setMips(mips);
        setNumberOfPes(numberOfPes);
        setRam(ram);
        setBw(bw);
        setSize(size);
        setContainerManager(containerManager);
        setCloudletScheduler(containerCloudletScheduler);
        setInMigration(false);
        setBeingInstantiated(true);
        setCurrentAllocatedBw(0);
        setCurrentAllocatedMips(null);
        setCurrentAllocatedRam(0);
        setCurrentAllocatedSize(0);
        setVirtualizationOverhead(0);
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
    public double updateCloudletsProcessing(double currentTime, List<Double> mipsShare) {
        if (mipsShare != null) {
            return getCloudletScheduler().updateCloudletsProcessing(currentTime, mipsShare);
        }
        return 0.0;
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
        return (long) (getCloudletScheduler().getCurrentRequestedUtilizationOfBw() * getBw());
    }

    /**
     * Gets the current requested ram.
     *
     * @return the current requested ram
     */
    public int getCurrentRequestedRam() {
        if (isBeingInstantiated()) {
            return getRam();
        }
        return (int) (getCloudletScheduler().getCurrentRequestedUtilizationOfRam() * getRam());
    }

    /**
     * Get utilization created by all cloudlets running on this container.
     *
     * @param time the time
     * @return total utilization
     */
    public double getTotalUtilizationOfCpu(double time) {
        //Log.printLine("Container: get Current getTotalUtilizationOfCpu"+ getCloudletScheduler().getTotalUtilizationOfCpu(time));
        return getCloudletScheduler().getTotalUtilizationOfCpu(time);
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

    //
    @Override
    public List<Double> getCurrentRequestedMips() {
        if (isBeingInstantiated()) {
            List<Double> currentRequestedMips = new ArrayList<>();

            for (int i = 0; i < getNumberOfPes(); i++) {
                currentRequestedMips.add(getMips());

            }

            return currentRequestedMips;
        }


        return getCloudletScheduler().getCurrentRequestedMips();
    }

    @Override
    public double getCurrentRequestedTotalMips() {
        if (isBeingInstantiated())
            return getMips() * getNumberOfPes();
        else
            return getCloudletScheduler().getCurrentRequestedTotalMips();
    }

    public int getId() { return id; }

    public int getUserId() { return userId; }
    protected void setUserId(int userId) { this.userId = userId; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public double getMips() { return mips; }
    protected void setMips(double mips) { this.mips = mips; }

    public int getNumberOfPes() { return numberOfPes; }
    public void setNumberOfPes(int numberOfPes) { this.numberOfPes = numberOfPes; }

    public int getRam() { return ram; }
    public void setRam(int ram) { this.ram = ram; }

    public long getBw() { return bw; }
    public void setBw(long bw) { this.bw = bw; }

    public String getContainerManager() { return containerManager; }
    protected void setContainerManager(String containerManager) { this.containerManager = containerManager; }

    public CloudletScheduler getCloudletScheduler() { return cloudletScheduler; }

    @Override
    public int getVirtualizationOverhead() { return virtualizationOverhead; }

    @Override
    public void setVirtualizationOverhead(int overhead) { virtualizationOverhead = overhead; }

    protected void setCloudletScheduler(CloudletScheduler cloudletScheduler) {
        this.cloudletScheduler = cloudletScheduler;
    }

    public HostEntity getHost() { return host; }
    public void setHost(HostEntity host) {
        this.host = host;
    }

    public boolean isInMigration() { return inMigration; }
    public void setInMigration(boolean inMigration) { this.inMigration = inMigration; }

    public long getCurrentAllocatedSize() { return currentAllocatedSize; }
    protected void setCurrentAllocatedSize(long currentAllocatedSize) {
        this.currentAllocatedSize = currentAllocatedSize;
    }

    public int getCurrentAllocatedRam() { return currentAllocatedRam; }
    public void setCurrentAllocatedRam(int currentAllocatedRam) { this.currentAllocatedRam = currentAllocatedRam; }

    public long getCurrentAllocatedBw() { return currentAllocatedBw; }
    public void setCurrentAllocatedBw(long currentAllocatedBw) { this.currentAllocatedBw = currentAllocatedBw; }

    public double getPreviousTime() { return previousTime; }
    public void setPreviousTime(double previousTime) { this.previousTime = previousTime; }

    public double getTotalMips() {
        return getMips() * getNumberOfPes();
    }

    public void setCurrentAllocatedMips(List<Double> currentAllocatedMips) {
        this.currentAllocatedMips = currentAllocatedMips;
    }

    public boolean isBeingInstantiated() { return beingInstantiated; }

    public List<VmStateHistoryEntry> getStateHistory() { return stateHistory; }
}
