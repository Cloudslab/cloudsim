/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.EX.disk;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.EX.util.CustomLog;
import org.cloudbus.cloudsim.lists.CloudletList;

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
 * @author Remo Andreoli
 * 
 */
public class HddCloudletSchedulerTimeShared extends CloudletSchedulerTimeShared {

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
    public double updateCloudletsProcessing(final double currentTime, final List<Double> mipsShare) {
        return updateVmProcessing(currentTime, mipsShare, List.of());
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

        double timeSpan = currentTime - getPreviousTime();

        int[] disksToNumCloudlets = disksToNumCloudlets();
        int[] disksToNumCopy = Arrays.copyOf(disksToNumCloudlets, disksToNumCloudlets.length);
        for (ListIterator<HddCloudlet> iter = this.<HddCloudlet> getCloudletExecList().listIterator(); iter
                .hasNext();) {
            HddCloudlet cl = iter.next();
            //This shared value means the value that cpu has been allocated completely for this cloudlet.
            // so the cpu has been working equal this value for duration of the time
            long cpuFinishedSoFar = (long) (timeSpan * getTotalCurrentAllocatedMipsForCloudlet(cl, currentTime) * Consts.MILLION);
            long ioFinishedSoFar = (long) (getIOCapacity(iopsShare, disksToNumCloudlets, cl) * timeSpan
                    * cl.getNumberOfHddPes() * Consts.MILLION);

            cl.updateCloudletFinishedSoFar(cpuFinishedSoFar, ioFinishedSoFar);

            // Check if it is finished now ... after we just updated it
            long remainingLength = cl.getRemainingCloudletLength();
            long remainingIOLength = cl.getRemainingCloudletIOLength();
            if (remainingLength == 0 && remainingIOLength == 0) {
                cloudletFinish(cl);
                iter.remove();

                // Update the disksToNumCopy, since it is expensive to recompute
                updateDisksToNumMapping(disksToNumCopy, cl);
            }
        }

        double nextEvent = computeNextEventTime(currentTime, mipsShare, iopsShare, disksToNumCopy);
        setPreviousTime(currentTime);

        return nextEvent;
    }

