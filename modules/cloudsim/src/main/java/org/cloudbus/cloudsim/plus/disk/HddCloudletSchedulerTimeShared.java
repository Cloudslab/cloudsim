/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.plus.disk;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Consts;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.plus.util.CustomLog;
import org.cloudbus.cloudsim.lists.ResCloudletList;

import java.util.*;
import java.util.logging.Level;

/**
 * HddCloudletSchedulerTimeShared implements a policy of scheduling performed by
 * a virtual machine. Unlike other cloudlet schedulers, this one takes into
 * account the I/O operations required by the cloudlet.
 * 
 * <br>
 * <br>
 * 
 * Unlike other {@link CloudletScheduler}s this class keeps a reference to the
 * VM, whose cloudlets are scheduled. Thus one needs to set the vm before using
 * an instance of this scheduler.
 * 
 * <br>
 * Adapted from code of:
 * <ul>
 * <li>Rodrigo N. Calheiros</li>
 * <li>Anton Beloglazov</li>
 * </ul>
 * 
 * @author nikolay.grozev
 * 
 * 
 */
public class HddCloudletSchedulerTimeShared extends CloudletScheduler {

    /** The current IO mips share. */
    private List<Double> currentIOMipsShare;

    /** The VM being scheduled. */
    private HddVm vm;

    /**
     * Creates a new CloudletSchedulerTimeShared object. This method must be
     * invoked before starting the actual simulation.
     * 
     * @pre $none
     * @post $none
     */
    public HddCloudletSchedulerTimeShared() {
        super();

        // We replace the ArrayList from the parent with a LindedList, since we
        // need better performance for insertion/deletion etc.
        cloudletWaitingList = new LinkedList<>();
        cloudletExecList = new LinkedList<>();
        cloudletPausedList = new LinkedList<>();
        cloudletFinishedList = new LinkedList<>();
        cloudletFailedList = new LinkedList<>();
    }

    public HddVm getVm() {
        return vm;
    }

    public void setVm(final HddVm vm) {
        this.vm = vm;
    }

    /**
     * Returns the current IO Mips share.
     * 
     * @return the current IO Mips share.
     */
    public List<Double> getCurrentIOMipsShare() {
        return currentIOMipsShare;
    }

    /**
     * Sets the current IO Mips share.
     * 
     * @param currentIOMipsShare
     *            - the current IO Mips share.
     */
    public void setCurrentIOMipsShare(final List<Double> currentIOMipsShare) {
        this.currentIOMipsShare = currentIOMipsShare;
    }

    /**
     * Updates the processing of cloudlets running under management of this
     * scheduler.
     * 
     * @param currentTime
     *            current simulation time
     * @param mipsShare
     *            array with MIPS share of each processor available to the
     *            scheduler
     * @return time predicted completion time of the earliest finishing
     *         cloudlet, or 0 if there is no next events
     * @pre currentTime >= 0
     * @post $none
     */
    @Override
    public double updateVmProcessing(final double currentTime, final List<Double> mipsShare) {
        return updateVmProcessing(currentTime, mipsShare, Arrays.<Double> asList());
    }

    /**
     * 
     * @param currentTime
     * @param mipsShare
     * @param iopsShare
     * @return
     */
    public double updateVmProcessing(final double currentTime, final List<Double> mipsShare,
            final List<Double> iopsShare) {
        CustomLog.printf(Level.FINEST, "\nupdateVmProcessing(currentTime=%f, mipsShare=%s,final iopsShare=%s)",
                currentTime, mipsShare.toString(), iopsShare.toString());

        setCurrentMipsShare(mipsShare);
        setCurrentIOMipsShare(iopsShare);

        if (getCloudletExecList().isEmpty()) {
            setPreviousTime(currentTime);
            return 0.0;
        }

        double timeSpam = currentTime - getPreviousTime();

        double cpuCapacity = getCPUCapacity(mipsShare);
        int[] disksToNumCloudlets = disksToNumCloudlets();
        int[] disksToNumCopy = Arrays.copyOf(disksToNumCloudlets, disksToNumCloudlets.length);
        for (ListIterator<HddResCloudlet> iter = this.<HddResCloudlet> getCloudletExecList().listIterator(); iter
                .hasNext();) {
            HddResCloudlet rcl = iter.next();
            //This shared value means the value that cpu has been allocated completely for this cloudlet.
            // so the cpu has been working equal this value for duration of the time
            long cpuFinishedSoFar = (long) (cpuCapacity * timeSpam * rcl.getNumberOfPes() * Consts.MILLION);
            long ioFinishedSoFar = (long) (getIOCapacity(iopsShare, disksToNumCloudlets, rcl) * timeSpam
                    * rcl.getNumberOfHdds() * Consts.MILLION);

            rcl.updateCloudletFinishedSoFar(cpuFinishedSoFar, ioFinishedSoFar);

            // Check if it is finished now ... after we just updated it
            long remainingLength = rcl.getRemainingCloudletLength();
            long remainingIOLength = rcl.getRemainingCloudletIOLength();
            if (remainingLength == 0 && remainingIOLength == 0) {
                cloudletFinish(rcl);
                iter.remove();

                // Update the disksToNumCopy, since it is expensive to recompute
                updateDisksToNumMapping(disksToNumCopy, rcl);
            }
        }

        double nextEvent = computeNextEventTime(currentTime, mipsShare, iopsShare, disksToNumCopy);
        setPreviousTime(currentTime);

        return nextEvent;
    }

