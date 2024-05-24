package org.cloudbus.cloudsim.EX.vm;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.EX.util.Id;

import java.util.EnumSet;
import java.util.Objects;

/**
 * An extension of the base cloudsim VM, adding information about:
 * creation/submission, starting/booting and termination times; the VM status
 * and metadata.
 * 
 * @author nikolay.grozev
 * @author Remo Andreoli
 */
public class VmEX extends Vm {

    private VmStatus status;
    private final VMMetadata metadata;

    private final String name;

    private double submissionTime;
    private double startTime;
    private double endTime;

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
     */
    public VmEX(final String name, final int userId, final double mips, final int numberOfPes, final int ram,
                final long bw, final long size, final String vmm, final CloudletScheduler cloudletScheduler) {
        this(name, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler, new VMMetadata());

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
     */
    public VmEX(final String name, final int userId, final double mips, final int numberOfPes, final int ram,
                final long bw, final long size, final String vmm, final CloudletScheduler cloudletScheduler,
                final VMMetadata metadata) {
        super(Id.pollId(Vm.class), userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
        this.name = name;
        this.metadata = metadata;
    }

    // Unfortunately the super class already has a boolean property if the VM is
    // in init state, so we need to make sure the to properties are in synch.
    // That's why we override the set/get methods to make sure they are synched.
    // ...

    @Override
    public void setBeingInstantiated(final boolean beingInstantiated) {
        if (status != null && status != VmStatus.INITIALISING) {
            throw new IllegalStateException("The initiated status can not be set if the VM is in " + status.name()
                    + " state.");
        }

        super.setBeingInstantiated(beingInstantiated);
        setStatus(super.isBeingInstantiated() ? VmStatus.INITIALISING : VmStatus.RUNNING);
    }

    @Override
    public boolean isBeingInstantiated() {
        if ((super.isBeingInstantiated() && status != null && status != VmStatus.INITIALISING)
                || (!super.isBeingInstantiated() && status == VmStatus.INITIALISING)) {
            throw new IllegalStateException("The initiated states are not in synch. state: " + status.name()
                    + " init flag:" + super.isBeingInstantiated());
        }
        return super.isBeingInstantiated();
    }

    /**
     * Returns the status of the VM.
     * 
     * @return the status of the VM.
     */
    public VmStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the VM, if possible. For example, if the VM is in
     * terminated state, and the init state is attempted to be set it will not
     * be possible to set the state. In case the state can not be set and
     * {@link IllegalStateException} is thrown. The logic about which state
     * transitions are possible is implemented in {@link VmStatus}.
     * 
     * @param status
     *            - the new status to set. Must not be null.
     */
    public void setStatus(final VmStatus status) {
        switch (status) {
            case INITIALISING -> setSubmissionTime(getCurrentTime());
            case RUNNING -> setStartTime(getCurrentTime());
            case TERMINATED -> setEndTime(getCurrentTime());
            default -> throw new IllegalArgumentException("Unknown status " + status.name());
        }

        this.status = status;
        super.setBeingInstantiated(VmStatus.INITIALISING == status);
    }

    /**
     * Sets the submission time (time of creation before booting) of the VM.
     * 
     * @return the submission time.
     */
    public double getSubmissionTime() {
        return submissionTime;
    }

    /**
     * Sets the submission time (time of creation before booting) of the VM.
     * 
     * @param submissionTime
     *            - the new submission time.
     */
    private void setSubmissionTime(double submissionTime) {
        this.submissionTime = submissionTime;
    }

    /**
     * Returns the starting time (after it has booted) of this VM.
     * 
     * @return - the starting time (after it has booted) of this VM.
     */
    public double getStartTime() {
        return startTime;
    }

    /**
     * Sets the starting time (after it has booted) of this VM.
     * 
     * @param startTime
     *            - the starting time (after it has booted) of this VM. Must be
     *            after the submission time.
     */
    private void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the ending time (due to failure or termination) of this VM. If
     * the VM is still running then -1 is returned.
     * 
     * @return the ending time (due to failure or termination) of this VM.
     */
    public double getEndTime() {
        return endTime;
    }

    /**
     * Sets the ending time (due to failure or termination) of this VM.
     * 
     * @param endTime
     *            - the end time. Must be after the starting time.
     */
    private void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    /**
     * Returns the duration for which this VM has been functional (after
     * booting).
     * 
     * @return the duration for which this VM has been functional (after
     *         booting).
     */
    public double getTimeAfterBooting() {
        double endTime = getEndTime() < 0 ? getCurrentTime() : getEndTime();
        return endTime - startTime;
    }

    /**
     * Returns the duration for which this VM has existed (after its creation).
     * 
     * @return the duration for which this VM has existed (after its creation).
     */
    public double getTimeAfterSubmission() {
        double endTime = getEndTime() < 0 ? getCurrentTime() : getEndTime();
        return endTime - submissionTime;
    }

    /**
     * Returns the name of the VM.
     * 
     * @return the name of the VM.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the metadata of this VM.
     * 
     * @return the metadata of this VM.
     */
    public VMMetadata getMetadata() {
        return metadata;
    }

    /**
     * If the VM is finished returns its lifetime period. Otherwise - 0;
     * 
     * @return if the VM is finished returns its lifetime period. Otherwise - 0;
     */
    public double getLifeDuration() {
        if (!EnumSet.of(VmStatus.INITIALISING, VmStatus.RUNNING).contains(getStatus())) {
            return getEndTime() - getStartTime();
        } else {
            return 0;
        }
    }

    /**
     * Returns the current simulation time. Can be overridden for test purposes,
     * as CloudSim.clock() can not be mocked.
     * 
     * @return the current simulation time.
     */
    protected double getCurrentTime() {
        return CloudSim.clock();
    }

    /**
     * Makes a deep copy of this VM. The only properties which are not copied
     * are the id, the scheduler, the status, and the submission/start/end
     * times. The id of the resulting VM is uniquely generated and the scheduler
     * is provided as a parameter.
     * 
     * @param scheduler
     *            - must not be null. Must be a valid scheduler.
     * @return a deep copy of this VM.
     */
    public VmEX clone(final CloudletScheduler scheduler) {
        if (!getClass().equals(VmEX.class)) {
            throw new IllegalStateException("The operation is undefined for subclass: " + getClass().getCanonicalName());
        }

        VmEX result = new VmEX(getName(), getUserId(), getMips(), getNumberOfPes(), getRam(), getBw(), getSize(),
                getVmm(), scheduler, getMetadata().clone());
        return result;
    }

    @Override
    public String toString() {
        return String.format("VM(%s, %d)", Objects.toString(getName(), "N/A"), getId());
    }
}
