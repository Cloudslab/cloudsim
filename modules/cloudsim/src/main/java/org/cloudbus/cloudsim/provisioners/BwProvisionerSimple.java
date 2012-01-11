/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.provisioners;

import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.Vm;

/**
 * BwProvisionerSimple is a class that implements a simple best effort allocation policy: if there
 * is bw available to request, it allocates; otherwise, it fails.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class BwProvisionerSimple extends BwProvisioner {

	/** The bw table. */
	private Map<String, Long> bwTable;

	/**
	 * Instantiates a new bw provisioner simple.
	 * 
	 * @param bw the bw
	 */
	public BwProvisionerSimple(long bw) {
		super(bw);
		setBwTable(new HashMap<String, Long>());
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.BwProvisioner#allocateBwForVm(cloudsim.Vm, long)
	 */
	@Override
	public boolean allocateBwForVm(Vm vm, long bw) {
		deallocateBwForVm(vm);

		if (getAvailableBw() >= bw) {
			setAvailableBw(getAvailableBw() - bw);
			getBwTable().put(vm.getUid(), bw);
			vm.setCurrentAllocatedBw(getAllocatedBwForVm(vm));
			return true;
		}

		vm.setCurrentAllocatedBw(getAllocatedBwForVm(vm));
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.BwProvisioner#getAllocatedBwForVm(cloudsim.Vm)
	 */
	@Override
	public long getAllocatedBwForVm(Vm vm) {
		if (getBwTable().containsKey(vm.getUid())) {
			return getBwTable().get(vm.getUid());
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.BwProvisioner#deallocateBwForVm(cloudsim.Vm)
	 */
	@Override
	public void deallocateBwForVm(Vm vm) {
		if (getBwTable().containsKey(vm.getUid())) {
			long amountFreed = getBwTable().remove(vm.getUid());
			setAvailableBw(getAvailableBw() + amountFreed);
			vm.setCurrentAllocatedBw(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.BwProvisioner#deallocateBwForVm(cloudsim.Vm)
	 */
	@Override
	public void deallocateBwForAllVms() {
		super.deallocateBwForAllVms();
		getBwTable().clear();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * gridsim.virtualization.power.provisioners.BWProvisioner#isSuitableForVm(gridsim.virtualization
	 * .power.VM, long)
	 */
	@Override
	public boolean isSuitableForVm(Vm vm, long bw) {
		long allocatedBw = getAllocatedBwForVm(vm);
		boolean result = allocateBwForVm(vm, bw);
		deallocateBwForVm(vm);
		if (allocatedBw > 0) {
			allocateBwForVm(vm, allocatedBw);
		}
		return result;
	}

	/**
	 * Gets the bw table.
	 * 
	 * @return the bw table
	 */
	protected Map<String, Long> getBwTable() {
		return bwTable;
	}

	/**
	 * Sets the bw table.
	 * 
	 * @param bwTable the bw table
	 */
	protected void setBwTable(Map<String, Long> bwTable) {
		this.bwTable = bwTable;
	}

}
