/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * Host class extends a Machine to include other hostList beside PEs
 * to support simulation of virtualized grids. It executes actions related
 * to management of virtual machines (e.g., creation and destruction). A host has
 * a defined policy for provisioning memory and bw, as well as an allocation policy
 * for Pe's to virtual machines.
 *
 * A host is associated to a datacenter. It can host virtual machines.
 *
 * @author		Rodrigo N. Calheiros
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 1.0
 */
public class Host {

	/** The id. */
	private int id;

	/** The storage. */
	private long storage;

	/** The ram provisioner. */
	private RamProvisioner ramProvisioner;

	/** The bw provisioner. */
	private BwProvisioner bwProvisioner;

	/** The allocation policy. */
	private VmScheduler vmScheduler;

	/** The vm list. */
	private List<? extends Vm> vmList;

	/** The pe list. */
	private List<? extends Pe> peList;

    /** Tells whether this machine is working properly or has failed. */
    private boolean failed;

	/** The vms migrating in. */
	private List<Vm> vmsMigratingIn;
	
	/** THe datacenter where the host is placed */
	private Datacenter datacenter;

	/**
	 * Instantiates a new host.
	 *
	 * @param id the id
	 * @param storage the storage
	 * @param ramProvisioner the ram provisioner
	 * @param bwProvisioner the bw provisioner
	 * @param peList the pe list
	 * @param vmScheduler the vm scheduler
	 */
	public Host(int id,
				RamProvisioner ramProvisioner,
				BwProvisioner bwProvisioner,
				long storage,
				List<? extends Pe> peList,
				VmScheduler vmScheduler) {
		setId(id);
		setRamProvisioner(ramProvisioner);
		setBwProvisioner(bwProvisioner);
		setStorage(storage);
		setVmScheduler(vmScheduler);

		setPeList(peList);
		setVmList(new ArrayList<Vm>());
		setFailed(false);
	}

	/**
	 * Requests updating of processing of cloudlets in the VMs running in this host.
	 *
	 * @param currentTime the current time
	 *
	 * @return 		expected time of completion of the next cloudlet in all VMs in this host. Double.MAX_VALUE
	 * if there is no future events expected in this host
	 *
	 * @pre 		currentTime >= 0.0
	 * @post 		$none
	 */
	public double updateVmsProcessing(double currentTime) {
		double smallerTime = Double.MAX_VALUE;

		for (Vm vm : getVmList()) {
//			if (vm.isInMigration()) {
//				continue;
//			}
			double time = vm.updateVmProcessing(currentTime, getVmScheduler().getAllocatedMipsForVm(vm));
			if (time > 0.0 && time < smallerTime) {
				smallerTime = time;
			}
		}

		return smallerTime;
	}