    private void updateDisksToNumMapping(int[] disksToNumCopy, HddResCloudlet rcl) {
        List<? extends HddPe> pes = getVm().getHost().getHddList();
        DataItem dataItem = rcl.getCloudlet().getData();
        if (dataItem != null && rcl.getRemainingCloudletIOLength() == 0) {
            for (int i = 0; i < pes.size(); i++) {
                // Does the cloudlet use the disk
                if (pes.get(i).containsDataItem(dataItem.getId())) {
                    disksToNumCopy[i]--;
                    break;
                }
            }
        }
    }

    private double computeNextEventTime(final double currentTime, final List<Double> mipsShare,
            final List<Double> iopsShare, int[] disksToNumCloudlets) {
        // check finished cloudlets
        double nextEvent = Double.MAX_VALUE;

        double cpuCapacity = getCPUCapacity(mipsShare);
        // int[] disksToNumCloudlets = disksToNumCloudlets();

        // estimate finish time of cloudlets
        for (HddResCloudlet rcl : this.<HddResCloudlet> getCloudletExecList()) {
            double estimatedFinishCPUTime = rcl.getRemainingCloudletLength() == 0 ? Double.NaN : currentTime
                    + (rcl.getRemainingCloudletLength() / (cpuCapacity * rcl.getNumberOfPes()));
            double estimatedFinishIOTime = rcl.getRemainingCloudletIOLength() == 0 ? Double.NaN : currentTime
                    + (rcl.getRemainingCloudletIOLength() / (getIOCapacity(iopsShare, disksToNumCloudlets, rcl) * rcl
                            .getNumberOfHdds()));

            double estimatedFinishTime = nanMin(estimatedFinishCPUTime, estimatedFinishIOTime);

            if (!Double.isNaN(estimatedFinishTime)
                    && estimatedFinishTime - currentTime < CloudSim.getMinTimeBetweenEvents()) {
                estimatedFinishTime = currentTime + CloudSim.getMinTimeBetweenEvents();
            }

            if (!Double.isNaN(estimatedFinishTime) && estimatedFinishTime < nextEvent) {
                nextEvent = estimatedFinishTime;
            }
        }
        return nextEvent;
    }

    private static double nanMin(final double estimatedFinishCPUTime, final double estimatedFinishIOTime) {
        double estimatedFinishTime = Double.NaN;
        if (Double.isNaN(estimatedFinishCPUTime)) {
            estimatedFinishTime = estimatedFinishIOTime;
        } else if (Double.isNaN(estimatedFinishIOTime)) {
            estimatedFinishTime = estimatedFinishCPUTime;
        } else {
            estimatedFinishTime = Math.min(estimatedFinishCPUTime, estimatedFinishIOTime);
        }
        return estimatedFinishTime;
    }

    // private void removeFinishedCloudlets() {
    // for (ListIterator<HddResCloudlet> iter = this.<HddResCloudlet>
    // getCloudletExecList().listIterator(); iter
    // .hasNext();) {
    // HddResCloudlet rcl = iter.next();
    // long remainingLength = rcl.getRemainingCloudletLength();
    // long remainingIOLength = rcl.getRemainingCloudletIOLength();
    //
    // if (remainingLength == 0 && remainingIOLength == 0) {
    // iter.remove();
    // cloudletFinish(rcl);
    // }
    // }
    // }

