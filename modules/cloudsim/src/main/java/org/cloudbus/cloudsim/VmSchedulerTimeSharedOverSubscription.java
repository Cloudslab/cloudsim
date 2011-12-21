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
import java.util.Map.Entry;

/**
 * The Class VmSchedulerTimeSharedOverSubscription.
 */
public class VmSchedulerTimeSharedOverSubscription extends VmSchedulerTimeShared {

	/**
	 * Instantiates a new vm scheduler time shared over subscription.
	 *
	 * @param pelist the pelist
	 */
	public VmSchedulerTimeSharedOverSubscription(List<? extends Pe> pelist) {
		super(pelist);
	}

	/**
	 * Allocate pes for vm. The policy allow over-subscription. In other words,
	 * the policy still allows the allocation of VMs that require more CPU capacity
	 * that is available. Oversubscription results in performance degradation. Each
	 * virtual PE cannot be allocated more CPU capacity than MIPS of a single PE.
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
		for (Double mips : mipsShareRequested) {
			totalRequestedMips += mips;
		}
		
		if (getVmsMigratingIn().contains(vmUid)) {
			totalRequestedMips *= 0.1; // performance cost incurred by the destination host = 10% MIPS
		}

		double peMips = getPeCapacity();
		List<Double> mipsShareAllocated = new ArrayList<Double>();
		for (Double mipsRequested : mipsShareRequested) {
			if (mipsRequested > peMips) {
				mipsRequested = peMips; // Over-subscription is implemented by the cost of performance degradation
			}
			if (getVmsMigratingOut().contains(vmUid)) {
				mipsRequested *= 0.9; // performance degradation due to migration = 10% MIPS
			} else if (getVmsMigratingIn().contains(vmUid)) {
				mipsRequested *= 0.1; // performance cost incurred by the destination host = 10% MIPS
			}
			mipsShareAllocated.add(mipsRequested);
		}

		if (getAvailableMips() >= totalRequestedMips) {
			getMipsMap().put(vmUid, mipsShareAllocated);
			setAvailableMips(getAvailableMips() - totalRequestedMips);
		} else {
			int pesSkipped = 0;
			for (Entry<String, List<Double>> entry : getMipsMap().entrySet()) {
				List<Double> mipsMap = entry.getValue();
				if (getVmsMigratingIn().contains(entry.getKey())) {
					pesSkipped += mipsMap.size();
					continue;
				}				 
				for (int i = 0; i < mipsMap.size(); i++) {
					if (mipsMap.get(i) == 0) {
						pesSkipped++;
					}
				}
			}
			
			if (getVmsMigratingIn().contains(vmUid)) {
				pesSkipped += mipsShareRequested.size();
			}	

			updateShortage();
		}

		return true;
	}

}
