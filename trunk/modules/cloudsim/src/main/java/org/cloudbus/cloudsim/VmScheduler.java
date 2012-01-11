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

import org.cloudbus.cloudsim.lists.PeList;

/**
 * VmScheduler is an abstract class that represents the policy used by a VMM to share processing
 * power among VMs running in a host.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public abstract class VmScheduler {

	/** The peList. */
	private List<? extends Pe> peList;

	/** The map of VMs to PEs. */
	private Map<String, List<Pe>> peMap;

	/** The MIPS that are currently allocated to the VMs. */
	private Map<String, List<Double>> mipsMap;

	/** The total available mips. */
	private double availableMips;

	/** The VMs migrating in. */
	private List<String> vmsMigratingIn;

	/** The VMs migrating out. */
	private List<String> vmsMigratingOut;

	/**
	 * Creates a new HostAllocationPolicy.
	 * 
	 * @param pelist the pelist
	 * @pre peList != $null
	 * @post $none
	 */
	public VmScheduler(List<? extends Pe> pelist) {
		setPeList(pelist);
		setPeMap(new HashMap<String, List<Pe>>());
		setMipsMap(new HashMap<String, List<Double>>());
		setAvailableMips(PeList.getTotalMips(getPeList()));
		setVmsMigratingIn(new ArrayList<String>());
		setVmsMigratingOut(new ArrayList<String>());
	}

	/**
	 * Allocates PEs for a VM.
	 * 
	 * @param vm the vm
	 * @param mipsShare the mips share
	 * @return $true if this policy allows a new VM in the host, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocatePesForVm(Vm vm, List<Double> mipsShare);

	/**
	 * Releases PEs allocated to a VM.
	 * 
	 * @param vm the vm
	 * @pre $none
	 * @post $none
	 */
	public abstract void deallocatePesForVm(Vm vm);

	/**
	 * Releases PEs allocated to all the VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public void deallocatePesForAllVms() {
		getMipsMap().clear();
		setAvailableMips(PeList.getTotalMips(getPeList()));
		for (Pe pe : getPeList()) {
			pe.getPeProvisioner().deallocateMipsForAllVms();
		}
	}

	/**
	 * Gets the pes allocated for vm.
	 * 
	 * @param vm the vm
	 * @return the pes allocated for vm
	 */
	public List<Pe> getPesAllocatedForVM(Vm vm) {
		return getPeMap().get(vm.getUid());
	}

	/**
	 * Returns the MIPS share of each Pe that is allocated to a given VM.
	 * 
	 * @param vm the vm
	 * @return an array containing the amount of MIPS of each pe that is available to the VM
	 * @pre $none
	 * @post $none
	 */
	public List<Double> getAllocatedMipsForVm(Vm vm) {
		return getMipsMap().get(vm.getUid());
	}

	/**
	 * Gets the total allocated MIPS for a VM over all the PEs.
	 * 
	 * @param vm the vm
	 * @return the allocated mips for vm
	 */
	public double getTotalAllocatedMipsForVm(Vm vm) {
		double allocated = 0;
		List<Double> mipsMap = getAllocatedMipsForVm(vm);
		if (mipsMap != null) {
			for (double mips : mipsMap) {
				allocated += mips;
			}
		}
		return allocated;
	}

	/**
	 * Returns maximum available MIPS among all the PEs.
	 * 
	 * @return max mips
	 */
	public double getMaxAvailableMips() {
		if (getPeList() == null) {
			Log.printLine("Pe list is empty");
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
	 */
	public double getPeCapacity() {
		if (getPeList() == null) {
			Log.printLine("Pe list is empty");
			return 0;
		}
		return getPeList().get(0).getMips();
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Pe> List<T> getPeList() {
		return (List<T>) peList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param peList the pe list
	 */
	protected <T extends Pe> void setPeList(List<T> peList) {
		this.peList = peList;
	}

	/**
	 * Gets the mips map.
	 * 
	 * @return the mips map
	 */
	protected Map<String, List<Double>> getMipsMap() {
		return mipsMap;
	}

	/**
	 * Sets the mips map.
	 * 
	 * @param mipsMap the mips map
	 */
	protected void setMipsMap(Map<String, List<Double>> mipsMap) {
		this.mipsMap = mipsMap;
	}

	/**
	 * Gets the free mips.
	 * 
	 * @return the free mips
	 */
	public double getAvailableMips() {
		return availableMips;
	}

	/**
	 * Sets the free mips.
	 * 
	 * @param availableMips the new free mips
	 */
	protected void setAvailableMips(double availableMips) {
		this.availableMips = availableMips;
	}

	/**
	 * Gets the vms in migration.
	 * 
	 * @return the vms in migration
	 */
	public List<String> getVmsMigratingOut() {
		return vmsMigratingOut;
	}

	/**
	 * Sets the vms in migration.
	 * 
	 * @param vmsInMigration the new vms migrating out
	 */
	protected void setVmsMigratingOut(List<String> vmsInMigration) {
		vmsMigratingOut = vmsInMigration;
	}

	/**
	 * Gets the vms migrating in.
	 * 
	 * @return the vms migrating in
	 */
	public List<String> getVmsMigratingIn() {
		return vmsMigratingIn;
	}

	/**
	 * Sets the vms migrating in.
	 * 
	 * @param vmsMigratingIn the new vms migrating in
	 */
	protected void setVmsMigratingIn(List<String> vmsMigratingIn) {
		this.vmsMigratingIn = vmsMigratingIn;
	}

	/**
	 * Gets the pe map.
	 * 
	 * @return the pe map
	 */
	public Map<String, List<Pe>> getPeMap() {
		return peMap;
	}

	/**
	 * Sets the pe map.
	 * 
	 * @param peMap the pe map
	 */
	protected void setPeMap(Map<String, List<Pe>> peMap) {
		this.peMap = peMap;
	}

}
