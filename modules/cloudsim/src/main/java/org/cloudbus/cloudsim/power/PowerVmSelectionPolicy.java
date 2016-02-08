/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Vm;

/**
 * An abstract VM selection policy used to select VMs from a list of migratable VMs.
 * The selection is defined by sub classes.
 * 
 * <br/>If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:<br/>
 * 
 * <ul>
 * <li><a href="http://dx.doi.org/10.1002/cpe.1867">Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012</a>
 * </ul>
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 */
public abstract class PowerVmSelectionPolicy {

	/**
	 * Gets a VM to migrate from a given host.
	 * 
	 * @param host the host
	 * @return the vm to migrate
	 */
	public abstract Vm getVmToMigrate(PowerHost host);

	/**
	 * Gets the list of migratable VMs from a given host.
	 * 
	 * @param host the host
	 * @return the list of migratable VMs
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
