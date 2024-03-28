/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * A Host is a Physical Machine (PM) inside a Datacenter. It is also called as a Server.
 * It executes actions related to management of virtual machines (e.g., creation and destruction).
 * A host has a defined policy for provisioning memory and bw, as well as an allocation policy for
 * Pe's to virtual machines. A host is associated to a datacenter. It can host virtual machines.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 */
public class Host implements HostEntity {

	/** The id of the host. */
	private int id;

	/** The storage capacity. */
	private long storage;

	/** The ram provisioner. */
	private RamProvisioner ramProvisioner;

	/** The bw provisioner. */
	private BwProvisioner bwProvisioner;

	/** The allocation policy for scheduling VM execution. */
	private VmScheduler vmScheduler;

	/** The list of VMs assigned to the host. */
	private final List<? extends Vm> vmList = new ArrayList<>();

	/** The Processing Elements (PEs) of the host, that
         * represent the CPU cores of it, and thus, its processing capacity. */
	private List<? extends Pe> peList;

	/** Tells whether this host is working properly or has failed. */
	private boolean failed;

	/** The VMs migrating in. */
	private final List<GuestEntity> guestsMigratingIn = new ArrayList<>();

	/** The datacenter where the host is placed. */
	private Datacenter datacenter;

	/**
	 * Instantiates a new host.
	 * 
	 * @param id the host id
	 * @param ramProvisioner the ram provisioner
	 * @param bwProvisioner the bw provisioner
	 * @param storage the storage capacity
	 * @param peList the host's PEs list
	 * @param vmScheduler the vm scheduler
	 */
	public Host(
			int id,
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
		setFailed(false);
	}

	/**
	 * Requests updating of cloudlets' processing in VMs running in this host.
	 * 
	 * @param currentTime the current time
	 * @return expected time of completion of the next cloudlet in all VMs in this host or
	 *         {@link Double#MAX_VALUE} if there is no future events expected in this host
	 * @pre currentTime >= 0.0
	 * @post $none
         * //TODO there is an inconsistency between the return value of this method
         * and the individual call of {@link GuestEntity#updateCloudletsProcessing(double, List) ,
         * and consequently the {@link CloudletScheduler#updateCloudletsProcessing(double, List) }.
         * The current method returns {@link Double#MAX_VALUE}  while the other ones
         * return 0. It has to be checked if there is a reason for this
         * difference.}
	 */
	public double updateGuestsProcessing(double currentTime) {
		double smallerTime = Double.MAX_VALUE;

		for (GuestEntity vm : getGuestList()) {
			double time = vm.updateCloudletsProcessing(
                                currentTime, getGuestScheduler().getAllocatedMipsForGuest(vm));
			if (time > 0.0 && time < smallerTime) {
				smallerTime = time;
			}
		}

		return smallerTime;
	}

	/**
	 * Adds a VM migrating into the current (dstHost) host.
	 *
	 * @param guest the vm
	 */
	public void addMigratingInGuest(GuestEntity guest) {
		guest.setInMigration(true);

		if (!getGuestsMigratingIn().contains(guest)) {
			if (getStorage() < guest.getSize()) {
				Log.printConcatLine("[VmScheduler.addMigratingInVm] Allocation of VM #", guest.getId(), " to Host #",
						getId(), " failed by storage");
				System.exit(0);
			}

			if (!getRamProvisioner().allocateRamForGuest(guest, guest.getCurrentRequestedRam())) {
				Log.printConcatLine("[VmScheduler.addMigratingInVm] Allocation of VM #", guest.getId(), " to Host #",
						getId(), " failed by RAM");
				System.exit(0);
			}

			if (!getBwProvisioner().allocateBwForGuest(guest, guest.getCurrentRequestedBw())) {
				Log.printLine("[VmScheduler.addMigratingInVm] Allocation of VM #" + guest.getId() + " to Host #"
						+ getId() + " failed by BW");
				System.exit(0);
			}

			getGuestScheduler().getGuestsMigratingIn().add(guest.getUid());
			if (!getGuestScheduler().allocatePesForGuest(guest, guest.getCurrentRequestedMips())) {
				Log.printLine("[VmScheduler.addMigratingInVm] Allocation of VM #" + guest.getId() + " to Host #"
						+ getId() + " failed by MIPS");
				System.exit(0);
			}

			setStorage(getStorage() - guest.getSize());

			getGuestsMigratingIn().add(guest);
			getGuestList().add(guest);
			updateGuestsProcessing(CloudSim.clock());
			guest.getHost().updateGuestsProcessing(CloudSim.clock());
		}
	}

	/**
	 * Removes a migrating in vm.
	 *
	 * @param guest the vm
	 */
	public void removeMigratingInGuest(GuestEntity guest) {
		guestDeallocate(guest);
		getGuestsMigratingIn().remove(guest);
		getGuestList().remove(guest);
		getGuestScheduler().getGuestsMigratingIn().remove(guest.getUid());
		guest.setInMigration(false);
	}

