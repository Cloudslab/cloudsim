package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmBwProvisioner;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmRamProvisioner;
import org.cloudbus.cloudsim.container.schedulers.ContainerVmScheduler;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPe;
import org.cloudbus.cloudsim.container.lists.ContainerVmPeList;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sareh on 10/07/15.
 */
public class ContainerHost {


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
    private ContainerVmRamProvisioner containerVmRamProvisioner;

    /**
     * The bw provisioner.
     */
    private ContainerVmBwProvisioner containerVmBwProvisioner;

    /**
     * The allocation policy.
     */
    private ContainerVmScheduler containerVmScheduler;

    /**
     * The vm list.
     */
    private final List<? extends ContainerVm> vmList = new ArrayList<>();
    /**
     * The vm list.
     */

    /**
     * The pe list.
     */
    private List<? extends ContainerVmPe> peList;

    /**
     * Tells whether this machine is working properly or has failed.
     */
    private boolean failed;

    /**
     * The vms migrating in.
     */
    private final List<ContainerVm> vmsMigratingIn = new ArrayList<>();
    /**
     * The datacenter where the host is placed.
     */
    private ContainerDatacenter datacenter;

    /**
     * Instantiates a new host.
     *
     * @param id             the id
     * @param containerVmRamProvisioner the ram provisioner
     * @param containerVmBwProvisioner  the bw provisioner
     * @param storage        the storage
     * @param peList         the pe list
     * @param containerVmScheduler    the vm scheduler
     */
    public ContainerHost(
            int id,
            ContainerVmRamProvisioner containerVmRamProvisioner,
            ContainerVmBwProvisioner containerVmBwProvisioner,
            long storage,
            List<? extends ContainerVmPe> peList,
            ContainerVmScheduler containerVmScheduler) {
        setId(id);
        setContainerVmRamProvisioner(containerVmRamProvisioner);
        setContainerVmBwProvisioner(containerVmBwProvisioner);
        setStorage(storage);
        setContainerVmScheduler(containerVmScheduler);
        setPeList(peList);
        setFailed(false);

    }

    /**
     * Requests updating of processing of cloudlets in the VMs running in this host.
     *
     * @param currentTime the current time
     * @return expected time of completion of the next cloudlet in all VMs in this host.
     * Double.MAX_VALUE if there is no future events expected in this host
     * @pre currentTime >= 0.0
     * @post $none
     */
    public double updateContainerVmsProcessing(double currentTime) {
        double smallerTime = Double.MAX_VALUE;

        for (ContainerVm containerVm : getVmList()) {
            double time = containerVm.updateVmProcessing(currentTime, getContainerVmScheduler().getAllocatedMipsForContainerVm(containerVm));
            if (time > 0.0 && time < smallerTime) {
                smallerTime = time;
            }
        }

        return smallerTime;
    }

    /**
     * Adds the migrating in vm.
     *
     * @param containerVm the vm
     */
    public void addMigratingInContainerVm(ContainerVm containerVm) {
        //Log.printLine("Host: addMigratingInContainerVm:......");
        containerVm.setInMigration(true);

        if (!getVmsMigratingIn().contains(containerVm)) {
            if (getStorage() < containerVm.getSize()) {
                Log.printConcatLine("[VmScheduler.addMigratingInContainerVm] Allocation of VM #", containerVm.getId(), " to Host #",
                        getId(), " failed by storage");
                System.exit(0);
            }

            if (!getContainerVmRamProvisioner().allocateRamForContainerVm(containerVm, containerVm.getCurrentRequestedRam())) {
                Log.printConcatLine("[VmScheduler.addMigratingInContainerVm] Allocation of VM #", containerVm.getId(), " to Host #",
                        getId(), " failed by RAM");
                System.exit(0);
            }

            if (!getContainerVmBwProvisioner().allocateBwForContainerVm(containerVm, containerVm.getCurrentRequestedBw())) {
                Log.printConcatLine("[VmScheduler.addMigratingInContainerVm] Allocation of VM #", containerVm.getId(), " to Host #",
                        getId(), " failed by BW");
                System.exit(0);
            }

            getContainerVmScheduler().getVmsMigratingIn().add(containerVm.getUid());
            if (!getContainerVmScheduler().allocatePesForVm(containerVm, containerVm.getCurrentRequestedMips())) {
                Log.printConcatLine("[VmScheduler.addMigratingInContainerVm] Allocation of VM #", containerVm.getId(), " to Host #",
                        getId(), " failed by MIPS");
                System.exit(0);
            }

            setStorage(getStorage() - containerVm.getSize());

            getVmsMigratingIn().add(containerVm);
            getVmList().add(containerVm);
            updateContainerVmsProcessing(CloudSim.clock());
            containerVm.getHost().updateContainerVmsProcessing(CloudSim.clock());
        }
    }

