package org.cloudbus.cloudsim.power;

import java.util.List;

import org.cloudbus.cloudsim.Vm;

/**
 * The Class PowerVmSelectionPolicyMinimumMigrationTime.
 */
public class PowerVmSelectionPolicyMinimumMigrationTime extends PowerVmSelectionPolicy {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cloudbus.cloudsim.experiments.power.PowerVmSelectionPolicy#getVmsToMigrate(org.cloudbus
	 * .cloudsim.power.PowerHost)
	 */
	@Override
	public Vm getVmToMigrate(PowerHost host) {
		List<PowerVm> migratableVms = getMigratableVms(host);
		if (migratableVms.isEmpty()) {
			return null;
		}
		Vm vmToMigrate = null;
		double minMetric = Double.MAX_VALUE;
		for (Vm vm : migratableVms) {
			if (vm.isInMigration()) {
				continue;
			}
			double metric = vm.getRam();
			if (metric < minMetric) {
				minMetric = metric;
				vmToMigrate = vm;
			}
		}
		return vmToMigrate;
	}

}
