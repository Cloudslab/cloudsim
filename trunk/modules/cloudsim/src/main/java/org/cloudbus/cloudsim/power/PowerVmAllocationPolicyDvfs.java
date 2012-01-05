package org.cloudbus.cloudsim.power;

import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

/**
 * This a simple class representing a DVFS VM allocation policy. The actual application of DVFS
 * (adjustment of the host's power consumption) happens in the PowerDatacenter class.
 * 
 * @author Anton Beloglazov
 * @since Jan 5, 2012
 * 
 */
public class PowerVmAllocationPolicyDvfs extends PowerVmAllocationPolicyAbstract {

	/**
	 * Instantiates a new power vm allocation policy dvfs.
	 * 
	 * @param list the list
	 */
	public PowerVmAllocationPolicyDvfs(List<? extends Host> list) {
		super(list);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#optimizeAllocation(java.util.List)
	 */
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		// This policy doesn't optimize the VM allocation
		return null;
	}

}
