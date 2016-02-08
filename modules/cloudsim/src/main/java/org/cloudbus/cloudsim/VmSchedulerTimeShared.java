/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;

/**
 * VmSchedulerTimeShared is a Virtual Machine Monitor (VMM) allocation policy that allocates one or more PEs 
 * from a PM to a VM, and allows sharing of PEs by multiple VMs. This class also implements 10% performance degradation due
 * to VM migration. This scheduler does not support over-subscription.
 * 
 * Each host has to use is own instance of a VmScheduler
 * that will so schedule the allocation of host's PEs for VMs running on it.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class VmSchedulerTimeShared extends VmScheduler {

	/** The map of requested mips, where each key is a VM
         * and each value is a list of MIPS requested by that VM. 
         */
	private Map<String, List<Double>> mipsMapRequested;

	/** The number of host's PEs in use. */
	private int pesInUse;

	/**
	 * Instantiates a new vm time-shared scheduler.
	 * 
	 * @param pelist the list of PEs of the host where the VmScheduler is associated to.
	 */
	public VmSchedulerTimeShared(List<? extends Pe> pelist) {
		super(pelist);
		setMipsMapRequested(new HashMap<String, List<Double>>());
	}

	@Override
	public boolean allocatePesForVm(Vm vm, List<Double> mipsShareRequested) {
		/*
		 * @todo add the same to RAM and BW provisioners
		 */
		if (vm.isInMigration()) {
			if (!getVmsMigratingIn().contains(vm.getUid()) && !getVmsMigratingOut().contains(vm.getUid())) {
				getVmsMigratingOut().add(vm.getUid());
			}
		} else {
			if (getVmsMigratingOut().contains(vm.getUid())) {
				getVmsMigratingOut().remove(vm.getUid());
			}
		}
		boolean result = allocatePesForVm(vm.getUid(), mipsShareRequested);
		updatePeProvisioning();
		return result;
	}

	/**
	 * Allocate PEs for a vm.
	 * 
	 * @param vmUid the vm uid
	 * @param mipsShareRequested the list of mips share requested by the vm
	 * @return true, if successful
	 */
	protected boolean allocatePesForVm(String vmUid, List<Double> mipsShareRequested) {
		double totalRequestedMips = 0;
		double peMips = getPeCapacity();
		for (Double mips : mipsShareRequested) {
			// each virtual PE of a VM must require not more than the capacity of a physical PE
			if (mips > peMips) {
				return false;
			}
			totalRequestedMips += mips;
		}

		// This scheduler does not allow over-subscription
		if (getAvailableMips() < totalRequestedMips) {
			return false;
		}

		getMipsMapRequested().put(vmUid, mipsShareRequested);
		setPesInUse(getPesInUse() + mipsShareRequested.size());

		if (getVmsMigratingIn().contains(vmUid)) {
			// the destination host only experience 10% of the migrating VM's MIPS
			totalRequestedMips *= 0.1;
		}

		List<Double> mipsShareAllocated = new ArrayList<Double>();
		for (Double mipsRequested : mipsShareRequested) {
			if (getVmsMigratingOut().contains(vmUid)) {
				// performance degradation due to migration = 10% MIPS
				mipsRequested *= 0.9;
			} else if (getVmsMigratingIn().contains(vmUid)) {
				// the destination host only experience 10% of the migrating VM's MIPS
				mipsRequested *= 0.1;
			}
			mipsShareAllocated.add(mipsRequested);
		}

		getMipsMap().put(vmUid, mipsShareAllocated);
		setAvailableMips(getAvailableMips() - totalRequestedMips);

		return true;
	}

	/**
	 * Update allocation of VMs on PEs.
         * @too The method is too long and may be refactored to make clearer its
         * responsibility.
	 */
	protected void updatePeProvisioning() {
		getPeMap().clear();
		for (Pe pe : getPeList()) {
			pe.getPeProvisioner().deallocateMipsForAllVms();
		}

		Iterator<Pe> peIterator = getPeList().iterator();
		Pe pe = peIterator.next();
		PeProvisioner peProvisioner = pe.getPeProvisioner();
		double availableMips = peProvisioner.getAvailableMips();

		for (Map.Entry<String, List<Double>> entry : getMipsMap().entrySet()) {
			String vmUid = entry.getKey();
			getPeMap().put(vmUid, new LinkedList<Pe>());

			for (double mips : entry.getValue()) {
				while (mips >= 0.1) {
					if (availableMips >= mips) {
						peProvisioner.allocateMipsForVm(vmUid, mips);
						getPeMap().get(vmUid).add(pe);
						availableMips -= mips;
						break;
					} else {
						peProvisioner.allocateMipsForVm(vmUid, availableMips);
						getPeMap().get(vmUid).add(pe);
						mips -= availableMips;
						if (mips <= 0.1) {
							break;
						}
						if (!peIterator.hasNext()) {
							Log.printConcatLine("There is no enough MIPS (", mips, ") to accommodate VM ", vmUid);
							// System.exit(0);
						}
						pe = peIterator.next();
						peProvisioner = pe.getPeProvisioner();
						availableMips = peProvisioner.getAvailableMips();
					}
				}
			}
		}
	}

	@Override
	public void deallocatePesForVm(Vm vm) {
		getMipsMapRequested().remove(vm.getUid());
		setPesInUse(0);
		getMipsMap().clear();
		setAvailableMips(PeList.getTotalMips(getPeList()));

		for (Pe pe : getPeList()) {
			pe.getPeProvisioner().deallocateMipsForVm(vm);
		}

		for (Map.Entry<String, List<Double>> entry : getMipsMapRequested().entrySet()) {
			allocatePesForVm(entry.getKey(), entry.getValue());
		}

		updatePeProvisioning();
	}

	/**
	 * Releases PEs allocated to all the VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	@Override
	public void deallocatePesForAllVms() {
		super.deallocatePesForAllVms();
		getMipsMapRequested().clear();
		setPesInUse(0);
	}

	/**
	 * Returns maximum available MIPS among all the PEs. For the time shared policy it is just all
	 * the avaiable MIPS.
	 * 
	 * @return max mips
	 */
	@Override
	public double getMaxAvailableMips() {
		return getAvailableMips();
	}

	/**
	 * Sets the number of PEs in use.
	 * 
	 * @param pesInUse the new pes in use
	 */
	protected void setPesInUse(int pesInUse) {
		this.pesInUse = pesInUse;
	}

	/**
	 * Gets the number of PEs in use.
	 * 
	 * @return the pes in use
	 */
	protected int getPesInUse() {
		return pesInUse;
	}

	/**
	 * Gets the mips map requested.
	 * 
	 * @return the mips map requested
	 */
	protected Map<String, List<Double>> getMipsMapRequested() {
		return mipsMapRequested;
	}

	/**
	 * Sets the mips map requested.
	 * 
	 * @param mipsMapRequested the mips map requested
	 */
	protected void setMipsMapRequested(Map<String, List<Double>> mipsMapRequested) {
		this.mipsMapRequested = mipsMapRequested;
	}

}
