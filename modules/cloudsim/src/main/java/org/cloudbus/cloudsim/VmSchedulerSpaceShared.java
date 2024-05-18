/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import org.cloudbus.cloudsim.core.GuestEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * VmSchedulerSpaceShared is a VMM allocation policy that allocates one or more PEs from a host to a 
 * Virtual Machine Monitor (VMM), and doesn't allow sharing of PEs. 
 * The allocated PEs will be used until the VM finishes running. 
 * If there is no enough free PEs as required by a VM,
 * or whether the available PEs doesn't have enough capacity, the allocation fails. 
 * In the case of fail, no PE is allocated to the requesting VM.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class VmSchedulerSpaceShared extends VmScheduler {

	/** A map between each VM and its allocated PEs, where the key is a VM ID and
         * the value a list of PEs allocated to VM. */
	private Map<String, List<Pe>> peAllocationMap;

	/** The list of free PEs yet available in the host. */
	private List<Pe> freePes;

	/**
	 * Instantiates a new vm space-shared scheduler.
	 * 
	 * @param pelist the pelist
	 */
	public VmSchedulerSpaceShared(List<? extends Pe> pelist) {
		super(pelist);
		setPeAllocationMap(new HashMap<>());
		setFreePes(new ArrayList<>());
		getFreePes().addAll(pelist);
	}

	@Override
	public boolean allocatePesForGuest(GuestEntity guest, List<Double> mipsShare) {
		// if there is no enough free PEs, fails
		if (getFreePes().size() < mipsShare.size()) {
			return false;
		}

		List<Pe> selectedPes = new ArrayList<>();
		Iterator<Pe> peIterator = getFreePes().iterator();
		Pe pe = peIterator.next();
		double totalMips = 0;
		for (Double mips : mipsShare) {
			if (mips <= pe.getMips()) {
				selectedPes.add(pe);
				totalMips += mips;

				if (!peIterator.hasNext()) {
					break;
				}
				pe = peIterator.next();
			}
		}
		if (mipsShare.size() > selectedPes.size()) {
			return false;
		}

		getFreePes().removeAll(selectedPes);

		getPeAllocationMap().put(guest.getUid(), selectedPes);
		getMipsMapAllocated().put(guest.getUid(), mipsShare);
		setAvailableMips(getAvailableMips() - totalMips);
		guest.setCurrentAllocatedMips(mipsShare);

		return true;
	}

	@Override
	public void deallocatePesForGuest(GuestEntity guest) {
		getFreePes().addAll(getPeAllocationMap().get(guest.getUid()));
		getPeAllocationMap().remove(guest.getUid());

		double totalMips = 0;
		for (double mips : getMipsMapAllocated().get(guest.getUid())) {
			totalMips += mips;
		}
		setAvailableMips(getAvailableMips() + totalMips);

		getMipsMapAllocated().remove(guest.getUid());
	}

	/**
	 * Sets the pe allocation map.
	 * 
	 * @param peAllocationMap the pe allocation map
	 */
	protected void setPeAllocationMap(Map<String, List<Pe>> peAllocationMap) {
		this.peAllocationMap = peAllocationMap;
	}

	/**
	 * Gets the pe allocation map.
	 * 
	 * @return the pe allocation map
	 */
	protected Map<String, List<Pe>> getPeAllocationMap() {
		return peAllocationMap;
	}

	/**
	 * Sets the free pes list.
	 * 
	 * @param freePes the new free pes list
	 */
	protected void setFreePes(List<Pe> freePes) {
		this.freePes = freePes;
	}

	/**
	 * Gets the free pes list.
	 * 
	 * @return the free pes list
	 */
	protected List<Pe> getFreePes() {
		return freePes;
	}

}
