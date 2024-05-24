package org.cloudbus.cloudsim.EX.disk;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.EX.VmSchedulerMapVmsToPes;
import org.cloudbus.cloudsim.EX.vm.MonitoredVmEX;
import org.cloudbus.cloudsim.EX.vm.VMMetadata;
import org.cloudbus.cloudsim.EX.vm.VmStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * A virtual machine with a harddisk. Unlike the other CloudSim implementations
 * of virtual machines, this one models the contention caused by the I/O
 * operations of the cloudlets.
 * 
 * @author nikolay.grozev
 * 
 */
public class HddVm extends MonitoredVmEX {

    /** The IO MIPS. */
    private double ioMips;
    private final LinkedHashSet<Integer> hdds = new LinkedHashSet<>();
    private boolean outOfMemory = false;

    /**
     * Constr.
     * 
     * @param name
     *            - see parent class.
     * @param userId
     *            - see parent class.
     * @param mips
     *            - see parent class.
     * @param numberOfPes
     *            - see parent class.
     * @param ram
     *            - see parent class.
     * @param bw
     *            - see parent class.
     * @param size
     *            - see parent class.
     * @param vmm
     *            - see parent class.
     * @param cloudletScheduler
     *            - the scheduler that will schedule the disk and CPU operations
     *            among cloudlets.
     * @param summaryPeriodLength
     *            - the summary period for performance measurement.
     * @param hddIds
     *            - a list of ids of the harddisks that this VM has access to.
     *            If empty the VM has access to all disks of the host, which i
     *            hosting it at a given time.
     */
    public HddVm(final String name, final int userId, final double mips, final double ioMips, final int numberOfPes,
                 final int ram, final long bw, final long size, final String vmm,
                 final HddCloudletSchedulerTimeShared cloudletScheduler, final double summaryPeriodLength,
                 final Integer[] hddIds) {
        super(name, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler, summaryPeriodLength);
        this.ioMips = ioMips;
        this.hdds.addAll(Arrays.asList(hddIds));
        cloudletScheduler.setVm(this);
    }

    public HddVm(final String name, final int userId, final double mips, final double ioMips, final int numberOfPes,
                 final int ram, final long bw, final long size, final String vmm,
                 final HddCloudletSchedulerTimeShared cloudletScheduler, final VMMetadata metadata,
                 final double summaryPeriodLength, final Integer[] hddIds) {
        super(name, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler, metadata, summaryPeriodLength);
        this.ioMips = ioMips;
        this.hdds.addAll(Arrays.asList(hddIds));
        cloudletScheduler.setVm(this);
    }

    /**
     * Constr.
     * 
     * @param name
     *            - see parent class.
     * @param userId
     *            - see parent class.
     * @param mips
     *            - see parent class.
     * @param numberOfPes
     *            - see parent class.
     * @param ram
     *            - see parent class.
     * @param bw
     *            - see parent class.
     * @param size
     *            - see parent class.
     * @param vmm
     *            - see parent class.
     * @param cloudletScheduler
     *            - the scheduler that will schedule the disk and CPU operations
     *            among cloudlets.
     * @param hddIds
     *            - a list of ids of the harddisks that this VM has access to.
     *            If empty the VM has access to all disks of the host, which is
     *            hosting it at a given time.
     */
    public HddVm(final String name, final int userId, final double mips, final double ioMips, final int numberOfPes,
                 final int ram, final long bw, final long size, final String vmm,
                 final HddCloudletSchedulerTimeShared cloudletScheduler, final Integer[] hddIds) {
        super(name, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler, 5);
        this.ioMips = ioMips;
        this.hdds.addAll(Arrays.asList(hddIds));
        cloudletScheduler.setVm(this);
    }