	public void addMigratingInVm(Vm vm) {
		if (!getVmsMigratingIn().contains(vm)) {
			getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam());
			getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw());
			getVmsMigratingIn().add(vm);
		}
	}

	public void removeMigratingInVm(Vm vm) {
		getRamProvisioner().deallocateRamForVm(vm);
		getBwProvisioner().deallocateBwForVm(vm);
		getVmsMigratingIn().remove(vm);
	}

	public void reallocateMigratingVms() {
		for (Vm vm : getVmsMigratingIn()) {
			getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam());
			getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw());
		}
	}

	/**
	 * Checks if is suitable for vm.
	 *
	 * @param vm the vm
	 *
	 * @return true, if is suitable for vm
	 */
	public boolean isSuitableForVm(Vm vm) {
		return (getVmScheduler().getAvailableMips() >= vm.getCurrentRequestedTotalMips() &&
				getRamProvisioner().isSuitableForVm(vm, vm.getCurrentRequestedRam()) &&
				getBwProvisioner().isSuitableForVm(vm, vm.getCurrentRequestedBw()));
	}

	/**
	 * Allocates PEs and memory to a new VM in the Host.
	 *
	 * @param vm Vm being started
	 *
	 * @return $true if the VM could be started in the host; $false otherwise
	 *
	 * @pre $none
	 * @post $none
	 */
	public boolean vmCreate(Vm vm) {
		if (!getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam())) {
			Log.printLine("Allocation of VM #" + vm.getId() + " to Host #" + getId() + " failed by RAM");
			return false;
		}

		if (!getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw())) {
			Log.printLine("Allocation of VM #" + vm.getId() + " to Host #" + getId() + " failed by BW");
			getRamProvisioner().deallocateRamForVm(vm);
			return false;
		}

		if (!getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
			Log.printLine("[VmScheduler.vmCreate] Allocation of VM #" + vm.getId() + " to Host #" + getId() + " failed by MIPS");
			getRamProvisioner().deallocateRamForVm(vm);
			getBwProvisioner().deallocateBwForVm(vm);
			return false;
		}

		getVmList().add(vm);
		vm.setHost(this);
		return true;
	}

	/**
	 * Destroys a VM running in the host.
	 *
	 * @param vm the VM
	 *
	 * @pre $none
	 * @post $none
	 */
	public void vmDestroy(Vm vm) {
		if (vm != null) {
			vmDeallocate(vm);
			getVmList().remove(vm);
			vm.setHost(null);
		}
	}

	/**
	 * Destroys all VMs running in the host.
	 *
	 * @pre $none
	 * @post $none
	 */
	public void vmDestroyAll() {
		vmDeallocateAll();
		for (Vm vm : getVmList()) {
			vm.setHost(null);
		}
		getVmList().clear();
	}

	/**
	 * Deallocate all hostList for the VM.
	 *
	 * @param vm the VM
	 */
	protected void vmDeallocate(Vm vm) {
		getRamProvisioner().deallocateRamForVm(vm);
		getBwProvisioner().deallocateBwForVm(vm);
		getVmScheduler().deallocatePesForVm(vm);
	}

	/**
	 * Deallocate all hostList for the VM.
	 *
	 * @param vm the VM
	 */
	protected void vmDeallocateAll() {
		getRamProvisioner().deallocateRamForAllVms();
		getBwProvisioner().deallocateBwForAllVms();
		getVmScheduler().deallocatePesForAllVms();
	}

	/**
	 * Returns a VM object.
	 *
	 * @param vmId the vm id
	 * @param userId ID of VM's owner
	 *
	 * @return the virtual machine object, $null if not found
	 *
	 * @pre $none
	 * @post $none
	 */
	public Vm getVm(int vmId, int userId){
		for (Vm vm : getVmList()) {
			if (vm.getId() == vmId && vm.getUserId() == userId) {
				return vm;
			}
		}
		return null;
	}

	/**
	 * Gets the pes number.
	 *
	 * @return the pes number
	 */
	public int getPesNumber() {
		return getPeList().size();
	}

	/**
	 * Gets the free pes number.
	 *
	 * @return the free pes number
	 */
	@SuppressWarnings("unchecked")
	public int getFreePesNumber() {
		return PeList.getFreePesNumber((List<Pe>) getPeList());
	}

	/**
	 * Gets the total mips.
	 *
	 * @return the total mips
	 */
	@SuppressWarnings("unchecked")
	public int getTotalMips() {
		return PeList.getTotalMips((List<Pe>) getPeList());
	}

	/**
	 * Allocates PEs for a VM.
	 *
	 * @param vm the vm
	 * @param mipsShare the mips share
	 *
	 * @return $true if this policy allows a new VM in the host, $false otherwise
	 *
	 * @pre $none
	 * @post $none
	 */
	public boolean allocatePesForVm(Vm vm, List<Double> mipsShare) {
		return getVmScheduler().allocatePesForVm(vm, mipsShare);
	}

	/**
	 * Releases PEs allocated to a VM.
	 *
	 * @param vm the vm
	 *
	 * @pre $none
	 * @post $none
	 */
	public void deallocatePesForVm(Vm vm) {
		getVmScheduler().deallocatePesForVm(vm);
	}

	/**
	 * Returns the MIPS share of each Pe that is allocated to a given VM.
	 *
	 * @param vm the vm
	 *
	 * @return an array containing the amount of MIPS of each pe that is available to the VM
	 *
	 * @pre $none
	 * @post $none
	 */
	public List<Double> getAllocatedMipsForVm(Vm vm) {
		return getVmScheduler().getAllocatedMipsForVm(vm);
	}

	/**
	 * Gets the total allocated MIPS for a VM over all the PEs.
	 *
	 * @param vm the vm
	 *
	 * @return the allocated mips for vm
	 */
	public double getTotalAllocatedMipsForVm(Vm vm) {
		return getVmScheduler().getTotalAllocatedMipsForVm(vm);
	}

	/**
	 * Returns maximum available MIPS among all the PEs.
	 *
	 * @return max mips
	 */
	public double getMaxAvailableMips() {
		return getVmScheduler().getMaxAvailableMips();
	}

	/**
	 * Gets the free mips.
	 *
	 * @return the free mips
	 */
	public double getAvailableMips() {
		return getVmScheduler().getAvailableMips();
	}

	/**
	 * Gets the machine bw.
	 *
	 * @return the machine bw
	 *
	 * @pre $none
	 * @post $result > 0
	 */
	public long getBw() {
		return getBwProvisioner().getBw();
	}

	/**
	 * Gets the machine memory.
	 *
	 * @return the machine memory
	 *
	 * @pre $none
	 * @post $result > 0
	 */
	public int getRam() {
		return getRamProvisioner().getRam();
	}

	/**
	 * Gets the machine storage.
	 *
	 * @return the machine storage
	 *
	 * @pre $none
	 * @post $result > 0
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
	public RamProvisioner getRamProvisioner() {
		return ramProvisioner;
	}

	/**
	 * Sets the ram provisioner.
	 *
	 * @param ramProvisioner the new ram provisioner
	 */
	protected void setRamProvisioner(RamProvisioner ramProvisioner) {
		this.ramProvisioner = ramProvisioner;
	}

	/**
	 * Gets the bw provisioner.
	 *
	 * @return the bw provisioner
	 */
	public BwProvisioner getBwProvisioner() {
		return bwProvisioner;
	}

	/**
	 * Sets the bw provisioner.
	 *
	 * @param bwProvisioner the new bw provisioner
	 */
	protected void setBwProvisioner(BwProvisioner bwProvisioner) {
		this.bwProvisioner = bwProvisioner;
	}

	/**
	 * Gets the VM scheduler.
	 *
	 * @return the VM scheduler
	 */
	protected VmScheduler getVmScheduler() {
		return vmScheduler;
	}

	/**
	 * Sets the VM scheduler.
	 *
	 * @param vmScheduler the vm scheduler
	 */
	protected void setVmScheduler(VmScheduler vmScheduler) {
		this.vmScheduler = vmScheduler;
	}

	/**
	 * Gets the pe list.
	 *
	 * @return the pe list
	 */
	public List<? extends Pe> getPeList() {
		return this.peList;
	}

	/**
	 * Sets the pe list.
	 *
	 * @param peList the new pe list
	 */
	protected void setPeList(List<? extends Pe> peList) {
		this.peList = peList;
	}

	/**
	 * Gets the vm list.
	 *
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 *
	 * @param vmList the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
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
     * Sets the PEs of this machine to a FAILED status.
     * NOTE: <tt>resName</tt> is used for debugging purposes,
     * which is <b>ON</b> by default.
     * Use {@link #setFailed(boolean)} if you do not want
     * this information.
     *
     * @param resName   the name of the resource
     * @param failed the failed
     *
     * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
     */
    @SuppressWarnings("unchecked")
	public boolean setFailed(String resName, boolean failed) {
        // all the PEs are failed (or recovered, depending on fail)
    	this.failed = failed;
        PeList.setStatusFailed((List<Pe>) getPeList(), resName, getId(), failed);
        return true;
    }

    /**
     * Sets the PEs of this machine to a FAILED status.
     *
     * @param failed the failed
     *
     * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
     */
    @SuppressWarnings("unchecked")
	public boolean setFailed(boolean failed) {
        // all the PEs are failed (or recovered, depending on fail)
    	this.failed = failed;
        PeList.setStatusFailed((List<Pe>) getPeList(), failed);
        return true;
    }

    /**
     * Sets the particular Pe status on this Machine.
     *
     * @param status   Pe status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
     * @param peId the pe id
     *
     * @return <tt>true</tt> if the Pe status has changed, <tt>false</tt>
     * otherwise (Pe id might not be exist)
     *
     * @pre peID >= 0
     * @post $none
     */
    @SuppressWarnings("unchecked")
	public boolean setPeStatus(int peId, int status) {
        return PeList.setPeStatus((List<Pe>) getPeList(), peId, status);
    }

	/**
	 * Gets the vms migrating in.
	 *
	 * @return the vms migrating in
	 */
    public List<Vm> getVmsMigratingIn() {
		return vmsMigratingIn;
	}

	/**
	 * Sets the vms migrating in.
	 *
	 * @param vmsMigratingIn the new vms migrating in
	 */
	protected void setVmsMigratingIn(List<Vm> vmsMigratingIn) {
		this.vmsMigratingIn = vmsMigratingIn;
	}
	
	/**
	 * Gets the data center.
	 * @return the data center where the host runs
	 */
	public Datacenter getDatacenter(){
		return this.datacenter;
	}
	
	/**
	 * Sets the data center.
	 *
	 * @param datacenter the data center from this host
	 */
	public void setDatacenter(Datacenter datacenter) {
		this.datacenter = datacenter;
	}

}
