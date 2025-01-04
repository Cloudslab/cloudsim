/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.lists.PeList;

/**
 * VmScheduler is an abstract class that represents the policy used by a Virtual Machine Monitor (VMM) 
 * to share processing power of a PM among VMs running in a host. 
 * 
 * Each host has to use is own instance of a VmScheduler
 * that will so schedule the allocation of host's PEs for VMs running on it.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 */
public abstract class VmScheduler {

	/** The PEs of the host where the scheduler is associated. */
	private List<? extends Pe> peList;

	/** The map of VMs to PEs, where each key is a VM id and each value is
         * a list of PEs allocated to that VM. */
	private Map<String, List<Pe>> peMap;

	/** The map of VMs to MIPS, were each key is a VM id and each value is
         * the currently allocated MIPS from the respective PE to that VM. 
         * The PEs where the MIPS capacity is get are defined
         * in the {@link #peMap}.
         */
	private Map<String, List<Double>> mipsMapAllocated;

	/** The total available MIPS that can be allocated on demand for VMs. */
    private double availableMips;

	/** The VMs migrating in the host (arriving). It is the list of VM ids */
	private List<String> guestsMigratingIn;

	/** The VMs migrating out the host (departing). It is the list of VM ids */
	private List<String> guestsMigratingOut;

	/**
	 * Creates a new VmScheduler.
	 * 
	 * @param pelist the list of PEs of the host where the VmScheduler is associated to.
	 * @pre peList != $null
	 * @post $none
	 */
	public VmScheduler(List<? extends Pe> pelist) {
		setPeList(pelist);
		setPeMap(new HashMap<>());
		setMipsMapAllocated(new HashMap<>());
		setAvailableMips(PeList.getTotalMips(getPeList()));
		setGuestsMigratingIn(new ArrayList<>());
		setGuestsMigratingOut(new ArrayList<>());
	}

	/**
	 * Requests the allocation of PEs for a VM.
	 *
	 * @param guest     the vm
	 * @param mipsShareRequested the list of MIPS share to be allocated to a VM
	 * @return $true if this policy allows a new VM in the host, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocatePesForGuest(GuestEntity guest, List<Double> mipsShareRequested);

	@Deprecated
	public boolean allocatePesForVm(Vm vm, List<Double> mipsShare) {
		return allocatePesForGuest(vm, mipsShare);
	}

	/**
	 * Releases PEs allocated to a VM. After that, the PEs may be used
	 * on demand by other VMs.
	 *
	 * @param guest the vm
	 * @pre $none
	 * @post $none
	 */
	public abstract void deallocatePesForGuest(GuestEntity guest);

	@Deprecated
	public void deallocatePesForVm(Vm vm) {
		deallocatePesForGuest(vm);
	}

	/**
	 * Releases PEs allocated to all the VMs of the host the VmScheduler is associated to.
         * After that, all PEs will be available to be used on demand for requesting VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public void deallocatePesForAllGuests() {
		getMipsMapAllocated().clear();
		setAvailableMips(PeList.getTotalMips(getPeList()));
		for (Pe pe : getPeList()) {
			pe.getPeProvisioner().deallocateMipsForAllGuests();
		}
	}

	@Deprecated
	public void deallocatePesForAllVms() {
		deallocatePesForAllGuests();
	}

	/**
	 * Gets the pes allocated for a vm.
	 *
	 * @param guest the vm
	 * @return the pes allocated for the given vm
	 */
	public List<Pe> getPesAllocatedForGuest(GuestEntity guest) {
		return getPeMap().get(guest.getUid());
	}

	@Deprecated
	public List<Pe> getPesAllocatedForVM(Vm vm) {
		return getPesAllocatedForGuest(vm);
	}

	/**
	 * Returns the MIPS share of each host's Pe that is allocated to a given VM.
	 *
	 * @param guest the vm
	 * @return a list containing the amount of MIPS of each pe that is available to the VM
	 * @pre $none
	 * @post $none
	 */
	public List<Double> getAllocatedMipsForGuest(GuestEntity guest) {
		return getMipsMapAllocated().get(guest.getUid());
	}

	@Deprecated
	public List<Double> getAllocatedMipsForVm(Vm vm) {
		return getAllocatedMipsForGuest(vm);
	}

	/**
	 * Gets the total allocated MIPS for a VM along all its allocated PEs.
	 *
	 * @param guest the vm
	 * @return the total allocated mips for the vm
	 */
	public double getTotalAllocatedMipsForGuest(GuestEntity guest) {
		double allocated = 0;
		List<Double> mipsMap = getAllocatedMipsForGuest(guest);
		if (mipsMap != null) {
			for (double mips : mipsMap) {
				allocated += mips;
			}
		}
		return allocated;
	}

	@Deprecated
	public double getTotalAllocatedMipsForVm(Vm vm) {
		return getTotalAllocatedMipsForGuest(vm);
	}

	/**
	 * Returns maximum available MIPS among all the host's PEs.
	 * 
	 * @return max mips
	 */
	public double getMaxAvailableMips() {
		if (getPeList() == null) {
			Log.println("Pe list is empty");
			return 0;
		}

		double max = 0.0;
		for (Pe pe : getPeList()) {
			double tmp = pe.getPeProvisioner().getAvailableMips();
			if (tmp > max) {
				max = tmp;
			}
		}

		return max;
	}

	/**
	 * Returns PE capacity in MIPS.
	 * 
	 * @return mips
         * //@TODO It considers that all PEs have the same capacity,
         * what has been shown doesn't be assured. The peList
         * received by the VmScheduler can be heterogeneous PEs.
	 */
	public double getPeCapacity() {
		if (getPeList() == null) {
			Log.println("Pe list is empty");
			return 0;
		}
		return getPeList().getFirst().getMips();
	}

	public List<? extends Pe> getPeList() { return peList; }
	protected void setPeList(List<? extends Pe> peList) { this.peList = peList; }

	public Map<String, List<Pe>> getPeMap() { return peMap; }
	protected void setPeMap(Map<String, List<Pe>> peMap) { this.peMap = peMap; }

	public Map<String, List<Double>> getMipsMapAllocated() { return mipsMapAllocated; }
	protected void setMipsMapAllocated(Map<String, List<Double>> mipsMapAllocated) { this.mipsMapAllocated = mipsMapAllocated; }

	public double getAvailableMips() { return availableMips; }
	protected void setAvailableMips(double availableMips) { this.availableMips = availableMips; }

	public List<String> getGuestsMigratingIn() {
		return guestsMigratingIn;
	}
	protected void setGuestsMigratingIn(List<String> guestsMigratingIn) { this.guestsMigratingIn = guestsMigratingIn; }

	public List<String> getGuestsMigratingOut() { return guestsMigratingOut; }
	protected void setGuestsMigratingOut(List<String> guestsMigratingOut) { this.guestsMigratingOut = guestsMigratingOut; }

	@Deprecated
	public List<String> getVmsMigratingIn() { return getGuestsMigratingIn(); }


	@Deprecated
	public List<String> getVmsMigratingOut() { return getGuestsMigratingOut(); }

	@Deprecated
	public Map<String, List<Double>> getMipsMap() { return mipsMapAllocated; }
}
