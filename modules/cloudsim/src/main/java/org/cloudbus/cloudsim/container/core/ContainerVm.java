package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.ArrayList;
import java.util.List;

/**
 * Vm represents a VM: it runs inside a Host, sharing hostList with other VMs. It processes
 * containers. This processing happens according to a policy, defined by the containerscheduler. Each
 * VM has a owner, which can submit containers to the VM to be executed
 *
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Sareh Fotuhi Piraghaj
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 * <p/>
 * Created by sareh on 9/07/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class ContainerVm extends Vm {

    /**
     * The Cloudlet scheduler.
     */
    private VmScheduler containerScheduler;
 /**
     * In waiting flag. shows that vm is waiting for containers to come.
     */
    private boolean inWaiting;

    /**
     * The ram provisioner.
     */
    private RamProvisioner containerRamProvisioner;

    /**
     * The bw provisioner.
     */
    private BwProvisioner containerBwProvisioner;

    /**
     * The vm list.
     */
    private final List<? extends Container> containerList = new ArrayList<>();

    /**
     * The pe list.
     */
    private List<? extends Pe> peList;

    /**
     * Tells whether this machine is working properly or has failed.
     */
    private boolean failed;

    /**
     * The vms migrating in.
     */
    private final List<GuestEntity> containersMigratingIn = new ArrayList<>();

    /**
     * Creates a new VMCharacteristics object.
     * @param id
     * @param userId
     * @param mips
     * @param ram
     * @param bw
     * @param size
     * @param vmm
     * @param containerScheduler
     * @param containerRamProvisioner
     * @param containerBwProvisioner
     * @param peList
     */

    public ContainerVm(
            int id,
            int userId,
            double mips,
            int ram,
            long bw,
            long size,
            String vmm,
            VmScheduler containerScheduler,
            RamProvisioner containerRamProvisioner,
            BwProvisioner containerBwProvisioner,
            List<? extends Pe> peList
    ) {
        super(id, userId, mips, peList.size(), ram, bw, size, vmm, null);
        // TODO: Remo Andreoli -> avoid null here, try to reconcile ContainerScheduler and CloudletScheduler classes
        setPeList(peList);
        setContainerScheduler(containerScheduler);

        setGuestRamProvisioner(containerRamProvisioner);
        setGuestBwProvisioner(containerBwProvisioner);
    }



    /**
     * Updates the processing of containers running on this VM.
     *
     * @param currentTime current simulation time
     * @param mipsShare   array with MIPS share of each Pe available to the scheduler
     * @return time predicted completion time of the earliest finishing cloudlet, or 0 if there is no
     * next events
     * @pre currentTime >= 0
     * @post $none
     */
    @Override
    public double updateCloudletsProcessing(double currentTime, List<Double> mipsShare) {
//        Log.printLine("Vm: update Vms Processing at " + currentTime);
        if (mipsShare != null && !getGuestList().isEmpty()) {
            double smallerTime = Double.MAX_VALUE;
//            Log.printLine("ContainerVm: update Vms Processing");
//            Log.printLine("The VM list size is:...." + getContainerList().size());

            for (GuestEntity container : getGuestList()) {
                double time = container.updateCloudletsProcessing(currentTime, getContainerScheduler().getAllocatedMipsForGuest(container));
                if (time > 0.0 && time < smallerTime) {
                    smallerTime = time;
                }
            }
//            Log.printLine("ContainerVm: The Smaller time is:......" + smallerTime);

            return smallerTime;
        }
//        if (mipsShare != null) {
//            return getGuestScheduler().updateVmProcessing(currentTime, mipsShare);
//        }
        return 0.0;
    }

    public double updateCloudletsProcessing(double currentTime) {
        double smallerTime = Double.MAX_VALUE;
//        Log.printLine("ContainerVm: update Vms Processing");
//        Log.printLine("The VM list size is:...." + getContainerList().size());

        for (GuestEntity container : getGuestList()) {
            double time = container.updateCloudletsProcessing(currentTime, getContainerScheduler().getAllocatedMipsForGuest(container));
            if (time > 0.0 && time < smallerTime) {
                smallerTime = time;
            }
            double totalRequestedMips = container.getCurrentRequestedTotalMips();
            double totalAllocatedMips = getContainerScheduler().getTotalAllocatedMipsForGuest(container);
            container.addStateHistoryEntry(
                    currentTime,
                    totalAllocatedMips,
                    totalRequestedMips,
                    (container.isInMigration() && !getGuestsMigratingIn().contains(container)));
        }
        //Log.printLine("Vm: The Smaller time is:......" + smallerTime);

        return smallerTime;
    }

    /**
     * Gets the current requested mips.
     *
     * @return the current requested mips
     */
    @Override
    public List<Double> getCurrentRequestedMips() {

        double requestedMipsTemp = 0;


        if (isBeingInstantiated()) {

            requestedMipsTemp = getMips();
        } else {
            for (GuestEntity container : getGuestList()) {

                List<Double> containerCurrentRequestedMips = container.getCurrentRequestedMips();
                requestedMipsTemp += containerCurrentRequestedMips.get(0) * containerCurrentRequestedMips.size();
//                Log.formatLine(
//                        " [Container #%d] utilization is %.2f",
//                        container.getId() ,
//                        container.getCurrentRequestedMips().get(0) * container.getCurrentRequestedMips().size() );

            }
//            Log.formatLine("Total mips usage is %.2f", requestedMipsTemp);
        }

        List<Double> currentRequestedMips = new ArrayList<>(getNumberOfPes());

        for (int i = 0; i < getNumberOfPes(); i++) {
            currentRequestedMips.add(requestedMipsTemp);
        }
        //Log.printLine("Vm: get Current requested Mips" + currentRequestedMips);
        return currentRequestedMips;
    }

    /**
     * Gets the current requested bw.
     *
     * @return the current requested bw
     */
    @Override
    public long getCurrentRequestedBw() {

        if (isBeingInstantiated()) {
            return getBw();
        } else {

            long requestedBwTemp = 0;

            for (GuestEntity container : getGuestList()) {
                requestedBwTemp += container.getCurrentRequestedBw();

            }

            //Log.printLine("Vm: get Current requested Mips" + requestedBwTemp);
            return requestedBwTemp;


        }
    }

    /**
     * Gets the current requested ram.
     *
     * @return the current requested ram
     */
    @Override
    public int getCurrentRequestedRam() {
        if (isBeingInstantiated()) {
            return getRam();
        } else {

            int requestedRamTemp = 0;

            for (GuestEntity container : getGuestList()) {
                requestedRamTemp += container.getCurrentRequestedRam();

            }

            //Log.printLine("Vm: get Current requested Mips" + requestedRamTemp);
            return requestedRamTemp;

        }
    }

    /**
     * Get utilization created by all clouddlets running on this Container.
     *
     * @param time the time
     * @return total utilization
     */
    @Override
    public double getTotalUtilizationOfCpu(double time) {
        float TotalUtilizationOfCpu = 0;

        for (GuestEntity container : getGuestList()) {
            TotalUtilizationOfCpu += container.getTotalUtilizationOfCpu(time);
        }

        //Log.printLine("Vm: get Current requested Mips" + TotalUtilizationOfCpu);
        return TotalUtilizationOfCpu;
    }

    /**
     * Sets the vm scheduler.
     *
     * @param containerScheduler the new vm scheduler
     */
    public void setContainerScheduler(VmScheduler containerScheduler) {
        this.containerScheduler = containerScheduler;
    }

    /**
     * Checks if is suitable for container.
     *
     * @param guest the container
     * @return true, if is suitable for container
     */
    public boolean isSuitableForGuest(GuestEntity guest) {

        return (getContainerScheduler().getPeCapacity() >= guest.getCurrentRequestedMaxMips()&& getContainerScheduler().getAvailableMips() >= guest.getTotalMips()
                && getGuestRamProvisioner().isSuitableForGuest(guest, guest.getCurrentRequestedRam()) && getGuestBwProvisioner()
                .isSuitableForGuest(guest, guest.getCurrentRequestedBw()));
    }

    /**
     * Gets the pes number.
     *
     * @return the pes number
     */
    public int getNumberOfPes() {
        return getPeList().size();
    }

    /**
     * Gets the free pes number.
     *
     * @return the free pes number
     */
    public int getNumberOfFreePes() {
//        Log.printLine("ContainerVm: get the free Pes......" + PeList.getNumberOfFreePes(getPeList()));
        return PeList.getNumberOfFreePes(getPeList());
    }

    /**
     * Gets the total mips.
     *
     * @return the total mips
     */
    public double getTotalMips() {
//        Log.printLine("ContainerVm: get the total mips......" + PeList.getTotalMips(getPeList()));
        return PeList.getTotalMips(getPeList());
    }

    @Override
    public Datacenter getDatacenter() {
        return getHost().getDatacenter();
    }

    @Override
    public VmScheduler getGuestScheduler() {
        return containerScheduler;
    }

    /**
     * Returns the MIPS share of each Pe that is allocated to a given container.
     *
     * @param guest the container
     * @return an array containing the amount of MIPS of each pe that is available to the container
     * @pre $none
     * @post $none
     */
    public List<Double> getAllocatedMipsForGuest(GuestEntity guest) {
        return getContainerScheduler().getAllocatedMipsForGuest(guest);
    }

    /**
     * Gets the total allocated MIPS for a container over all the PEs.
     *
     * @param guest the container
     * @return the allocated mips for container
     */
    public double getTotalAllocatedMipsForGuest(GuestEntity guest) {
        //Log.printLine("ContainerVm: total allocated Pes for Container:......" + container.getId());
        return getContainerScheduler().getTotalAllocatedMipsForGuest(guest);
    }

    /**
     * Returns maximum available MIPS among all the PEs.
     *
     * @return max mips
     */
    public double getMaxAvailableMips() {
        //Log.printLine("ContainerVm: Maximum Available Pes:......");
        return getContainerScheduler().getMaxAvailableMips();
    }

    /**
     * Gets the free mips.
     *
     * @return the free mips
     */
    public double getAvailableMips() {
        //Log.printLine("ContainerVm: Get available Mips");
        return getContainerScheduler().getAvailableMips();
    }

    /**
     * Gets the machine bw.
     *
     * @return the machine bw
     * @pre $none
     * @post $result > 0
     */
    public long getBw() {
        return getGuestBwProvisioner().getBw();
    }

    /**
     * Gets the machine memory.
     *
     * @return the machine memory
     * @pre $none
     * @post $result > 0
     */
    public int getRam() {
        return getGuestRamProvisioner().getRam();
    }

    /**
     * TODO: Remo Andreoli: I'm not sure if this is alright, but I need it for compatibility reasons with default guestCreate()
     */
    public long getStorage() {
        return getSize();
    }
    public void setStorage(long storage) { setSize(storage); }
    /**
     * Gets the VM scheduler.
     *
     * @return the VM scheduler
     */
    public VmScheduler getContainerScheduler() {
        return containerScheduler;
    }


    /**
     * Checks if is failed.
     *
     * @return true, if is failed
     */
    public boolean isFailed() {
        return failed;
    }

    /**
     * Sets the PEs of this machine to a FAILED status. NOTE: <tt>resName</tt> is used for debugging
     * purposes, which is <b>ON</b> by default. Use {@link #setFailed(boolean)} if you do not want
     * this information.
     *
     * @param resName the name of the resource
     * @param failed  the failed
     * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
     */
    public boolean setFailed(String resName, boolean failed) {
        // all the PEs are failed (or recovered, depending on fail)
        this.failed = failed;
        PeList.setStatusFailed(getPeList(), resName, getId(), failed);
        return true;
    }

    /**
     * Sets the PEs of this machine to a FAILED status.
     *
     * @param failed the failed
     * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
     */
    public boolean setFailed(boolean failed) {
        // all the PEs are failed (or recovered, depending on fail)
        this.failed = failed;
        PeList.setStatusFailed(getPeList(), failed);
        return true;
    }

    /**
     * Sets the particular Pe status on this Machine.
     *
     * @param peId   the pe id
     * @param status Pe status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
     * @return <tt>true</tt> if the Pe status has changed, <tt>false</tt> otherwise (Pe id might not
     * be exist)
     * @pre peID >= 0
     * @post $none
     */
    public boolean setPeStatus(int peId, int status) {
        return PeList.setPeStatus(getPeList(), peId, status);
    }

    /**
     * Gets the containers migrating in.
     *
     * @return the containers migrating in
     */
    public List<GuestEntity> getGuestsMigratingIn() {
        return containersMigratingIn;
    }

    public RamProvisioner getGuestRamProvisioner() {
        return containerRamProvisioner;
    }

    public void setGuestRamProvisioner(RamProvisioner containerRamProvisioner) {
        this.containerRamProvisioner = containerRamProvisioner;
    }


    public BwProvisioner getGuestBwProvisioner() {
        return containerBwProvisioner;
    }

    public void setGuestBwProvisioner(BwProvisioner containerBwProvisioner) {
        this.containerBwProvisioner = containerBwProvisioner;
    }

    public boolean isInWaiting() {
        return inWaiting;
    }

    public void setInWaiting(boolean inWaiting) {
        this.inWaiting = inWaiting;
    }
}