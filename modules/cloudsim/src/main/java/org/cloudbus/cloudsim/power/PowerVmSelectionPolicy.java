package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Vm;

/**
 * The Class PowerVmSelectionPolicy.
 */
public abstract class PowerVmSelectionPolicy {

	/**
	 * Gets the vms to migrate.
	 * 
	 * @param host the host
	 * @return the vms to migrate
	 */
	public abstract Vm getVmToMigrate(PowerHost host);

	/**
	 * Gets the migratable vms.
	 * 
	 * @param host the host
	 * @return the migratable vms
	 */
	protected List<PowerVm> getMigratableVms(PowerHost host) {
		List<PowerVm> migratableVms = new ArrayList<PowerVm>();
		for (PowerVm vm : host.<PowerVm> getVmList()) {
			if (!vm.isInMigration()) {
				migratableVms.add(vm);
			}
		}
		return migratableVms;
	}

}