        /**
         * Removes the migrating in vm.
         *
         * @param vm the vm
         */
        public void removeMigratingInContainerVm(ContainerVm vm) {
            containerVmDeallocate(vm);
            getVmsMigratingIn().remove(vm);
            getVmList().remove(vm);
            getContainerVmScheduler().getVmsMigratingIn().remove(vm.getUid());
            vm.setInMigration(false);
        }

    /**
     * Reallocate migrating in vms.
     */
    public void reallocateMigratingInContainerVms() {
        for (ContainerVm containerVm : getVmsMigratingIn()) {
            if (!getVmList().contains(containerVm)) {
                getVmList().add(containerVm);
            }
            if (!getContainerVmScheduler().getVmsMigratingIn().contains(containerVm.getUid())) {
                getContainerVmScheduler().getVmsMigratingIn().add(containerVm.getUid());
            }
            getContainerVmRamProvisioner().allocateRamForContainerVm(containerVm, containerVm.getCurrentRequestedRam());
            getContainerVmBwProvisioner().allocateBwForContainerVm(containerVm, containerVm.getCurrentRequestedBw());
            getContainerVmScheduler().allocatePesForVm(containerVm, containerVm.getCurrentRequestedMips());
            setStorage(getStorage() - containerVm.getSize());
        }
    }

    /**
     * Checks if is suitable for vm.
     *
     * @param vm the vm
     * @return true, if is suitable for vm
     */
    public boolean isSuitableForContainerVm(ContainerVm vm) {
        //Log.printLine("Host: Is suitable for VM???......");
        return (getContainerVmScheduler().getPeCapacity() >= vm.getCurrentRequestedMaxMips()
                && getContainerVmScheduler().getAvailableMips() >= vm.getCurrentRequestedTotalMips()
                && getContainerVmRamProvisioner().isSuitableForContainerVm(vm, vm.getCurrentRequestedRam()) && getContainerVmBwProvisioner()
                .isSuitableForContainerVm(vm, vm.getCurrentRequestedBw()));
    }

    /**
     * Allocates PEs and memory to a new VM in the Host.
     *
     * @param vm Vm being started
     * @return $true if the VM could be started in the host; $false otherwise
     * @pre $none
     * @post $none
     */
    public boolean containerVmCreate(ContainerVm vm) {
        //Log.printLine("Host: Create VM???......" + vm.getId());
        if (getStorage() < vm.getSize()) {
            Log.printConcatLine("[VmScheduler.containerVmCreate] Allocation of VM #", vm.getId(), " to Host #", getId(),
                    " failed by storage");
            return false;
        }

        if (!getContainerVmRamProvisioner().allocateRamForContainerVm(vm, vm.getCurrentRequestedRam())) {
            Log.printConcatLine("[VmScheduler.containerVmCreate] Allocation of VM #", vm.getId(), " to Host #", getId(),
                    " failed by RAM");
            return false;
        }

        if (!getContainerVmBwProvisioner().allocateBwForContainerVm(vm, vm.getCurrentRequestedBw())) {
            Log.printConcatLine("[VmScheduler.containerVmCreate] Allocation of VM #", vm.getId(), " to Host #", getId(),
                    " failed by BW");
            getContainerVmRamProvisioner().deallocateRamForContainerVm(vm);
            return false;
        }

        if (!getContainerVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
            Log.printConcatLine("[VmScheduler.containerVmCreate] Allocation of VM #", vm.getId(), " to Host #", getId(),
                    " failed by MIPS");
            getContainerVmRamProvisioner().deallocateRamForContainerVm(vm);
            getContainerVmBwProvisioner().deallocateBwForContainerVm(vm);
            return false;
        }

        setStorage(getStorage() - vm.getSize());
        getVmList().add(vm);
        vm.setHost(this);
        return true;
    }

    /**
     * Destroys a VM running in the host.
     *
     * @param containerVm the VM
     * @pre $none
     * @post $none
     */
    public void containerVmDestroy(ContainerVm containerVm) {
        //Log.printLine("Host:  Destroy Vm:.... " + containerVm.getId());
        if (containerVm != null) {
            containerVmDeallocate(containerVm);
            getVmList().remove(containerVm);
            containerVm.setHost(null);
        }
    }

