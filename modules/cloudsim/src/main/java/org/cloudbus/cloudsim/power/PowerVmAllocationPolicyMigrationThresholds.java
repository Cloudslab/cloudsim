package org.cloudbus.cloudsim.power;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

/**
 * The Class PowerVmAllocationPolicyMigrationThresholds.
 */
public class PowerVmAllocationPolicyMigrationThresholds extends PowerVmAllocationPolicyMigrationAbstract {

	/**
	 * Instantiates a new power vm allocation policy migration mad.
	 * 
	 * @param hostList the host list
	 * @param vmSelectionPolicy the vm selection policy
	 * @param utilizationThreshold the utilization threshold
	 */
	public PowerVmAllocationPolicyMigrationThresholds(
			List<? extends Host> hostList,
			PowerVmSelectionPolicy vmSelectionPolicy,
			double utilizationThreshold) {
		super(hostList, vmSelectionPolicy, utilizationThreshold);
	}

	/**
	 * Checks if is host over utilized.
	 * 
	 * @param _host the _host
	 * @return true, if is host over utilized
	 */
	@Override
	protected boolean isHostOverUtilized(PowerHost host) {
		addHistoryEntry(host, getUtilizationThreshold());
		double totalRequestedMips = 0;
		for (Vm vm : host.getVmList()) {
			totalRequestedMips += vm.getCurrentRequestedTotalMips();
		}
		double utilization = totalRequestedMips / host.getTotalMips();
		return utilization > getUtilizationThreshold();
	}

}
