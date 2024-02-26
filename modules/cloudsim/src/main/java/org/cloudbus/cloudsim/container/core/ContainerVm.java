package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerBwProvisioner;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPe;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerRamProvisioner;
import org.cloudbus.cloudsim.container.lists.ContainerPeList;
import org.cloudbus.cloudsim.container.schedulers.ContainerScheduler;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.VmStateHistoryEntry;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.ArrayList;
import java.util.LinkedList;
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
     * The number of PEs.
     */
    private int numberOfPes;

    /**
     * The ram.
     */
    private int ram;

    /**
     * The bw.
     */
    private long bw;


    /**
     * The vmm.
     */
    private String vmm;

    /**
     * The Cloudlet scheduler.
     */
    private ContainerScheduler containerScheduler;

    /**
     * The host.
     */
    private ContainerHost host;

    /**
     * In migration flag.
     */
    private boolean inMigration;
 /**
     * In waiting flag. shows that vm is waiting for containers to come.
     */
    private boolean inWaiting;

    /**
     * The current allocated size.
     */
    private long currentAllocatedSize;

    /**
     * The current allocated ram.
     */
    private int currentAllocatedRam;

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
    private final List<VmStateHistoryEntry> stateHistory = new LinkedList<>();


    /**
     * The id.
     */
    private int id;

    /**
     * The storage.
     */
    private long storage;

    /**
     * The ram provisioner.
     */
    private ContainerRamProvisioner containerRamProvisioner;

    /**
     * The bw provisioner.
     */
    private ContainerBwProvisioner containerBwProvisioner;

    /**
     * The vm list.
     */
    private final List<? extends Container> containerList = new ArrayList<>();

    /**
     * The pe list.
     */
    private List<? extends ContainerPe> peList;

    /**
     * Tells whether this machine is working properly or has failed.
     */
    private boolean failed;

    /**
     * The vms migrating in.
     */
    private final List<Container> containersMigratingIn = new ArrayList<>();

    /**
     * The datacenter where the host is placed.
     */
    private ContainerDatacenter datacenter;


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
            ContainerScheduler containerScheduler,
            ContainerRamProvisioner containerRamProvisioner,
            ContainerBwProvisioner containerBwProvisioner,
            List<? extends ContainerPe> peList
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
    public double updateVmProcessing(double currentTime, List<Double> mipsShare) {
//        Log.printLine("Vm: update Vms Processing at " + currentTime);
        if (mipsShare != null && !getContainerList().isEmpty()) {
            double smallerTime = Double.MAX_VALUE;
//            Log.printLine("ContainerVm: update Vms Processing");
//            Log.printLine("The VM list size is:...." + getContainerList().size());

            for (Container container : getContainerList()) {
                double time = container.updateContainerProcessing(currentTime, getContainerScheduler().getAllocatedMipsForContainer(container));
                if (time > 0.0 && time < smallerTime) {
                    smallerTime = time;
                }
            }
//            Log.printLine("ContainerVm: The Smaller time is:......" + smallerTime);

            return smallerTime;
        }
//        if (mipsShare != null) {
//            return getContainerScheduler().updateVmProcessing(currentTime, mipsShare);
//        }
        return 0.0;
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
            for (Container contianer : getContainerList()) {

                List<Double> containerCurrentRequestedMips = contianer.getCurrentRequestedMips();
                requestedMipsTemp += containerCurrentRequestedMips.get(0) * containerCurrentRequestedMips.size();
//                Log.formatLine(
//                        " [Container #%d] utilization is %.2f",
//                        contianer.getId() ,
//                        contianer.getCurrentRequestedMips().get(0) * contianer.getCurrentRequestedMips().size() );

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

            for (Container container : getContainerList()) {
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

            for (Container container : getContainerList()) {
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

        for (Container container : getContainerList()) {
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
    protected void setContainerScheduler(ContainerScheduler containerScheduler) {
        this.containerScheduler = containerScheduler;
    }

    /**
     * Adds the migrating in vm.
     *
     * @param container the vm
     */
    public void addMigratingInContainer(Container container) {
//        Log.printLine("ContainerVm: addMigratingInContainer:......");
        container.setInMigration(true);

        if (!getContainersMigratingIn().contains(container)) {
            if (getSize() < container.getSize()) {
                Log.printConcatLine("[ContainerScheduler.addMigratingInContainer] Allocation of VM #", container.getId(), " to Host #",
                        getId(), " failed by storage");
                System.exit(0);
            }

            if (!getContainerRamProvisioner().allocateRamForContainer(container, container.getCurrentRequestedRam())) {
                Log.printConcatLine("[ContainerScheduler.addMigratingInContainer] Allocation of VM #", container.getId(), " to Host #",
                        getId(), " failed by RAM");
                System.exit(0);
            }

            if (!getContainerBwProvisioner().allocateBwForContainer(container, container.getCurrentRequestedBw())) {
                Log.printLine("[ContainerScheduler.addMigratingInContainer] Allocation of VM #" + container.getId() + " to Host #"
                        + getId() + " failed by BW");
                System.exit(0);
            }

            getContainerScheduler().getContainersMigratingIn().add(container.getUid());
            if (!getContainerScheduler().allocatePesForContainer(container, container.getCurrentRequestedMips())) {
                Log.printLine(String.format("[ContainerScheduler.addMigratingInContainer] Allocation of VM #%d to Host #%d failed by MIPS", container.getId(), getId()));
                System.exit(0);
            }

            setSize(getSize() - container.getSize());

            getContainersMigratingIn().add(container);
            getContainerList().add(container);
            updateContainersProcessing(CloudSim.clock());
            container.getVm().updateContainersProcessing(CloudSim.clock());
        }
    }

    public double updateContainersProcessing(double currentTime) {
        double smallerTime = Double.MAX_VALUE;
//        Log.printLine("ContainerVm: update Vms Processing");
//        Log.printLine("The VM list size is:...." + getContainerList().size());

        for (Container container : getContainerList()) {
            double time = container.updateContainerProcessing(currentTime, getContainerScheduler().getAllocatedMipsForContainer(container));
            if (time > 0.0 && time < smallerTime) {
                smallerTime = time;
            }
            double totalRequestedMips = container.getCurrentRequestedTotalMips();
            double totalAllocatedMips = getContainerScheduler().getTotalAllocatedMipsForContainer(container);
            container.addStateHistoryEntry(
                    currentTime,
                    totalAllocatedMips,
                    totalRequestedMips,
                    (container.isInMigration() && !getContainersMigratingIn().contains(container)));
        }
        //Log.printLine("Vm: The Smaller time is:......" + smallerTime);

        return smallerTime;
    }

    /**
     * Removes the migrating in vm.
     *
     * @param container the container
     */
    public void removeMigratingInContainer(Container container) {
        containerDeallocate(container);
        getContainersMigratingIn().remove(container);
        getContainerList().remove(container);
        Log.printLine("ContainerVm# "+getId()+"removeMigratingInContainer:......" + container.getId() + "   Is deleted from the list");
        getContainerScheduler().getContainersMigratingIn().remove(container.getUid());
        container.setInMigration(false);
    }

    /**
     * Reallocate migrating in containers.
     */
    public void reallocateMigratingInContainers() {
//        Log.printLine("ContainerVm: re alocating MigratingInContainer:......");
        for (Container container : getContainersMigratingIn()) {
            if (!getContainerList().contains(container)) {
                getContainerList().add(container);
            }
            if (!getContainerScheduler().getContainersMigratingIn().contains(container.getUid())) {
                getContainerScheduler().getContainersMigratingIn().add(container.getUid());
            }
            getContainerRamProvisioner().allocateRamForContainer(container, container.getCurrentRequestedRam());
            getContainerBwProvisioner().allocateBwForContainer(container, container.getCurrentRequestedBw());
            getContainerScheduler().allocatePesForContainer(container, container.getCurrentRequestedMips());
            setSize(getSize() - container.getSize());
        }
    }

    /**
     * Checks if is suitable for container.
     *
     * @param container the container
     * @return true, if is suitable for container
     */
    public boolean isSuitableForContainer(Container container) {

        return (getContainerScheduler().getPeCapacity() >= container.getCurrentRequestedMaxMips()&& getContainerScheduler().getAvailableMips() >= container.getWorkloadTotalMips()
                && getContainerRamProvisioner().isSuitableForContainer(container, container.getCurrentRequestedRam()) && getContainerBwProvisioner()
                .isSuitableForContainer(container, container.getCurrentRequestedBw()));
    }

       /**
     * Destroys a container running in the VM.
        *
        * @param container the container
     * @pre $none
     * @post $none
     */
    public void containerDestroy(Container container) {
        //Log.printLine("Vm:  Destroy Container:.... " + container.getId());
        if (container != null) {
            containerDeallocate(container);
//            Log.printConcatLine("The Container To remove is :   ", container.getId(), "Size before removing is ", getContainerList().size(), "  vm ID is: ", getId());
            getContainerList().remove(container);
            Log.printLine("ContainerVm# "+getId()+" containerDestroy:......" + container.getId() + "Is deleted from the list");

//            Log.printConcatLine("Size after removing", getContainerList().size());
            while(getContainerList().contains(container)){
                Log.printConcatLine("The container", container.getId(), " is still here");
//                getContainerList().remove(container);
            }
            container.setVm(null);
        }
    }

    /**
     * Destroys all containers running in the VM.
     *
     * @pre $none
     * @post $none
     */
    public void containerDestroyAll() {
//        Log.printLine("ContainerVm: Destroy all Containers");
        containerDeallocateAll();
        for (Container container : getContainerList()) {
            container.setVm(null);
            setSize(getSize() + container.getSize());
        }
//        Log.printLine("ContainerVm# "+getId()+" : containerDestroyAll:...... the whole list is cleared ");

        getContainerList().clear();

    }

    /**
     * Deallocate all VMList for the container.
     *
     * @param container the container
     */
    protected void containerDeallocate(Container container) {
//        Log.printLine("ContainerVm: Deallocated the VM:......" + vm.getId());
        getContainerRamProvisioner().deallocateRamForContainer(container);
        getContainerBwProvisioner().deallocateBwForContainer(container);
        getContainerScheduler().deallocatePesForContainer(container);
        setSize(getSize() + container.getSize());
    }

    /**
     * Deallocate all vmList for the container.
     */
    protected void containerDeallocateAll() {
//        Log.printLine("ContainerVm: Deallocate all the Vms......");
        getContainerRamProvisioner().deallocateRamForAllContainers();
        getContainerBwProvisioner().deallocateBwForAllContainers();
        getContainerScheduler().deallocatePesForAllContainers();
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
    public Container getContainer(int containerId, int userId) {
        for (Container container : getContainerList()) {
            if (container.getId() == containerId && container.getUserId() == userId) {
                return container;
            }
        }
        return null;
    }

    /**
     * Gets the free pes number.
     *
     * @return the free pes number
     */
    public int getNumberOfFreePes() {
//        Log.printLine("ContainerVm: get the free Pes......" + ContainerPeList.getNumberOfFreePes(getPeList()));
        return ContainerPeList.getNumberOfFreePes(getPeList());
    }

    /**
     * Gets the total mips.
     *
     * @return the total mips
     */
    public int getTotalMips() {
//        Log.printLine("ContainerVm: get the total mips......" + ContainerPeList.getTotalMips(getPeList()));
        return ContainerPeList.getTotalMips(getPeList());
    }

    /**
     * Allocates PEs for a VM.
     *
     * @param container the vm
     * @param mipsShare the mips share
     * @return $true if this policy allows a new VM in the host, $false otherwise
     * @pre $none
     * @post $none
     */
    public boolean allocatePesForContainer(Container container, List<Double> mipsShare) {
        //Log.printLine("ContainerVm: allocate Pes for Container:......" + container.getId());
        return getContainerScheduler().allocatePesForContainer(container, mipsShare);
    }

    /**
     * Releases PEs allocated to a container.
     *
     * @param container the container
     * @pre $none
     * @post $none
     */
    public void deallocatePesForContainer(Container container) {
        //Log.printLine("ContainerVm: deallocate Pes for Container:......" + container.getId());
        getContainerScheduler().deallocatePesForContainer(container);
    }

    /**
     * Returns the MIPS share of each Pe that is allocated to a given container.
     *
     * @param container the container
     * @return an array containing the amount of MIPS of each pe that is available to the container
     * @pre $none
     * @post $none
     */
    public List<Double> getAllocatedMipsForContainer(Container container) {
        return getContainerScheduler().getAllocatedMipsForContainer(container);
    }

    /**
     * Gets the total allocated MIPS for a container over all the PEs.
     *
     * @param container the container
     * @return the allocated mips for container
     */
    public double getTotalAllocatedMipsForContainer(Container container) {
        //Log.printLine("ContainerVm: total allocated Pes for Container:......" + container.getId());
        return getContainerScheduler().getTotalAllocatedMipsForContainer(container);
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
     * Gets the VM scheduler.
     *
     * @return the VM scheduler
     */
    public ContainerScheduler getContainerScheduler() {
        return containerScheduler;
    }


    /**
     * Gets the pe list.
     *
     * @param <T> the generic type
     * @return the pe list
     */
    @SuppressWarnings("unchecked")
    public <T extends ContainerPe> List<T> getPeList() {
        return (List<T>) peList;
    }

    /**
     * Sets the pe list.
     *
     * @param <T>    the generic type
     * @param peList the new pe list
     */
    protected <T extends ContainerPe> void setPeList(List<T> peList) {
        this.peList = peList;
    }

    /**
     * Gets the container list.
     *
     * @param <T> the generic type
     * @return the container list
     */
    @SuppressWarnings("unchecked")
    public <T extends Container> List<T> getContainerList() {
        return (List<T>) containerList;
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
        ContainerPeList.setStatusFailed(getPeList(), resName, getId(), failed);
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
        ContainerPeList.setStatusFailed(getPeList(), failed);
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
        return ContainerPeList.setPeStatus(getPeList(), peId, status);
    }

    /**
     * Gets the containers migrating in.
     *
     * @return the containers migrating in
     */
    public List<Container> getContainersMigratingIn() {
        return containersMigratingIn;
    }

    public ContainerRamProvisioner getContainerRamProvisioner() {
        return containerRamProvisioner;
    }

    public void setContainerRamProvisioner(ContainerRamProvisioner containerRamProvisioner) {
        this.containerRamProvisioner = containerRamProvisioner;
    }


    public ContainerBwProvisioner getContainerBwProvisioner() {
        return containerBwProvisioner;
    }

    public void setContainerBwProvisioner(ContainerBwProvisioner containerBwProvisioner) {
        this.containerBwProvisioner = containerBwProvisioner;
    }

    /**
     * Allocates PEs and memory to a new container in the VM.
     *
     * @param container container being started
     * @return $true if the container could be started in the VM; $false otherwise
     * @pre $none
     * @post $none
     */
    public boolean containerCreate(Container container) {
//        Log.printLine("Host: Create VM???......" + container.getId());
        if (getSize() < container.getSize()) {
            Log.printConcatLine("[ContainerScheduler.ContainerCreate] Allocation of Container #", container.getId(), " to VM #", getId(),
                    " failed by storage");
            return false;
        }

        if (!getContainerRamProvisioner().allocateRamForContainer(container, container.getCurrentRequestedRam())) {
            Log.printConcatLine("[ContainerScheduler.ContainerCreate] Allocation of Container #", container.getId(), " to VM #", getId(),
                    " failed by RAM");
            return false;
        }

        if (!getContainerBwProvisioner().allocateBwForContainer(container, container.getCurrentRequestedBw())) {
            Log.printConcatLine("[ContainerScheduler.ContainerCreate] Allocation of Container #", container.getId(), " to VM #", getId(),
                    " failed by BW");
            getContainerRamProvisioner().deallocateRamForContainer(container);
            return false;
        }

        if (!getContainerScheduler().allocatePesForContainer(container, container.getCurrentRequestedMips())) {
            Log.printConcatLine("[ContainerScheduler.ContainerCreate] Allocation of Container #", container.getId(), " to VM #", getId(),
                    " failed by MIPS");
            getContainerRamProvisioner().deallocateRamForContainer(container);
            getContainerBwProvisioner().deallocateBwForContainer(container);
            return false;
        }

        setSize(getSize() - container.getSize());
        getContainerList().add(container);
        container.setVm(this);
        return true;
    }

    public int getNumberOfContainers() {
        int c =0;
        for(Container container:getContainerList()){
            if(!getContainersMigratingIn().contains(container)){
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



