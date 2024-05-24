package org.cloudbus.cloudsim.EX.disk;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.EX.VmSchedulerWithIndependentPes;
import org.cloudbus.cloudsim.EX.util.Id;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.List;

/**
 * A host with one or several harddisks.
 * 
 * @author nikolay.grozev
 * 
 */
public class HddHost extends Host {

    /** The list of harddisks. */
    private final List<? extends HddPe> hddList;
    /** A scheduler for the harddisk operations. */
    private final VmSchedulerWithIndependentPes<HddPe> hddIOScheduler;

    /**
     * Constructor.
     * 
     * @param ramProvisioner
     * @param bwProvisioner
     * @param storage
     * @param peList
     * @param hddList
     * @param vmCPUScheduler
     *            - the CPU scheduler.
     * @param vmHDDScheduler
     *            - the IO scheduler.
     */
    public HddHost(final RamProvisioner ramProvisioner, final BwProvisioner bwProvisioner, final long storage,
                   final List<? extends Pe> peList, final List<? extends HddPe> hddList, final VmScheduler vmCPUScheduler,
                   final VmSchedulerWithIndependentPes<HddPe> vmHDDScheduler) {
        super(Id.pollId(HddHost.class), ramProvisioner, bwProvisioner, storage, peList, vmCPUScheduler);
        this.hddIOScheduler = vmHDDScheduler;
        this.hddList = hddList;
        setFailed(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cloudbus.cloudsim.Host#updateVmsProcessing(double)
     */
    @Override
    public double updateCloudletsProcessing(final double currentTime) {
        double smallerTime = Double.MAX_VALUE;

        for (HddVm vm : this.getGuestList()) {
            List<Double> mips = getGuestScheduler().getAllocatedMipsForGuest(vm);
            List<Double> iops = getHddIOScheduler().getAllocatedMipsForGuest(vm);
            double time = vm.updateVmProcessing(currentTime, mips, iops);

            if (time > 0.0 && time < smallerTime) {
                smallerTime = time;
            }
        }

        return smallerTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cloudbus.cloudsim.Host#vmCreate(org.cloudbus.cloudsim.Vm)
     */
    @Override
    public boolean guestCreate(final GuestEntity guest) {
        boolean allocationOfHDD = false;
        HostEntity prevHost = guest.getHost();
        guest.setHost(this);

        boolean allocatednOfCPUFlag = super.guestCreate(guest);
        HddVm hddVm = (HddVm) guest;
        allocationOfHDD = allocatednOfCPUFlag
                && getHddIOScheduler().allocatePesForGuest(hddVm, hddVm.getCurrentRequestedIOMips());

        if (allocatednOfCPUFlag && !allocationOfHDD) {
            getGuestRamProvisioner().deallocateRamForGuest(hddVm);
            getGuestBwProvisioner().deallocateBwForGuest(hddVm);
            getGuestScheduler().deallocatePesForGuest(hddVm);
            getHddIOScheduler().deallocatePesForGuest(hddVm);
            hddVm.setHost(prevHost);
        }

        return allocationOfHDD;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cloudbus.cloudsim.Host#getVmList()
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List<HddVm> getGuestList() {
        return super.getGuestList();
    }

    /**
     * Returns the numbder of unused/free harddrives.
     * 
     * @return the numbder of unused/free harddrives.
     */
    public int getNumberOfFreeHdds() {
        return PeList.getNumberOfFreePes(getHddList());
    }

    /**
     * Returns the total MIPS.
     * 
     * @return the total MIPS.
     */
    public int getTotalIOMips() {
        return PeList.getTotalMips(getHddList());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cloudbus.cloudsim.Host#setFailed(boolean)
     */
    @Override
    public boolean setFailed(final boolean failed) {
        if (getHddList() != null) {
            PeList.setStatusFailed(getHddList(), failed);
        }
        return super.setFailed(failed);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cloudbus.cloudsim.Host#setFailed(java.lang.String, boolean)
     */
    @Override
    public boolean setFailed(final String resName, final boolean failed) {
        PeList.setStatusFailed(getHddList(), resName, getId(), failed);
        return super.setFailed(resName, failed);
    }

    /**
     * Sets the status of the harddisk with the specified id.
     * 
     * @param peId
     *            - the id of the harddisk.
     * @param status
     *            - the new status.
     * @return if the status was set correctly0.
     */
    public boolean setHddStatus(final int peId, final int status) {
        return PeList.setPeStatus(getHddList(), peId, status);
    }

    /**
     * Returns the list of all hard disks of this host.
     * 
     * @return the list of all hard disks of this host.
     */
    public List<? extends HddPe> getHddList() {
        return hddList;
    }

    /**
     * Gets the hdds number.
     * 
     * @return the hdds number
     */
    public int getNumberOfHdds() {
        return getHddList().size();
    }

    /**
     * Returns the scheduler, that manages the distribution of the I/O
     * operations among VMs.
     * 
     * @return the scheduler, that manages the distribution of the I/O
     *         operations among VMs.
     */
    public VmSchedulerWithIndependentPes<HddPe> getHddIOScheduler() {
        return hddIOScheduler;
    }

}