    // private double getIOCapacity(final List<Double> mipsShare, final
    // HddResCloudlet rcl) {
    // DataItem dataItem = rcl.getCloudlet().getData();
    // double result = 0;
    // if (dataItem != null && rcl.getRemainingCloudletIOLength() > 0) {
    // List<? extends HddPe> pes = getVm().getHost().getHddList();
    //
    // // Get the index of the disk, containing the data item
    // int hddIndxInHost = -1;
    // for (int i = 0; i < pes.size(); i++) {
    // if (pes.get(i).containsDataItem(dataItem.getId())) {
    // hddIndxInHost = i;
    // break;
    // }
    // }
    //
    // if (hddIndxInHost >= 0) {
    // HddPe hdd = pes.get(hddIndxInHost);
    //
    // int count = 0;
    // for (HddResCloudlet resCloudlet : this.<HddResCloudlet>
    // getCloudletExecList()) {
    // DataItem cloudLetItem = resCloudlet.getCloudlet().getData();
    // // Does the cloudlet use the disk
    // if (cloudLetItem != null && hdd.containsDataItem(cloudLetItem.getId())
    // && resCloudlet.getRemainingCloudletIOLength() > 0) {
    // count++;
    // }
    // }
    //
    // // The result is the IOPS of the harddisk divided by the number
    // // of cloudlets using it
    // result = mipsShare.get(hddIndxInHost) / count;
    // }
    // }
    // return result;
    // }

    private double getIOCapacity(final List<Double> mipsShare, int[] diskToCloudlets, final HddResCloudlet rcl) {
        DataItem dataItem = rcl.getCloudlet().getData();
        double result = 0;
        if (dataItem != null && rcl.getRemainingCloudletIOLength() > 0) {
            List<? extends HddPe> pes = getVm().getHost().getHddList();

            // Get the index of the disk, containing the data item
            int hddIndxInHost = -1;
            for (int i = 0; i < pes.size(); i++) {
                if (pes.get(i).containsDataItem(dataItem.getId())) {
                    hddIndxInHost = i;
                    break;
                }
            }

            if (hddIndxInHost >= 0) {
                // The result is the IOPS of the harddisk divided by the number
                // of cloudlets using it
                result = mipsShare.get(hddIndxInHost) / diskToCloudlets[hddIndxInHost];
            }
        }
        return result;
    }

    /**
     * Returns how many cloudlets use each of the disks. For example result[0]
     * returns how many cloudlets use the disk
     * getVm().getHost().getHddList()[0].
     * 
     * @return how many cloudlets use each of the disks.
     */
    private int[] disksToNumCloudlets() {
        List<? extends HddPe> pes = getVm().getHost().getHddList();
        int[] res = new int[pes.size()];
        for (HddResCloudlet rcl : this.<HddResCloudlet> getCloudletExecList()) {
            DataItem dataItem = rcl.getCloudlet().getData();
            if (dataItem != null && rcl.getRemainingCloudletIOLength() > 0) {
                for (int i = 0; i < pes.size(); i++) {
                    // Does the cloudlet use the disk
                    if (pes.get(i).containsDataItem(dataItem.getId())) {
                        res[i]++;
                        break;
                    }
                }
            }
        }
        return res;
    }

    // private void updateDiskToNumCloudlets(int[] disksToNum,
    // List<HddResCloudlet> finished) {
    // List<? extends HddPe> pes = getVm().getHost().getHddList();
    // for (HddResCloudlet rcl : finished) {
    // DataItem dataItem = rcl.getCloudlet().getData();
    // if (dataItem != null && rcl.getRemainingCloudletIOLength() == 0) {
    // for (int i = 0; i < pes.size(); i++) {
    // // Does the cloudlet use the disk
    // if (pes.get(i).containsDataItem(dataItem.getId())) {
    // disksToNum[i]--;
    // break;
    // }
    // }
    // }
    // }
    // }

    public double getCPUCapacity(final List<Double> mipsShare) {
        double capacity = 0.0;
        int cpus = 0;
        for (Double mips : mipsShare) {
            capacity += mips;
            if (mips > 0.0) {
                cpus++;
            }
        }

        int pesInUse = 0;
        for (HddResCloudlet rcl : this.<HddResCloudlet> getCloudletExecList()) {
            if (rcl.getRemainingCloudletLength() != 0) {
                pesInUse += rcl.getNumberOfPes();
            }
        }

        if (pesInUse > cpus) {
            capacity /= pesInUse;
        } else {
            capacity /= cpus;
        }
        return capacity;
    }