    /**
     * Destroys all VMs running in the host.
     *
     * @pre $none
     * @post $none
     */
    public void containerVmDestroyAll() {
        //Log.printLine("Host: Destroy all Vms");
        containerVmDeallocateAll();
        for (ContainerVm containerVm : getVmList()) {
            containerVm.setHost(null);
            setStorage(getStorage() + containerVm.getSize());
        }
        getVmList().clear();
    }

    /**
     * Deallocate all hostList for the VM.
     *
     * @param containerVm the VM
     */
    protected void containerVmDeallocate(ContainerVm containerVm) {
        //Log.printLine("Host: Deallocated the VM:......" + containerVm.getId());
        getContainerVmRamProvisioner().deallocateRamForContainerVm(containerVm);
        getContainerVmBwProvisioner().deallocateBwForContainerVm(containerVm);
        getContainerVmScheduler().deallocatePesForVm(containerVm);
        setStorage(getStorage() + containerVm.getSize());
    }

    /**
     * Deallocate all hostList for the VM.
     */
    protected void containerVmDeallocateAll() {
        //Log.printLine("Host: Deallocate all the Vms......");
        getContainerVmRamProvisioner().deallocateRamForAllContainerVms();
        getContainerVmBwProvisioner().deallocateBwForAllContainerVms();
        getContainerVmScheduler().deallocatePesForAllContainerVms();
    }

