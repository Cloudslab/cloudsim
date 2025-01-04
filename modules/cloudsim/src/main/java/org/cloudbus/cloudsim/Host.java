/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.*;

import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.core.VirtualEntity;
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
	private final List<? extends GuestEntity> guestList = new ArrayList<>();

	/** The Processing Elements (PEs) of the host, that
         * represent the CPU cores of it, and thus, its processing capacity. */
	private List<? extends Pe> peList;

	/** Tells whether this host is working properly or has failed. */
	private boolean failed;

	/** The VMs migrating in. */
	private final List<? extends GuestEntity> guestsMigratingIn = new ArrayList<>();

	/** The datacenter where the host is placed. */
	private Datacenter datacenter;

	/** guest id -> overhead */
	private Map<Integer, Integer> cachedVirtualizationOverhead;

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
		setGuestRamProvisioner(ramProvisioner);
		setGuestBwProvisioner(bwProvisioner);
		setStorage(storage);
		setGuestScheduler(vmScheduler);

		setPeList(peList);
		setFailed(false);

		cachedVirtualizationOverhead = new HashMap<>();
	}

	/**
	 * Requests updating of cloudlets' processing in VMs running in this host.
	 * 
	 * @param currentTime the current time
	 * @return expected time of completion of the next cloudlet in all VMs in this host or
	 *         {@link Double#MAX_VALUE} if there is no future events expected in this host
	 * @pre currentTime >= 0.0
	 * @post $none
         * //@TODO there is an inconsistency between the return value of this method
         * and the individual call of {@link GuestEntity#updateCloudletsProcessing(double, List) ,
         * and consequently the {@link CloudletScheduler#updateCloudletsProcessing(double, List) }.
         * The current method returns {@link Double#MAX_VALUE}  while the other ones
         * return 0. It has to be checked if there is a reason for this
         * difference.}
	 */
	public double updateCloudletsProcessing(double currentTime) {
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

	@Deprecated
	public double updateVmsProcessing(double currentTime) {
		return updateCloudletsProcessing(currentTime);
	}



	@Deprecated
	public boolean isSuitableForVm(Vm vm) { return isSuitableForGuest(vm); }

	/**
	 * Find guest (which could be nested) and return its total virtualization overhead
	 *
	 * @param guestId guest to compute the total virtualization overhead
	 * @param it iterator to a guest list
	 * @param acc accumulated virtualization overhead so far
	 * @return 0 if guest is not present
	 */
	public int getTotalVirtualizationOverhead(int guestId, Iterator<GuestEntity> it, int acc) {
		if (cachedVirtualizationOverhead.containsKey(guestId)) {
			return acc + cachedVirtualizationOverhead.get(guestId);
		}

		if (!it.hasNext()) {
			return 0;
		}

		GuestEntity currGuest = it.next();
		if (!cachedVirtualizationOverhead.containsKey(currGuest.getId())) {
			cachedVirtualizationOverhead.put(currGuest.getId(), acc + currGuest.getVirtualizationOverhead());
		}

		if (currGuest.getId() == guestId) {
			return acc + currGuest.getVirtualizationOverhead();
		}

		int nested = 0;
		if (currGuest instanceof VirtualEntity vm)
			nested = getTotalVirtualizationOverhead(guestId, vm.getGuestList().iterator(), acc+currGuest.getVirtualizationOverhead());

		int next = getTotalVirtualizationOverhead(guestId, it, acc);

		return Math.max(nested, next);
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

	// Instantiated by default
	public boolean isBeingInstantiated() {
		return false;
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

	@Deprecated
	public List<Double> getAllocatedMipsForVm(Vm vm) { return getAllocatedMipsForGuest(vm); }

	/**
	 * Gets the total allocated MIPS for a VM along all its PEs.
	 *
	 * @param guest the vm
	 * @return the allocated mips for vm
	 */
	public double getTotalAllocatedMipsForGuest(GuestEntity guest) {
		return getGuestScheduler().getTotalAllocatedMipsForGuest(guest);
	}

	@Deprecated
	public double getTotalAllocatedMipsForVm(Vm vm) { return getTotalAllocatedMipsForGuest(vm); }

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
		return getGuestBwProvisioner().getBw();
	}

	/**
	 * Gets the host memory.
	 * 
	 * @return the host memory
	 * @pre $none
	 * @post $result > 0
	 */
	public int getRam() {
		return getGuestRamProvisioner().getRam();
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
	public RamProvisioner getGuestRamProvisioner() {
		return ramProvisioner;
	}

	@Deprecated
	public RamProvisioner getRamProvisioner() { return getGuestRamProvisioner(); }

	/**
	 * Sets the ram provisioner.
	 * 
	 * @param ramProvisioner the new ram provisioner
	 */
	protected void setGuestRamProvisioner(RamProvisioner ramProvisioner) {
		this.ramProvisioner = ramProvisioner;
	}

	@Deprecated
	protected void setRamProvisioner(RamProvisioner ramProvisioner) { setGuestRamProvisioner(ramProvisioner);}

	/**
	 * Gets the bw provisioner.
	 * 
	 * @return the bw provisioner
	 */
	public BwProvisioner getGuestBwProvisioner() {
		return bwProvisioner;
	}

	@Deprecated
	public BwProvisioner getBwProvisioner() { return getGuestBwProvisioner(); }

	/**
	 * Sets the bw provisioner.
	 * 
	 * @param bwProvisioner the new bw provisioner
	 */
	protected void setGuestBwProvisioner(BwProvisioner bwProvisioner) {
		this.bwProvisioner = bwProvisioner;
	}

	@Deprecated
	protected void setBwProvisioner(BwProvisioner bwProvisioner) { setGuestBwProvisioner(bwProvisioner); }

	/**
	 * Gets the VM scheduler.
	 * 
	 * @return the VM scheduler
	 */
	public VmScheduler getGuestScheduler() { return vmScheduler; }

	@Deprecated
	public VmScheduler getVmScheduler() { return getGuestScheduler(); }

	/**
	 * Sets the VM scheduler.
	 * 
	 * @param vmScheduler the vm scheduler
	 */
	protected void setGuestScheduler(VmScheduler vmScheduler) {
		this.vmScheduler = vmScheduler;
	}

	@Deprecated
	protected void setVmScheduler(VmScheduler vmScheduler) { setGuestScheduler(vmScheduler); }

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
	public <T extends GuestEntity> List<T> getGuestList() { return (List<T>) guestList; }

	@Deprecated
	public <T extends Vm> List<T> getVmList() { return getGuestList(); }

	/**
	 * Sets the storage.
	 * 
	 * @param storage the new storage
	 */
	public void setStorage(long storage) {
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

	/** @TODO: Not in use, what to do with this? */
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
	 * Gets the vms migrating in.
	 *
	 * @return the vms migrating in
	 */
	public <T extends GuestEntity> List<T> getGuestsMigratingIn() {
		return (List<T>) guestsMigratingIn;
	}

	@Deprecated
	public <T extends Vm> List<T> getVmsMigratingIn() { return getGuestsMigratingIn(); }

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


	/**
	 * DEPRECATED: TO BE REMOVED!
	 */
	@Deprecated
	public boolean vmCreate(Vm vm) { return guestCreate(vm); }

	@Deprecated
	public void vmDestroy(Vm vm) { guestDestroy(vm); }

	@Deprecated
	public void vmDestroyAll() { guestDestroyAll(); }

	@Deprecated
	protected void vmDeallocate(Vm vm) { guestDeallocate(vm); }

	@Deprecated
	protected void vmDeallocateAll() { guestDestroyAll(); }

	@Deprecated
	public Vm getVm(int vmId, int userId) { return (Vm) getGuest(vmId, userId); }

	@Deprecated
	public void addMigratingInVm(Vm vm) { addMigratingInGuest(vm); }

	@Deprecated
	public void removeMigratingInVm(Vm vm) { removeMigratingInGuest(vm); }

	@Deprecated
	public void reallocateMigratingInVms() { reallocateMigratingInGuests(); }
}