    /**
     * Cancels execution of a cloudlet.
     * 
     * @param cloudletId
     *            ID of the cloudlet being cancealed
     * @return the canceled cloudlet, $null if not found
     * @pre $none
     * @post $none
     */
    @Override
    public HddCloudlet cloudletCancel(final int cloudletId) {
        // First, looks in the finished queue
        int position = ResCloudletList.getPositionById(getCloudletFinishedList(), cloudletId);

        if (position >= 0) {
            return this.<HddResCloudlet> getCloudletFinishedList().remove(position).getCloudlet();
        }

        // Then searches in the exec list
        position = ResCloudletList.getPositionById(getCloudletExecList(), cloudletId);

        if (position >= 0) {
            HddResCloudlet rcl = this.<HddResCloudlet> getCloudletExecList().remove(position);
            if (rcl.isDone()) {
                cloudletFinish(rcl);
            } else {
                rcl.setCloudletStatus(HddCloudlet.CANCELED);
            }
            return rcl.getCloudlet();
        }

        // Now, looks in the paused queue
        position = ResCloudletList.getPositionById(getCloudletPausedList(), cloudletId);
        if (position >= 0) {
            return this.<HddResCloudlet> getCloudletPausedList().remove(position).getCloudlet();
        }

        return null;
    }

    /**
     * Pauses execution of a cloudlet.
     * 
     * @param cloudletId
     *            ID of the cloudlet being paused
     * @return $true if cloudlet paused, $false otherwise
     * @pre $none
     * @post $none
     */
    @Override
    public boolean cloudletPause(final int cloudletId) {
        int position = ResCloudletList.getPositionById(getCloudletExecList(), cloudletId);

        if (position >= 0) {
            // remove cloudlet from the exec list and put it in the paused list
            HddResCloudlet rcl = this.<HddResCloudlet> getCloudletExecList().remove(position);
            if (rcl.isDone()) {
                cloudletFinish(rcl);
            } else {
                rcl.setCloudletStatus(HddCloudlet.PAUSED);
                getCloudletPausedList().add(rcl);
            }
            return true;
        }
        return false;
    }

    /**
     * Processes a finished cloudlet.
     * 
     * @param rcl
     *            finished cloudlet
     * @pre rgl != $null
     * @post $none
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void cloudletFinish(final ResCloudlet rcl) {
        rcl.setCloudletStatus(HddCloudlet.SUCCESS);
        rcl.finalizeCloudlet();
        ((List) getCloudletFinishedList()).add(rcl);
    }

    /**
     * Resumes execution of a paused cloudlet.
     * 
     * @param cloudletId
     *            ID of the cloudlet being resumed
     * @return expected finish time of the cloudlet, 0.0 if queued
     * @pre $none
     * @post $none
     */
    // Changed
    @Override
    public double cloudletResume(final int cloudletId) {
        int position = ResCloudletList.getPositionById(getCloudletPausedList(), cloudletId);

        if (position >= 0) {
            HddResCloudlet rgl = this.<HddResCloudlet> getCloudletPausedList().remove(position);
            rgl.setCloudletStatus(HddCloudlet.INEXEC);
            getCloudletExecList().add(rgl);

            // calculate the expected time for cloudlet completion
            // first: how many PEs do we have?

            double remainingLength = rgl.getRemainingCloudletLength();
            double remainingIOLength = rgl.getRemainingCloudletIOLength();
            double estimatedFinishCPUTime = remainingLength == 0 ? Double.NaN : CloudSim.clock()
                    + (remainingLength / (getCPUCapacity(getCurrentMipsShare()) * rgl.getNumberOfPes()));
            double estimatedFinishIOTime = remainingIOLength == 0 ? Double.NaN : CloudSim.clock()
                    + (remainingIOLength / (getIOCapacity(getCurrentIOMipsShare(), disksToNumCloudlets(), rgl) * rgl
                            .getNumberOfHdds()));

            return nanMin(estimatedFinishCPUTime, estimatedFinishIOTime);
        }

        return 0.0;
    }