    private void updateDisksToNumMapping(int[] disksToNumCopy, HddCloudlet cl) {
        List<? extends HddPe> pes = getVm().getHost().getHddList();
        DataItem dataItem = cl.getData();
        if (dataItem != null && cl.getRemainingCloudletIOLength() == 0) {
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
        // int[] disksToNumCloudlets = disksToNumCloudlets();

        // estimate finish time of cloudlets
        for (HddCloudlet cl : this.<HddCloudlet> getCloudletExecList()) {
            double estimatedFinishCPUTime = cl.getRemainingCloudletLength() == 0 ? Double.NaN : getEstimatedFinishTime(cl, currentTime);
            double estimatedFinishIOTime = cl.getRemainingCloudletIOLength() == 0 ? Double.NaN : currentTime
                    + (cl.getRemainingCloudletIOLength() / (getIOCapacity(iopsShare, disksToNumCloudlets, cl) * cl
                            .getNumberOfHddPes()));

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
    // DataItem dataItem = rcl.getCloudlet().getTaskLength();
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
    // DataItem cloudLetItem = resCloudlet.getCloudlet().getTaskLength();
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

    private double getIOCapacity(final List<Double> mipsShare, int[] diskToCloudlets, final HddCloudlet cl) {
        DataItem dataItem = cl.getData();
        double result = 0;
        if (dataItem != null && cl.getRemainingCloudletIOLength() > 0) {
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
        for (HddCloudlet cl : this.<HddCloudlet> getCloudletExecList()) {
            DataItem dataItem = cl.getData();
            if (dataItem != null && cl.getRemainingCloudletIOLength() > 0) {
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
    // DataItem dataItem = rcl.getCloudlet().getTaskLength();
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
        int position = CloudletList.getPositionById(getCloudletExecList(), cloudletId);

        if (position >= 0) {
            // remove cloudlet from the exec list and put it in the paused list
            HddCloudlet cl = this.<HddCloudlet> getCloudletExecList().remove(position);
            if (cl.isDone()) {
                cloudletFinish(cl);
            } else {
                cl.updateStatus(Cloudlet.CloudletStatus.PAUSED);
                getCloudletPausedList().add(cl);
            }
            return true;
        }
        return false;
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
        int position = CloudletList.getPositionById(getCloudletPausedList(), cloudletId);

        if (position >= 0) {
            HddCloudlet cl = this.<HddCloudlet> getCloudletPausedList().remove(position);
            cl.updateStatus(Cloudlet.CloudletStatus.INEXEC);
            getCloudletExecList().add(cl);

            // calculate the expected time for cloudlet completion
            // first: how many PEs do we have?

            double remainingLength = cl.getRemainingCloudletLength();
            double remainingIOLength = cl.getRemainingCloudletIOLength();
            double estimatedFinishCPUTime = remainingLength == 0 ? Double.NaN : CloudSim.clock()
                    + (remainingLength / getTotalCurrentAllocatedMipsForCloudlet(cl, CloudSim.clock()));
            double estimatedFinishIOTime = remainingIOLength == 0 ? Double.NaN : CloudSim.clock()
                    + (remainingIOLength / (getIOCapacity(getCurrentIOMipsShare(), disksToNumCloudlets(), cl) * cl
                            .getNumberOfHddPes()));

            return nanMin(estimatedFinishCPUTime, estimatedFinishIOTime);
        }

        return 0.0;
    }

    /**
     * Receives an cloudlet to be executed in the VM managed by this scheduler.
     * 
     * @param cl
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
    public double cloudletSubmit(final Cloudlet cl, final double fileTransferTime) {
        HddCloudlet hddCloudlet = (HddCloudlet) cl;

        hddCloudlet.updateStatus(Cloudlet.CloudletStatus.INEXEC);

        if (containsDataFor(hddCloudlet)) {
            getCloudletExecList().add(hddCloudlet);

            // use the current capacity to estimate the extra amount of
            // time to file transferring. It must be added to the cloudlet
            // length
            double cpuCapacity = getCurrentCapacity();
            double extraSize = cpuCapacity * fileTransferTime;
            long cpuLength = (long) (hddCloudlet.getCloudletLength() + extraSize);
            long ioLength = hddCloudlet.getCloudletIOLength();
            hddCloudlet.setCloudletLength(cpuLength);
            hddCloudlet.setCloudletIOLength(ioLength);

            double cpuEst = hddCloudlet.getCloudletLength() == 0 ? Double.NaN : hddCloudlet.getCloudletLength()
                    / cpuCapacity;
            double ioEst = hddCloudlet.getCloudletIOLength() == 0 ? Double.NaN : hddCloudlet.getCloudletIOLength()
                    / getIOCapacity(getCurrentIOMipsShare(), disksToNumCloudlets(), hddCloudlet);

            return nanMin(cpuEst, ioEst);
        } else {
            CustomLog.printf("Cloudlet %d could not be served on VM %d, since its data item #%d is not accessible.",
                    hddCloudlet.getCloudletId(), getVm().getId(), hddCloudlet.getData().getId());
            failCloudlet(hddCloudlet);
            return 0;
        }
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
     * Returns the next cloudlet in the failed list, $null if this list is
     * empty.
     * 
     * @return a failed cloudlet
     */
    public Cloudlet getNextFailedCloudlet() {
        if (!getCloudletFailedList().isEmpty()) {
            return getCloudletFailedList().remove(0);
        }
        return null;
    }

    public List<Double> getCurrentRequestedIOMips() {
        List<Double> ioMipsShare = new ArrayList<>();
        return ioMipsShare;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void failAllCloudlets() {
        for (ListIterator<HddCloudlet> iter = this.<HddCloudlet> getCloudletExecList().listIterator(); iter
                .hasNext();) {
            HddCloudlet hddCloudlet = iter.next();
            iter.remove();
            hddCloudlet.updateStatus(Cloudlet.CloudletStatus.FAILED);
            ((List) cloudletFailedList).add(hddCloudlet);
        }

        for (ListIterator<HddCloudlet> iter = this.<HddCloudlet> getCloudletPausedList().listIterator(); iter
                .hasNext();) {
            HddCloudlet hddCloudlet = iter.next();
            iter.remove();
            hddCloudlet.updateStatus(Cloudlet.CloudletStatus.FAILED);
            ((List) cloudletFailedList).add(hddCloudlet);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void failCloudlet(final HddCloudlet hddResCloudlet) {
        getCloudletExecList().remove(hddResCloudlet);
        getCloudletFailedList().remove(hddResCloudlet);
        hddResCloudlet.updateStatus(Cloudlet.CloudletStatus.FAILED);
        ((List) cloudletFailedList).add(hddResCloudlet);
    }

    private boolean containsDataFor(final HddCloudlet cl) {
        DataItem dataItem = cl.getData();
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
        cl.updateStatus(Cloudlet.CloudletStatus.FAILED);
        getCloudletFailedList().add(cl);
    }
}
