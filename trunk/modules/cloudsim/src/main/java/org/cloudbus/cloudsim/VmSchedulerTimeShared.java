/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;

/**
 * VmSchedulerTimeShared is a VMM allocation policy that allocates one or more Pe to a VM, and
 * allows sharing of PEs by multiple VMs. This class also implements 10% performance degration due
 * to VM migration.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class VmSchedulerTimeShared extends VmScheduler {

	/** The mips map requested. */
	private Map<String, List<Double>> mipsMapRequested;

	/** The pes in use. */
	private int pesInUse;

	/**
	 * Instantiates a new vm scheduler time shared.
	 * 
	 * @param pelist the pelist
	 */
	public VmSchedulerTimeShared(List<? extends Pe> pelist) {
		super(pelist);
		setMipsMapRequested(new HashMap<String, List<Double>>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cloudsim.VmScheduler#allocatePesForVm(cloudsim.Vm, java.util.List)
	 */
	@Override
	public boolean allocatePesForVm(Vm vm, List<Double> mipsShareRequested) {
		/**
		 * TODO: add the same to RAM and BW provisioners
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
	 * Allocate pes for vm.
	 * 
	 * @param vmUid the vm uid
	 * @param mipsShareRequested the mips share requested
	 * 
	 * @return true, if successful
	 */
	protected boolean allocatePesForVm(String vmUid, List<Double> mipsShareRequested) {
		getMipsMapRequested().put(vmUid, mipsShareRequested);
		setPesInUse(getPesInUse() + mipsShareRequested.size());

		double totalRequestedMips = 0;
		double peMips = getPeCapacity();
		for (Double mips : mipsShareRequested) {
			if (mips > peMips) { // each virtual PE of a VM must require not more than the capacity
									// of a physical PE
				return false;
			}
			totalRequestedMips += mips;
		}

		if (getVmsMigratingIn().contains(vmUid)) {
			totalRequestedMips *= 0.1; // performance cost incurred by the destination host = 10%
										// MIPS
		}

		List<Double> mipsShareAllocated = new ArrayList<Double>();
		for (Double mipsRequested : mipsShareRequested) {
			if (getVmsMigratingOut().contains(vmUid)) {
				mipsRequested *= 0.9; // performance degradation due to migration = 10% MIPS
			} else if (getVmsMigratingIn().contains(vmUid)) {
				mipsRequested *= 0.1; // performance cost incurred by the destination host = 90% MIPS
			}
			mipsShareAllocated.add(mipsRequested);
		}

		if (getAvailableMips() >= totalRequestedMips) {
			getMipsMap().put(vmUid, mipsShareAllocated);
			setAvailableMips(getAvailableMips() - totalRequestedMips);
		} else {			
			updateShortage();
		}

		return true;
	}

	/**
	 * This method recalculates distribution of MIPs among VMs considering eventual shortage of MIPS
	 * compared to the amount requested by VMs.
	 */
	protected void updateShortage() {
		//first, we have to know the wight of each VM
		//in the allocation of mips. We want to keep
		//allocation proportional to it
		
		HashMap<String,Double> weightMap = new  HashMap<String,Double>();
		double totalRequiredMips = 0.0;
		
		Iterator<Entry<String, List<Double>>> iter = mipsMapRequested.entrySet().iterator();
		while (iter.hasNext()){
			Entry<String, List<Double>> entry = iter.next();
			
			//each element of the iterator is one entry of the table: (vmId, mipsShare)
			String vmId = entry.getKey();
			List<Double> shares = entry.getValue();
			
			//count amount of mips required by the vm
			double requiredMipsByThisVm = 0.0;
			for(double share: shares){
				totalRequiredMips+=share;
				requiredMipsByThisVm+=share;
			}
			
			//store the value to use later to define weights
			weightMap.put(vmId, requiredMipsByThisVm);
		}
			
		//now, we have the information on individual weights
		//use this information to define actual shares of
		//mips received by each VM
				
		iter = mipsMapRequested.entrySet().iterator();
		while (iter.hasNext()){
			Entry<String, List<Double>> entry = iter.next();
			
			//each element of the iterator is one entry of the table: (vmId, mipsShare)
			String vmId = entry.getKey();
			List<Double> shares = entry.getValue();
			
			//get weight of this vm
			double vmWeight = weightMap.get(vmId)/totalRequiredMips;
			
			//update actual received share
			LinkedList<Double> updatedSharesList = new LinkedList<Double>();
			double actuallyAllocatedMips = 0.0;
			for(double share: shares){
				double updatedShare = share*vmWeight;
				
				//update share if migrating
				if (getVmsMigratingOut().contains(vmId)) {
					updatedShare *= 0.9; // performance degradation due to migration = 10% MIPS
				} else if (getVmsMigratingIn().contains(vmId)) {
					updatedShare *= 0.1; // performance cost incurred by the destination host = 90% MIPS
				}
				
				actuallyAllocatedMips+=updatedShare;
				updatedSharesList.add(updatedShare);
			}
			
			//add in the new map
			getMipsMap().put(vmId, updatedSharesList);
			setAvailableMips(getAvailableMips() - actuallyAllocatedMips);
		}	
	}

	/**
	 * Update allocation of VMs on PEs.
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
							Log.printLine("There is no enough MIPS (" + mips + ") to accommodate VM " + vmUid);
							System.exit(0);
						}
						pe = peIterator.next();
						peProvisioner = pe.getPeProvisioner();
						availableMips = peProvisioner.getAvailableMips();
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cloudsim.VmScheduler#deallocatePesForVm(cloudsim.Vm)
	 */
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
	 * Sets the pes in use.
	 * 
	 * @param pesInUse the new pes in use
	 */
	protected void setPesInUse(int pesInUse) {
		this.pesInUse = pesInUse;
	}

	/**
	 * Gets the pes in use.
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