    public HddVm(final String name, final int userId, final double mips, final double ioMips, final int numberOfPes,
                 final int ram, final long bw, final long size, final String vmm,
                 final HddCloudletSchedulerTimeShared cloudletScheduler, final VMMetadata metadata, final Integer[] hddIds) {
        super(name, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler, metadata, 5);
        this.ioMips = ioMips;
        this.hdds.addAll(Arrays.asList(hddIds));
        cloudletScheduler.setVm(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cloudbus.cloudsim.Vm#updateVmProcessing(double, java.util.List)
     */
    @Override
    public double updateCloudletsProcessing(final double currentTime, final List<Double> mipsShare) {
        return updateVmProcessing(currentTime, mipsShare, List.of());
    }

    /**
     * Updates the processing of the VM.
     * 
     * @param currentTime
     *            - current simulation time.
     * @param mipsShare
     *            - array with MIPS share of each Pe available to the scheduler.
     * @param iopsShare
     *            - array with I/O MIPS share of each Harddisk available to the
     *            scheduler.
     * @return
     */
    public double updateVmProcessing(final double currentTime, final List<Double> mipsShare,
            final List<Double> iopsShare) {
        if (mipsShare != null && iopsShare != null) {
            return getCloudletScheduler().updateVmProcessing(currentTime, mipsShare, iopsShare);
        }
        return 0.0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cloudbus.cloudsim.Vm#getCloudletScheduler()
     */
    @Override
    public HddCloudletSchedulerTimeShared getCloudletScheduler() {
        return (HddCloudletSchedulerTimeShared) super.getCloudletScheduler();
    }

    public double getIoMips() {
        return ioMips;
    }

    public void setIoMips(final double ioMips) {
        this.ioMips = ioMips;
    }

    /**
     * Returns the number of the harddisks, that this VM can use.
     * 
     * @return the number of the harddisks, that this VM can use.
     */
    public int getNumberOfHdds() {
        return hdds.isEmpty() && getHost() != null ? getHost().getNumberOfHdds() : hdds.size();
    }

    /**
     * Returns the ids of the harddisks, that this VM can use.
     * 
     * @return the ids of the harddisks, that this VM can use.
     */
    public LinkedHashSet<Integer> getHddsIds() {
        LinkedHashSet<Integer> result = hdds;
        if (hdds.isEmpty() && getHost() != null) {
            result = new LinkedHashSet<>();
            for (HddPe hdd : getHost().getHddList()) {
                result.add(hdd.getId());
            }
        }
        return result;
    }

    @Override
    public List<Double> getCurrentRequestedMips() {
        if (getHost().getGuestScheduler() instanceof VmSchedulerMapVmsToPes<?> scheduler) {

            List<Double> currentRequestedMips = getCloudletScheduler().getCurrentRequestedMips();
            if (isBeingInstantiated()) {
                currentRequestedMips = new ArrayList<>();
                for (Pe pe : getHost().getPeList()) {
                    if (scheduler.doesVmUse(this, pe)) {
                        currentRequestedMips.add(getMips());
                    } else {
                        currentRequestedMips.add(0.0);
                    }
                }
                for (int i = 0; i < getNumberOfPes(); i++) {
                    currentRequestedMips.add(getMips());
                }
            }
            return currentRequestedMips;

        } else {
            return super.getCurrentRequestedMips();
        }
    }

    /**
     * Returns a mapping between hdd ids and required miops.
     * 
     * @return - a mapping between hdd ids and required miops.
     */

    public List<Double> getCurrentRequestedIOMips() {
        List<Double> currentRequestedMips = getCloudletScheduler().getCurrentRequestedIOMips();
        if (isBeingInstantiated()) {
            currentRequestedMips = new ArrayList<>();

            // Put zeros for the harddisks we don't have access to from this VM
            for (HddPe hdd : getHost().getHddList()) {
                if (this.getHddsIds().contains(hdd.getId())) {
                    currentRequestedMips.add(getIoMips());
                } else {
                    currentRequestedMips.add(0.0);
                }

            }
        }
        return currentRequestedMips;
    }

    public boolean isOutOfMemory() {
        return outOfMemory;
    }

    public void setOutOfMemory(final boolean outOfMemory) {
        this.outOfMemory = outOfMemory;
        if (outOfMemory) {
            setStatus(VmStatus.TERMINATED);
        }
    }

    @Override
    public HddHost getHost() {
        return (HddHost) super.getHost();
    }

    @Override
    public HddVm clone(final CloudletScheduler scheduler) {
        if (!getClass().equals(HddVm.class)) {
            throw new IllegalStateException("The operation is undefined for subclass: " + getClass().getCanonicalName());
        }

        // Does not copy the hard disks.
        HddVm result = new HddVm(getName(), getUserId(), getMips(), getIoMips(), getNumberOfPes(), getRam(), getBw(),
                getSize(), getVmm(), (HddCloudletSchedulerTimeShared) scheduler, getMetadata().clone(),
                getSummaryPeriodLength(), new Integer[0]);
        return result;
    }

}
