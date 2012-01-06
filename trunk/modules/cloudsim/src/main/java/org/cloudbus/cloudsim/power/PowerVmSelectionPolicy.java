package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Vm;

/**
 * The Class PowerVmSelectionPolicy.
 * 
 * If you are using any algorithms, policies or workload included in the power package, please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience, ISSN: 1532-0626, Wiley
 * Press, New York, USA, 2011, DOI: 10.1002/cpe.1867
 * 
 * @author Anton Beloglazov
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