	/**
	 * Reallocate migrating in vms. Gets the VM in the migrating in queue
         * and allocate them on the host.
	 */
	public void reallocateMigratingInGuests() {
		for (GuestEntity vm : getGuestsMigratingIn()) {
			if (!getGuestList().contains(vm)) {
				getGuestList().add(vm);
			}
			if (!getGuestScheduler().getGuestsMigratingIn().contains(vm.getUid())) {
				getGuestScheduler().getGuestsMigratingIn().add(vm.getUid());
			}
			getRamProvisioner().allocateRamForGuest(vm, vm.getCurrentRequestedRam());
			getBwProvisioner().allocateBwForGuest(vm, vm.getCurrentRequestedBw());
			getGuestScheduler().allocatePesForGuest(vm, vm.getCurrentRequestedMips());
			setStorage(getStorage() - vm.getSize());
		}
	}

	/**
	 * Checks if the host is suitable for vm. If it has enough resources
	 * to attend the VM.
	 *
	 * @param guest the vm
	 * @return true, if is suitable for vm
	 */
	public boolean isSuitableForGuest(GuestEntity guest) {
		return (getGuestScheduler().getPeCapacity() >= guest.getCurrentRequestedMaxMips()
				&& getGuestScheduler().getAvailableMips() >= guest.getCurrentRequestedTotalMips()
				&& getRamProvisioner().isSuitableForGuest(guest, guest.getCurrentRequestedRam()) && getBwProvisioner()
				.isSuitableForGuest(guest, guest.getCurrentRequestedBw()));
	}

	/**
	 * Try to allocate resources to a new VM in the Host.
	 *
	 * @param guest Vm being started
	 * @return $true if the VM could be started in the host; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean guestCreate(GuestEntity guest) {
		if (getStorage() < guest.getSize()) {
			Log.printConcatLine("[VmScheduler.vmCreate] Allocation of VM #", guest.getId(), " to Host #", getId(),
					" failed by storage");
			return false;
		}

		if (!getRamProvisioner().allocateRamForGuest(guest, guest.getCurrentRequestedRam())) {
			Log.printConcatLine("[VmScheduler.vmCreate] Allocation of VM #", guest.getId(), " to Host #", getId(),
					" failed by RAM");
			return false;
		}

		if (!getBwProvisioner().allocateBwForGuest(guest, guest.getCurrentRequestedBw())) {
			Log.printConcatLine("[VmScheduler.vmCreate] Allocation of VM #", guest.getId(), " to Host #", getId(),
					" failed by BW");
			getRamProvisioner().deallocateRamForGuest(guest);
			return false;
		}

		if (!getGuestScheduler().allocatePesForGuest(guest, guest.getCurrentRequestedMips())) {
			Log.printConcatLine("[VmScheduler.vmCreate] Allocation of VM #", guest.getId(), " to Host #", getId(),
					" failed by MIPS");
			getRamProvisioner().deallocateRamForGuest(guest);
			getBwProvisioner().deallocateBwForGuest(guest);
			return false;
		}

		setStorage(getStorage() - guest.getSize());
		getGuestList().add(guest);
		guest.setHost(this);
		return true;
	}

	/**
	 * Destroys a VM running in the host.
	 *
	 * @param guest the VM
	 * @pre $none
	 * @post $none
	 */
	public void guestDestroy(GuestEntity guest) {
		if (guest != null) {
			guestDeallocate(guest);
			getGuestList().remove(guest);
			guest.setHost(null);
		}
	}

	/**
	 * Destroys all VMs running in the host.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public void guestDestroyAll() {
		guestDeallocateAll();
		for (GuestEntity vm : getGuestList()) {
			vm.setHost(null);
			setStorage(getStorage() + vm.getSize());
		}
		getGuestList().clear();
	}

	/**
	 * Deallocate all resources of a VM.
	 *
	 * @param guest the VM
	 */
	protected void guestDeallocate(GuestEntity guest) {
		getRamProvisioner().deallocateRamForGuest(guest);
		getBwProvisioner().deallocateBwForGuest(guest);
		getGuestScheduler().deallocatePesForGuest(guest);
		setStorage(getStorage() + guest.getSize());
	}

	/**
	 * Deallocate all resources of all VMs.
	 */
	protected void guestDeallocateAll() {
		getRamProvisioner().deallocateRamForAllGuests();
		getBwProvisioner().deallocateBwForAllGuests();
		getGuestScheduler().deallocatePesForAllGuests();
	}

