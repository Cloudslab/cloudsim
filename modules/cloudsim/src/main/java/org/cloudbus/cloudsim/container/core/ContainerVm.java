package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;
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

        setContainerRamProvisioner(containerRamProvisioner);
        setContainerBwProvisioner(containerBwProvisioner);
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

    public double updateGuestsProcessing(double currentTime) {
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
     * Adds the migrating in vm.
     *
     * @param container the vm
     */
    public void addMigratingInGuest(Container container) {
//        Log.printLine("ContainerVm: addMigratingInContainer:......");
        container.setInMigration(true);

        if (!getGuestsMigratingIn().contains(container)) {
            if (getSize() < container.getSize()) {
                Log.printConcatLine("[GuestScheduler.addMigratingInContainer] Allocation of VM #", container.getId(), " to Host #",
                        getId(), " failed by storage");
                System.exit(0);
            }

            if (!getContainerRamProvisioner().allocateRamForGuest(container, container.getCurrentRequestedRam())) {
                Log.printConcatLine("[GuestScheduler.addMigratingInContainer] Allocation of VM #", container.getId(), " to Host #",
                        getId(), " failed by RAM");
                System.exit(0);
            }

            if (!getContainerBwProvisioner().allocateBwForGuest(container, container.getCurrentRequestedBw())) {
                Log.printLine("[GuestScheduler.addMigratingInContainer] Allocation of VM #" + container.getId() + " to Host #"
                        + getId() + " failed by BW");
                System.exit(0);
            }

            getContainerScheduler().getGuestsMigratingIn().add(container.getUid());
            if (!getContainerScheduler().allocatePesForGuest(container, container.getCurrentRequestedMips())) {
                Log.printLine(String.format("[GuestScheduler.addMigratingInContainer] Allocation of VM #%d to Host #%d failed by MIPS", container.getId(), getId()));
                System.exit(0);
            }

            setSize(getSize() - container.getSize());

            getGuestsMigratingIn().add(container);
            getGuestList().add(container);
            updateGuestsProcessing(CloudSim.clock());
            container.getHost().updateGuestsProcessing(CloudSim.clock());
        }
    }

    /**
     * Removes the migrating in vm.
     *
     * @param guest the container
     */
    public void removeMigratingInGuest(GuestEntity guest) {
        guestDeallocate(guest);
        getGuestsMigratingIn().remove(guest);
        getGuestList().remove(guest);
        Log.printLine("ContainerVm# "+getId()+"removeMigratingInContainer:......" + guest.getId() + "   Is deleted from the list");
        getContainerScheduler().getGuestsMigratingIn().remove(guest.getUid());
        guest.setInMigration(false);
    }

    /**
     * Reallocate migrating in containers.
     */
    public void reallocateMigratingInGuests() {
//        Log.printLine("ContainerVm: re alocating MigratingInContainer:......");
        for (GuestEntity container : getGuestsMigratingIn()) {
            if (!getGuestList().contains(container)) {
                getGuestList().add(container);
            }
            if (!getContainerScheduler().getGuestsMigratingIn().contains(container.getUid())) {
                getContainerScheduler().getGuestsMigratingIn().add(container.getUid());
            }
            getContainerRamProvisioner().allocateRamForGuest(container, container.getCurrentRequestedRam());
            getContainerBwProvisioner().allocateBwForGuest(container, container.getCurrentRequestedBw());
            getContainerScheduler().allocatePesForGuest(container, container.getCurrentRequestedMips());
            setSize(getSize() - container.getSize());
        }
    }

    /**
     * Checks if is suitable for container.
     *
     * @param guest the container
     * @return true, if is suitable for container
     */
    public boolean isSuitableForGuest(GuestEntity guest) {

        return (getContainerScheduler().getPeCapacity() >= guest.getCurrentRequestedMaxMips()&& getContainerScheduler().getAvailableMips() >= guest.getTotalMips()
                && getContainerRamProvisioner().isSuitableForGuest(guest, guest.getCurrentRequestedRam()) && getContainerBwProvisioner()
                .isSuitableForGuest(guest, guest.getCurrentRequestedBw()));
    }

       /**
        * Destroys a container running in the VM.
        *
        * @param guest the container
        * @pre $none
        * @post $none
        */
    public void guestDestroy(GuestEntity guest) {
        //Log.printLine("Vm:  Destroy Container:.... " + container.getId());
        if (guest != null) {
            guestDeallocate(guest);
//            Log.printConcatLine("The Container To remove is :   ", container.getId(), "Size before removing is ", getContainerList().size(), "  vm ID is: ", getId());
            getGuestList().remove(guest);
            Log.printLine("ContainerVm# "+getId()+" containerDestroy:......" + guest.getId() + "Is deleted from the list");

//            Log.printConcatLine("Size after removing", getContainerList().size());
            while(getGuestList().contains(guest)){
                Log.printConcatLine("The container", guest.getId(), " is still here");
//                getContainerList().remove(container);
            }
            guest.setHost(null);
        }
    }

    /**
     * Destroys all containers running in the VM.
     *
     * @pre $none
     * @post $none
     */
    public void guestDestroyAll() {
//        Log.printLine("ContainerVm: Destroy all Containers");
        guestDeallocateAll();
        for (GuestEntity container : getGuestList()) {
            container.setHost(null);
            setSize(getSize() + container.getSize());
        }
//        Log.printLine("ContainerVm# "+getId()+" : containerDestroyAll:...... the whole list is cleared ");

        getGuestList().clear();

    }

    /**
     * Deallocate all VMList for the container.
     *
     * @param guest the container
     */
    protected void guestDeallocate(GuestEntity guest) {
//        Log.printLine("ContainerVm: Deallocated the VM:......" + vm.getId());
        getContainerRamProvisioner().deallocateRamForGuest(guest);
        getContainerBwProvisioner().deallocateBwForGuest(guest);
        getContainerScheduler().deallocatePesForGuest(guest);
        setSize(getSize() + guest.getSize());
    }

    /**
     * Deallocate all vmList for the container.
     */
    protected void guestDeallocateAll() {
//        Log.printLine("ContainerVm: Deallocate all the Vms......");
        getContainerRamProvisioner().deallocateRamForAllGuests();
        getContainerBwProvisioner().deallocateBwForAllGuests();
        getContainerScheduler().deallocatePesForAllGuests();
    }

    /**
     * Returns a container object.
     *
     * @param containerId the container id
     * @param userId      ID of container's owner
     * @return the container object, $null if not found
     * @pre $none
     * @post $none
     */
    public GuestEntity getGuest(int containerId, int userId) {
        for (GuestEntity container : getGuestList()) {
            if (container.getId() == containerId && container.getUserId() == userId) {
                return container;
            }
        }
        return null;
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
     * Allocates PEs for a VM.
     *
     * @param guest     the vm
     * @param mipsShare the mips share
     * @return $true if this policy allows a new VM in the host, $false otherwise
     * @pre $none
     * @post $none
     */
    public boolean allocatePesForGuest(GuestEntity guest, List<Double> mipsShare) {
        //Log.printLine("ContainerVm: allocate Pes for Container:......" + container.getId());
        return getContainerScheduler().allocatePesForGuest(guest, mipsShare);
    }

    /**
     * Releases PEs allocated to a container.
     *
     * @param guest the container
     * @pre $none
     * @post $none
     */
    public void deallocatePesForGuest(GuestEntity guest) {
        //Log.printLine("ContainerVm: deallocate Pes for Container:......" + container.getId());
        getContainerScheduler().deallocatePesForGuest(guest);
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
        //Log.printLine("ContainerVm: Get BW:......" + getContainerBwProvisioner().getBw());
        return getContainerBwProvisioner().getBw();
    }

    /**
     * Gets the machine memory.
     *
     * @return the machine memory
     * @pre $none
     * @post $result > 0
     */
    public int getRam() {
        //Log.printLine("ContainerVm: Get Ram:......" + getContainerRamProvisioner().getRam());

        return getContainerRamProvisioner().getRam();
    }

    /**
     * TODO: Remo Andreoli: I'm not sure if this is alright
     * @return
     */
    @Override
    public long getStorage() {
        return getSize();
    }

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

    public RamProvisioner getContainerRamProvisioner() {
        return containerRamProvisioner;
    }

    public void setContainerRamProvisioner(RamProvisioner containerRamProvisioner) {
        this.containerRamProvisioner = containerRamProvisioner;
    }


    public BwProvisioner getContainerBwProvisioner() {
        return containerBwProvisioner;
    }

    public void setContainerBwProvisioner(BwProvisioner containerBwProvisioner) {
        this.containerBwProvisioner = containerBwProvisioner;
    }

    /**
     * Allocates PEs and memory to a new container in the VM.
     *
     * @param guest container being started
     * @return $true if the container could be started in the VM; $false otherwise
     * @pre $none
     * @post $none
     */
    public boolean guestCreate(GuestEntity guest) {
//        Log.printLine("Host: Create VM???......" + container.getId());
        if (getSize() < guest.getSize()) {
            Log.printConcatLine("[GuestScheduler.ContainerCreate] Allocation of Container #", guest.getId(), " to VM #", getId(),
                    " failed by storage");
            return false;
        }

        if (!getContainerRamProvisioner().allocateRamForGuest(guest, guest.getCurrentRequestedRam())) {
            Log.printConcatLine("[GuestScheduler.ContainerCreate] Allocation of Container #", guest.getId(), " to VM #", getId(),
                    " failed by RAM");
            return false;
        }

        if (!getContainerBwProvisioner().allocateBwForGuest(guest, guest.getCurrentRequestedBw())) {
            Log.printConcatLine("[GuestScheduler.ContainerCreate] Allocation of Container #", guest.getId(), " to VM #", getId(),
                    " failed by BW");
            getContainerRamProvisioner().deallocateRamForGuest(guest);
            return false;
        }

        if (!getContainerScheduler().allocatePesForGuest(guest, guest.getCurrentRequestedMips())) {
            Log.printConcatLine("[GuestScheduler.ContainerCreate] Allocation of Container #", guest.getId(), " to VM #", getId(),
                    " failed by MIPS");
            getContainerRamProvisioner().deallocateRamForGuest(guest);
            getContainerBwProvisioner().deallocateBwForGuest(guest);
            return false;
        }

        setSize(getSize() - guest.getSize());
        getGuestList().add(guest);
        guest.setHost(this);
        return true;
    }

    public int getNumberOfGuests() {
        int c =0;
        for(GuestEntity container: getGuestList()){
            if(!getGuestsMigratingIn().contains(container)){
                c++;
            }
        }
        return c;
    }

    public boolean isInWaiting() {
        return inWaiting;
    }

    public void setInWaiting(boolean inWaiting) {
        this.inWaiting = inWaiting;
    }
}