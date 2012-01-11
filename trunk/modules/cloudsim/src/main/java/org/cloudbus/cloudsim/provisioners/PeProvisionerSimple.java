/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.provisioners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Vm;

/**
 * The Class PeProvisionerSimple.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PeProvisionerSimple extends PeProvisioner {

	/** The pe table. */
	private Map<String, List<Double>> peTable;

	/**
	 * Creates the PeProvisionerSimple object.
	 * 
	 * @param availableMips the available mips
	 * 
	 * @pre $none
	 * @post $none
	 */
	public PeProvisionerSimple(double availableMips) {
		super(availableMips);
		setPeTable(new HashMap<String, ArrayList<Double>>());
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.PeProvisioner#allocateMipsForVM(cloudsim.power.VM, int)
	 */
	@Override
	public boolean allocateMipsForVm(Vm vm, double mips) {
		return allocateMipsForVm(vm.getUid(), mips);
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.PeProvisioner#allocateMipsForVm(java.lang.String, double)
	 */
	@Override
	public boolean allocateMipsForVm(String vmUid, double mips) {
		if (getAvailableMips() < mips) {
			return false;
		}

		List<Double> allocatedMips;

		if (getPeTable().containsKey(vmUid)) {
			allocatedMips = getPeTable().get(vmUid);
		} else {
			allocatedMips = new ArrayList<Double>();
		}

		allocatedMips.add(mips);

		setAvailableMips(getAvailableMips() - mips);
		getPeTable().put(vmUid, allocatedMips);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.PeProvisioner#allocateMipsForVM(cloudsim.power.VM,
	 * java.util.ArrayList)
	 */
	@Override
	public boolean allocateMipsForVm(Vm vm, List<Double> mips) {
		int totalMipsToAllocate = 0;
		for (double _mips : mips) {
			totalMipsToAllocate += _mips;
		}

		if (getAvailableMips() + getTotalAllocatedMipsForVm(vm) < totalMipsToAllocate) {
			return false;
		}

		setAvailableMips(getAvailableMips() + getTotalAllocatedMipsForVm(vm) - totalMipsToAllocate);

		getPeTable().put(vm.getUid(), mips);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.PeProvisioner#deallocateMipsForAllVms()
	 */
	@Override
	public void deallocateMipsForAllVms() {
		super.deallocateMipsForAllVms();
		getPeTable().clear();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * cloudsim.provisioners.PeProvisioner#getAllocatedMipsForVMByVirtualPeId(cloudsim.power.VM,
	 * int)
	 */
	@Override
	public double getAllocatedMipsForVmByVirtualPeId(Vm vm, int peId) {
		if (getPeTable().containsKey(vm.getUid())) {
			try {
				return getPeTable().get(vm.getUid()).get(peId);
			} catch (Exception e) {
			}
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.PeProvisioner#getAllocatedMipsForVM(cloudsim.power.VM)
	 */
	@Override
	public List<Double> getAllocatedMipsForVm(Vm vm) {
		if (getPeTable().containsKey(vm.getUid())) {
			return getPeTable().get(vm.getUid());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.PeProvisioner#getTotalAllocatedMipsForVM(cloudsim.power.VM)
	 */
	@Override
	public double getTotalAllocatedMipsForVm(Vm vm) {
		if (getPeTable().containsKey(vm.getUid())) {
			double totalAllocatedMips = 0.0;
			for (double mips : getPeTable().get(vm.getUid())) {
				totalAllocatedMips += mips;
			}
			return totalAllocatedMips;
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.PeProvisioner#deallocateMipsForVM(cloudsim.power.VM)
	 */
	@Override
	public void deallocateMipsForVm(Vm vm) {
		if (getPeTable().containsKey(vm.getUid())) {
			for (double mips : getPeTable().get(vm.getUid())) {
				setAvailableMips(getAvailableMips() + mips);
			}
			getPeTable().remove(vm.getUid());
		}
	}

	/**
	 * Gets the pe table.
	 * 
	 * @return the peTable
	 */
	protected Map<String, List<Double>> getPeTable() {
		return peTable;
	}

	/**
	 * Sets the pe table.
	 * 
	 * @param peTable the peTable to set
	 */
	@SuppressWarnings("unchecked")
	protected void setPeTable(Map<String, ? extends List<Double>> peTable) {
		this.peTable = (Map<String, List<Double>>) peTable;
	}

}
