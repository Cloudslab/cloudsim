/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * VmSchedulerSpaceShared is a VMM allocation policy that
 * allocates one or more Pe to a VM, and doesn't allow sharing
 * of PEs. If there is no free PEs to the VM, allocation fails.
 * Free PEs are not allocated to VMs
 *
 * @author		Rodrigo N. Calheiros
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 1.0
 */
public class VmSchedulerSpaceShared extends VmScheduler {

	/** Map containing VM ID and a vector of PEs allocated to this VM. */
	private Map<String, List<Integer>> peAllocationMap;

	/** The free pes vector. */
	private List<Integer> freePes;

	/**
	 * Instantiates a new vm scheduler space shared.
	 *
	 * @param pelist the pelist
	 */
	public VmSchedulerSpaceShared(List<? extends Pe> pelist) {
		super(pelist);
		setPeAllocationMap(new HashMap<String, List<Integer>>());
		setFreePes(new ArrayList<Integer>());
		for (int i = 0; i < pelist.size(); i++) {
			getFreePes().add(i);
		}
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmScheduler#allocatePesForVm(org.cloudbus.cloudsim.Vm, java.util.List)
	 */
	/**
	 * TODO: rewrite using PeProvisioner
	 */
	@Override
	public boolean allocatePesForVm(Vm vm, List<Double> mipsShare) {
		//if there is no enough free PEs, fails
		if (getFreePes().size() < mipsShare.size()) {
			return false;
		}

		double currentFreeMips = getAvailableMips();
		List<Integer> chosenPes = new ArrayList<Integer>();
		List<Double> newMipsList = new LinkedList<Double>();
		for (int i = 0; i < mipsShare.size(); i++) {
			int allocatedPe = getFreePes().remove(0);
			chosenPes.add(allocatedPe);

			//add the smaller between requested MIPS and available MIPS: if PE supplied more capacity than requested,
			//we reduce use of processor (and power consumption). Otherwise, we go PE's full power.
			if (getPeList().get(allocatedPe).getMips() < mipsShare.get(i)) {
				newMipsList.add((double) getPeList().get(allocatedPe).getMips());
				currentFreeMips -= getPeList().get(allocatedPe).getMips();
			} else {
				newMipsList.add(mipsShare.get(i));
				currentFreeMips -= mipsShare.get(i);
			}
		}

		getPeAllocationMap().put(vm.getUid(), chosenPes);
		getMipsMap().put(vm.getUid(), newMipsList);
		setAvailableMips(currentFreeMips);

		return true;
	}


	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmScheduler#deallocatePesForVm(org.cloudbus.cloudsim.Vm)
	 */
	@Override
	public void deallocatePesForVm(Vm vm) {
		// free Pes
		List<Integer> peVector = getPeAllocationMap().remove(vm.getUid());

		if (peVector == null) {
			Log.printLine(this.getClass() + ":[Error]: no Pes allocated for this VM.");
			return;
		}

		while (!peVector.isEmpty()) {
			Integer element = peVector.remove(0);
			getFreePes().add(element);
		}

		// update use of mips
		double currentMips = getAvailableMips();
		for (double mips : getMipsMap().get(vm.getUid())) {
			currentMips += mips;
		}
		setAvailableMips(currentMips);
	}

	/**
	 * Sets the pe allocation map.
	 *
	 * @param peAllocationMap the pe allocation map
	 */
	protected void setPeAllocationMap(Map<String, List<Integer>> peAllocationMap) {
		this.peAllocationMap = peAllocationMap;
	}

	/**
	 * Gets the pe allocation map.
	 *
	 * @return the pe allocation map
	 */
	protected Map<String, List<Integer>> getPeAllocationMap() {
		return peAllocationMap;
	}

	/**
	 * Sets the free pes vector.
	 *
	 * @param freePes the new free pes vector
	 */
	protected void setFreePes(List<Integer> freePes) {
		this.freePes = freePes;
	}

	/**
	 * Gets the free pes vector.
	 *
	 * @return the free pes vector
	 */
	protected List<Integer> getFreePes() {
		return freePes;
	}

}