	/**
	 * Gets a VM by its id and user.
	 *
	 * @param vmId   the vm id
	 * @param userId ID of VM's owner
	 * @return the virtual machine object, $null if not found
	 * @pre $none
	 * @post $none
	 */
	public GuestEntity getGuest(int vmId, int userId) {
		for (GuestEntity vm : getGuestList()) {
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
	public int getNumberOfPes() {
		return getPeList().size();
	}

	/**
	 * Gets the free pes number.
	 * 
	 * @return the free pes number
	 */
	public int getNumberOfFreePes() {
		return PeList.getNumberOfFreePes(getPeList());
	}

	/**
	 * Gets the total mips.
	 * 
	 * @return the total mips
	 */
	public double getTotalMips() {
		return PeList.getTotalMips(getPeList());
	}

	/**
	 * Allocates PEs for a VM.
	 *
	 * @param guest     the vm
	 * @param mipsShare the list of MIPS share to be allocated to the VM
	 * @return $true if this policy allows a new VM in the host, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean allocatePesForGuest(GuestEntity guest, List<Double> mipsShare) {
		return getGuestScheduler().allocatePesForGuest(guest, mipsShare);
	}

	/**
	 * Releases PEs allocated to a VM.
	 *
	 * @param guest the vm
	 * @pre $none
	 * @post $none
	 */
	public void deallocatePesForGuest(GuestEntity guest) {
		getGuestScheduler().deallocatePesForGuest(guest);
	}

	/**
	 * Gets the MIPS share of each Pe that is allocated to a given VM.
	 *
	 * @param guest the vm
	 * @return an array containing the amount of MIPS of each pe that is available to the VM
	 * @pre $none
	 * @post $none
	 */
	public List<Double> getAllocatedMipsForGuest(GuestEntity guest) {
		return getGuestScheduler().getAllocatedMipsForGuest(guest);
	}

	/**
	 * Gets the total allocated MIPS for a VM along all its PEs.
	 *
	 * @param guest the vm
	 * @return the allocated mips for vm
	 */
	public double getTotalAllocatedMipsForGuest(GuestEntity guest) {
		return getGuestScheduler().getTotalAllocatedMipsForGuest(guest);
	}

	/**
	 * Returns the maximum available MIPS among all the PEs of the host.
	 * 
	 * @return max mips
	 */
	public double getMaxAvailableMips() {
		return getGuestScheduler().getMaxAvailableMips();
	}

	/**
	 * Gets the total free MIPS available at the host.
	 * 
	 * @return the free mips
	 */
	public double getAvailableMips() {
		return getGuestScheduler().getAvailableMips();
	}

	/**
	 * Gets the host bw.
	 * 
	 * @return the host bw
	 * @pre $none
	 * @post $result > 0
	 */
	public long getBw() {
		return getBwProvisioner().getBw();
	}

	/**
	 * Gets the host memory.
	 * 
	 * @return the host memory
	 * @pre $none
	 * @post $result > 0
	 */
	public int getRam() {
		return getRamProvisioner().getRam();
	}

	/**
	 * Gets the host storage.
	 * 
	 * @return the host storage
	 * @pre $none
	 * @post $result >= 0
	 */
	public long getStorage() {
		return storage;
	}

	/**
	 * Gets the host id.
	 * 
	 * @return the host id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the host id.
	 * 
	 * @param id the new host id
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
	public VmScheduler getGuestScheduler() {
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
	 * @param <T> the generic type
	 * @return the pe list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Pe> List<T> getPeList() {
		return (List<T>) peList;
	}

	/**
	 * Sets the pe list.
	 * 
	 * @param <T> the generic type
	 * @param peList the new pe list
	 */
	protected <T extends Pe> void setPeList(List<T> peList) {
		this.peList = peList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends GuestEntity> List<T> getGuestList() {
		return (List<T>) vmList;
	}

	public int getNumberOfGuests() {
		return getGuestList().size();
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
	 * Checks if the host PEs have failed.
	 * 
	 * @return true, if the host PEs have failed; false otherwise
	 */
	public boolean isFailed() {
		return failed;
	}

	/** TODO: Remo Andreoli: Not in use, what to do with this? */
	@Override
	public boolean isInWaiting() { return false; }
	@Override
	public void setInWaiting(boolean inWaiting) {}

	/**
	 * Sets the PEs of the host to a FAILED status. NOTE: <tt>resName</tt> is used for debugging
	 * purposes, which is <b>ON</b> by default. Use {@link #setFailed(boolean)} if you do not want
	 * this information.
	 * 
	 * @param resName the name of the resource
	 * @param failed the failed
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	public boolean setFailed(String resName, boolean failed) {
		// all the PEs are failed (or recovered, depending on fail)
		this.failed = failed;
		PeList.setStatusFailed(getPeList(), resName, getId(), failed);
		return true;
	}

	/**
	 * Sets the PEs of the host to a FAILED status.
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
	 * Sets the particular Pe status on the host.
	 * 
	 * @param peId the pe id
	 * @param status Pe status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
	 * @return <tt>true</tt> if the Pe status has changed, <tt>false</tt> otherwise (Pe id might not
	 *         be exist)
	 * @pre peID >= 0
	 * @post $none
	 */
	public boolean setPeStatus(int peId, int status) {
		return PeList.setPeStatus(getPeList(), peId, status);
	}

	/**
	 * Gets the vms migrating in.
	 *
	 * @return the vms migrating in
	 */
	public List<GuestEntity> getGuestsMigratingIn() {
		return guestsMigratingIn;
	}

	/**
	 * Gets the data center of the host.
	 * 
	 * @return the data center where the host runs
	 */
	public Datacenter getDatacenter() {
		return datacenter;
	}

	/**
	 * Sets the data center of the host.
	 * 
	 * @param datacenter the data center from this host
	 */
	public void setDatacenter(Datacenter datacenter) {
		this.datacenter = datacenter;
	}

}
