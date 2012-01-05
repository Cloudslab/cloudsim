package org.cloudbus.cloudsim.power;

import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Vm;

/**
 * The Class PowerVmSelectionPolicyRandomSelection.
 */
public class PowerVmSelectionPolicyRandomSelection extends PowerVmSelectionPolicy {

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
		int index = (new Random()).nextInt(migratableVms.size());
		return migratableVms.get(index);
	}

}
