package org.cloudbus.cloudsim.power;

import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

/**
 * This a simple class representing a simple VM allocation policy that does not perform any
 * optimization of the VM allocation.
 * 
 * @author Anton Beloglazov
 * @since Jan 5, 2012
 * 
 */
public class PowerVmAllocationPolicySimple extends PowerVmAllocationPolicyAbstract {

	/**
	 * Instantiates a new power vm allocation policy simple.
	 * 
	 * @param list the list
	 */
	public PowerVmAllocationPolicySimple(List<? extends Host> list) {
		super(list);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#optimizeAllocation(java.util.List)
	 */
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		// This policy does not optimize the VM allocation
		return null;
	}

}