    /**
     * Returns a VM object.
     *
     * @param vmId   the vm id
     * @param userId ID of VM's owner
     * @return the virtual machine object, $null if not found
     * @pre $none
     * @post $none
     */
    public ContainerVm getContainerVm(int vmId, int userId) {
        //Log.printLine("Host: get the vm......" + vmId);
        //Log.printLine("Host: the vm list size:......" + getVmList().size());
        for (ContainerVm containerVm : getVmList()) {
            if (containerVm.getId() == vmId && containerVm.getUserId() == userId) {
                return containerVm;
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
        //Log.printLine("Host: get the peList Size......" + getPeList().size());
        return getPeList().size();
    }

    /**
     * Gets the free pes number.
     *
     * @return the free pes number
     */
    public int getNumberOfFreePes() {
        //Log.printLine("Host: get the free Pes......" + ContainerVmPeList.getNumberOfFreePes(getPeList()));
        return ContainerVmPeList.getNumberOfFreePes(getPeList());
    }

    /**
     * Gets the total mips.
     *
     * @return the total mips
     */
    public int getTotalMips() {
        //Log.printLine("Host: get the total mips......" + ContainerVmPeList.getTotalMips(getPeList()));
        return ContainerVmPeList.getTotalMips(getPeList());
    }

    /**
     * Allocates PEs for a VM.
     *
     * @param containerVm        the vm
     * @param mipsShare the mips share
     * @return $true if this policy allows a new VM in the host, $false otherwise
     * @pre $none
     * @post $none
     */
    public boolean allocatePesForContainerVm(ContainerVm containerVm, List<Double> mipsShare) {
        //Log.printLine("Host: allocate Pes for Vm:......" + containerVm.getId());
        return getContainerVmScheduler().allocatePesForVm(containerVm, mipsShare);
    }

    /**
     * Releases PEs allocated to a VM.
     *
     * @param containerVm the vm
     * @pre $none
     * @post $none
     */
    public void deallocatePesForContainerVm(ContainerVm containerVm) {
        //Log.printLine("Host: deallocate Pes for Vm:......" + containerVm.getId());
        getContainerVmScheduler().deallocatePesForVm(containerVm);
    }

    /**
     * Returns the MIPS share of each Pe that is allocated to a given VM.
     *
     * @param containerVm the vm
     * @return an array containing the amount of MIPS of each pe that is available to the VM
     * @pre $none
     * @post $none
     */
    public List<Double> getAllocatedMipsForContainerVm(ContainerVm containerVm) {
        //Log.printLine("Host: get allocated Pes for Vm:......" + containerVm.getId());
        return getContainerVmScheduler().getAllocatedMipsForContainerVm(containerVm);
    }

    /**
     * Gets the total allocated MIPS for a VM over all the PEs.
     *
     * @param containerVm the vm
     * @return the allocated mips for vm
     */
    public double getTotalAllocatedMipsForContainerVm(ContainerVm containerVm) {
        //Log.printLine("Host: total allocated Pes for Vm:......" + containerVm.getId());
        return getContainerVmScheduler().getTotalAllocatedMipsForContainerVm(containerVm);
    }

    /**
     * Returns maximum available MIPS among all the PEs.
     *
     * @return max mips
     */
    public double getMaxAvailableMips() {
        //Log.printLine("Host: Maximum Available Pes:......");
        return getContainerVmScheduler().getMaxAvailableMips();
    }

    /**
     * Gets the free mips.
     *
     * @return the free mips
     */
    public double getAvailableMips() {
        //Log.printLine("Host: Get available Mips");
        return getContainerVmScheduler().getAvailableMips();
    }

    /**
     * Gets the machine bw.
     *
     * @return the machine bw
     * @pre $none
     * @post $result > 0
     */
    public long getBw() {
        //Log.printLine("Host: Get BW:......" + getContainerVmBwProvisioner().getBw());
        return getContainerVmBwProvisioner().getBw();
    }

    /**
     * Gets the machine memory.
     *
     * @return the machine memory
     * @pre $none
     * @post $result > 0
     */
    public float getRam() {
        //Log.printLine("Host: Get Ram:......" + getContainerVmRamProvisioner().getRam());

        return getContainerVmRamProvisioner().getRam();
    }

    /**
     * Gets the machine storage.
     *
     * @return the machine storage
     * @pre $none
     * @post $result >= 0
     */
    public long getStorage() {
        return storage;
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
     * Gets the ram provisioner.
     *
     * @return the ram provisioner
     */
    public ContainerVmRamProvisioner getContainerVmRamProvisioner() {
        return containerVmRamProvisioner;
    }

    /**
     * Sets the ram provisioner.
     *
     * @param containerVmRamProvisioner the new ram provisioner
     */
    protected void setContainerVmRamProvisioner(ContainerVmRamProvisioner containerVmRamProvisioner) {
        this.containerVmRamProvisioner = containerVmRamProvisioner;
    }

    /**
     * Gets the bw provisioner.
     *
     * @return the bw provisioner
     */
    public ContainerVmBwProvisioner getContainerVmBwProvisioner() {
        return containerVmBwProvisioner;
    }

    /**
     * Sets the bw provisioner.
     *
     * @param containerVmBwProvisioner the new bw provisioner
     */
    protected void setContainerVmBwProvisioner(ContainerVmBwProvisioner containerVmBwProvisioner) {
        this.containerVmBwProvisioner = containerVmBwProvisioner;
    }

    /**
     * Gets the VM scheduler.
     *
     * @return the VM scheduler
     */
    public ContainerVmScheduler getContainerVmScheduler() {
        return containerVmScheduler;
    }

    /**
     * Sets the VM scheduler.
     *
     * @param vmScheduler the vm scheduler
     */
    protected void setContainerVmScheduler(ContainerVmScheduler vmScheduler) {
        this.containerVmScheduler = vmScheduler;
    }

    /**
     * Gets the pe list.
     *
     * @param <T> the generic type
     * @return the pe list
     */
    @SuppressWarnings("unchecked")
    public <T extends ContainerVmPe> List<T> getPeList() {
        return (List<T>) peList;
    }

    /**
     * Sets the pe list.
     *
     * @param <T>    the generic type
     * @param containerVmPeList the new pe list
     */
    protected <T extends ContainerVmPe> void setPeList(List<T> containerVmPeList) {
        this.peList = containerVmPeList;
    }

    /**
     * Gets the vm list.
     *
     * @param <T> the generic type
     * @return the vm list
     */
    @SuppressWarnings("unchecked")
    public <T extends ContainerVm> List<T> getVmList() {
        return (List<T>) vmList;
    }

    /**
     * Sets the storage.
     *
     * @param storage the new storage
     */
    protected void setStorage(long storage) {
        this.storage = storage;
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
        ContainerVmPeList.setStatusFailed(getPeList(), resName, getId(), failed);
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
        ContainerVmPeList.setStatusFailed(getPeList(), failed);
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
        return ContainerVmPeList.setPeStatus(getPeList(), peId, status);
    }

    /**
     * Gets the vms migrating in.
     *
     * @return the vms migrating in
     */
    public List<ContainerVm> getVmsMigratingIn() {
        return vmsMigratingIn;
    }

    /**
     * Gets the data center.
     *
     * @return the data center where the host runs
     */
    public ContainerDatacenter getDatacenter() {
        return datacenter;
    }

    /**
     * Sets the data center.
     *
     * @param datacenter the data center from this host
     */
    public void setDatacenter(ContainerDatacenter datacenter) {
        this.datacenter = datacenter;
    }


}