    /**
     * Receives an cloudlet to be executed in the VM managed by this scheduler.
     * 
     * @param cloudlet
     *            the submited cloudlet
     * @param fileTransferTime
     *            time required to move the required files from the SAN to the
     *            VM
     * @return expected finish time of this cloudlet
     * @pre gl != null
     * @post $none
     */
    // Changed
    @Override
    public double cloudletSubmit(final Cloudlet cloudlet, final double fileTransferTime) {
        HddCloudlet hddCloudlet = (HddCloudlet) cloudlet;

        HddResCloudlet rcl = new HddResCloudlet(hddCloudlet);
        rcl.setCloudletStatus(HddCloudlet.INEXEC);
        for (int i = 0; i < hddCloudlet.getNumberOfPes(); i++) {
            rcl.setMachineAndPeId(0, i);
        }

        if (containsDataFor(rcl)) {
            getCloudletExecList().add(rcl);

            // use the current capacity to estimate the extra amount of
            // time to file transferring. It must be added to the cloudlet
            // length
            double extraSize = getCPUCapacity(getCurrentMipsShare()) * fileTransferTime;
            long cpuLength = (long) (hddCloudlet.getCloudletLength() + extraSize);
            long ioLength = hddCloudlet.getCloudletIOLength();
            hddCloudlet.setCloudletLength(cpuLength);
            hddCloudlet.setCloudletIOLength(ioLength);

            double cpuEst = hddCloudlet.getCloudletLength() == 0 ? Double.NaN : hddCloudlet.getCloudletLength()
                    / getCPUCapacity(getCurrentMipsShare());
            double ioEst = hddCloudlet.getCloudletIOLength() == 0 ? Double.NaN : hddCloudlet.getCloudletIOLength()
                    / getIOCapacity(getCurrentIOMipsShare(), disksToNumCloudlets(), rcl);

            return nanMin(cpuEst, ioEst);
        } else {
            CustomLog.printf("Cloudlet %d could not be served on VM %d, since its data item #%d is not accessible.",
                    hddCloudlet.getCloudletId(), getVm().getId(), hddCloudlet.getData().getId());
            failCloudlet(rcl);
            return 0;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see cloudsim.CloudletScheduler#cloudletSubmit(cloudsim.Cloudlet)
     */
    @Override
    public double cloudletSubmit(final Cloudlet cloudlet) {
        return cloudletSubmit(cloudlet, 0.0);
    }

    /**
     * Gets the status of a cloudlet.
     * 
     * @param cloudletId
     *            ID of the cloudlet
     * @return status of the cloudlet, -1 if cloudlet not found
     * @pre $none
     * @post $none
     */
    @Override
    public int getCloudletStatus(final int cloudletId) {
        int position = ResCloudletList.getPositionById(getCloudletExecList(), cloudletId);
        if (position >= 0) {
            return getCloudletExecList().get(position).getCloudletStatus();
        }
        position = ResCloudletList.getPositionById(getCloudletExecList(), cloudletId);
        if (position >= 0) {
            return getCloudletPausedList().get(position).getCloudletStatus();
        }
        return -1;
    }

    /**
     * Get utilization created by all cloudlets.
     * 
     * @param time
     *            the time
     * @return total utilization
     */
    @Override
    public double getTotalUtilizationOfCpu(final double time) {
        double totalUtilization = 0;
        for (HddResCloudlet gl : this.<HddResCloudlet> getCloudletExecList()) {
            totalUtilization += gl.getCloudlet().getUtilizationOfCpu(time);
        }
        return totalUtilization;
    }

    /**
     * Informs about completion of some cloudlet in the VM managed by this
     * scheduler.
     * 
     * @return $true if there is at least one finished cloudlet; $false
     *         otherwise
     * @pre $none
     * @post $none
     */
    @Override
    public boolean isFinishedCloudlets() {
        return !getCloudletFinishedList().isEmpty();
    }

    /**
     * Informs about failure of some cloudlet in the VM managed by this
     * scheduler.
     * 
     * @return $true if there is at least one failed cloudlet; $false otherwise
     */
    public boolean isFailedCloudlets() {
        return !getCloudletFailedList().isEmpty();
    }

    /**
     * Returns the next cloudlet in the finished list, $null if this list is
     * empty.
     * 
     * @return a finished cloudlet
     * @pre $none
     * @post $none
     */
    @Override
    public HddCloudlet getNextFinishedCloudlet() {
        if (!getCloudletFinishedList().isEmpty()) {
            return this.<HddResCloudlet> getCloudletFinishedList().remove(0).getCloudlet();
        }
        return null;
    }

    /**
     * Returns the next cloudlet in the failed list, $null if this list is
     * empty.
     * 
     * @return a failed cloudlet
     */
    public Cloudlet getNextFailedCloudlet() {
        if (!getCloudletFailedList().isEmpty()) {
            return getCloudletFailedList().remove(0).getCloudlet();
        }
        return null;
    }

    /**
     * Returns the number of cloudlets runnning in the virtual machine.
     * 
     * @return number of cloudlets runnning
     * @pre $none
     * @post $none
     */
    @Override
    public int runningCloudlets() {
        return getCloudletExecList().size();
    }

    /**
     * Returns one cloudlet to migrate to another vm.
     * 
     * @return one running cloudlet
     * @pre $none
     * @post $none
     */
    @Override
    public Cloudlet migrateCloudlet() {
        HddResCloudlet rgl = this.<HddResCloudlet> getCloudletExecList().remove(0);
        rgl.finalizeCloudlet();
        return rgl.getCloudlet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see cloudsim.CloudletScheduler#getCurrentRequestedMips()
     */
    @Override
    public List<Double> getCurrentRequestedMips() {
        List<Double> mipsShare = new ArrayList<Double>();
        return mipsShare;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cloudsim.CloudletScheduler#getTotalCurrentAvailableMipsForCloudlet(cloudsim
     * .ResCloudlet, java.util.List)
     */
    @Override
    public double getTotalCurrentAvailableMipsForCloudlet(final ResCloudlet rcl, final List<Double> mipsShare) {
        return getCPUCapacity(getCurrentMipsShare());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cloudsim.CloudletScheduler#getTotalCurrentAllocatedMipsForCloudlet(cloudsim
     * .ResCloudlet, double)
     */
    @Override
    public double getTotalCurrentAllocatedMipsForCloudlet(final ResCloudlet rcl, final double time) {
        return 0.0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cloudsim.CloudletScheduler#getTotalCurrentRequestedMipsForCloudlet(cloudsim
     * .ResCloudlet, double)
     */
    @Override
    public double getTotalCurrentRequestedMipsForCloudlet(final ResCloudlet rcl, final double time) {
        // TODO Auto-generated method stub
        return 0.0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cloudbus.cloudsim.CloudletScheduler#getCurrentRequestedUtilizationOfRam
     * ()
     */
    @Override
    public double getCurrentRequestedUtilizationOfRam() {
        double ram = 0;
        for (ResCloudlet cloudlet : cloudletExecList) {
            ram += cloudlet.getCloudlet().getUtilizationOfRam(CloudSim.clock());
        }
        return ram;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cloudbus.cloudsim.CloudletScheduler#getCurrentRequestedUtilizationOfBw
     * ()
     */
    @Override
    public double getCurrentRequestedUtilizationOfBw() {
        double bw = 0;
        for (ResCloudlet cloudlet : cloudletExecList) {
            bw += cloudlet.getCloudlet().getUtilizationOfBw(CloudSim.clock());
        }
        return bw;
    }

    public List<Double> getCurrentRequestedIOMips() {
        List<Double> ioMipsShare = new ArrayList<>();
        return ioMipsShare;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void failAllCloudlets() {
        for (ListIterator<HddResCloudlet> iter = this.<HddResCloudlet> getCloudletExecList().listIterator(); iter
                .hasNext();) {
            HddResCloudlet hddResCloudlet = iter.next();
            iter.remove();
            hddResCloudlet.setCloudletStatus(Cloudlet.FAILED);
            ((List) cloudletFailedList).add(hddResCloudlet);
        }

        for (ListIterator<HddResCloudlet> iter = this.<HddResCloudlet> getCloudletPausedList().listIterator(); iter
                .hasNext();) {
            HddResCloudlet hddResCloudlet = iter.next();
            iter.remove();
            hddResCloudlet.setCloudletStatus(Cloudlet.FAILED);
            ((List) cloudletFailedList).add(hddResCloudlet);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void failCloudlet(final HddResCloudlet hddResCloudlet) {
        getCloudletExecList().remove(hddResCloudlet);
        getCloudletFailedList().remove(hddResCloudlet);
        hddResCloudlet.setCloudletStatus(Cloudlet.FAILED);
        ((List) cloudletFailedList).add(hddResCloudlet);
    }

    private boolean containsDataFor(final HddResCloudlet rcl) {
        DataItem dataItem = rcl.getCloudlet().getData();
        boolean result = dataItem == null;

        if (dataItem != null) {
            List<? extends HddPe> pes = getVm().getHost().getHddList();
            for (HddPe pe : pes) {
                if (pe.containsDataItem(dataItem.getId()) && vm.getHddsIds().contains(pe.getId())) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addFailedCloudlet(final HddCloudlet cl) throws Exception {
        cl.setCloudletStatus(Cloudlet.FAILED);
        ((List) getCloudletFailedList()).add(new HddResCloudlet(cl));
    }

}
