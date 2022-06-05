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

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;

/**
 * The class of an abstract VM selection policy.
 * 
 * If you are using any algorithms, policies or workload included in the power package, please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 */
public abstract class PowerVmSelectionPolicy {

	/**
	 * Gets the vms to migrate.
	 * 
	 * @param host the host
	 * @param migrateMoreThanOne 
	 * @return the vms to migrate
	 */
	public abstract Vm getVmToMigrate(PowerHost host, boolean migrateMoreThanOne);

	/**
	 * Gets the migratable vms.
	 * 
	 * @param host the host
	 * @return the migratable vms
	 */
	protected List<PowerVm> getMigratableVms(PowerHost host) {
		List<PowerVm> migratableVms = new ArrayList<PowerVm>();
		//Log.printLine("In method to get the migratable VMS");
		//Log.printLine(host.getVmList());
		for (PowerVm vm : host.<PowerVm> getVmList()) {
			//Log.printLine("Looping over migratable VMs");
			//Log.printLine(vm.isInMigration());
			if (!vm.isInMigration()) {
				migratableVms.add(vm);
				//Log.printLine("We have added one");
			}
		}
		if(migratableVms.isEmpty()) {
			return null;
		}
		
		return migratableVms;
	}
	
	public static List<PowerVm> getMigratableVmsNew(PowerHost host) {
		List<PowerVm> migratableVms = new ArrayList<PowerVm>();
		//Log.printLine("In method to get the migratable VMS");
		//Log.printLine(host.getVmList());
		for (PowerVm vm : host.<PowerVm> getVmList()) {
			//Log.printLine("Looping over migratable VMs");
			//Log.printLine(vm.isInMigration());
			if (!vm.isInMigration()) {
				migratableVms.add(vm);
				//Log.printLine("We have added one");
			}
		}
		if(migratableVms.isEmpty()) {
			return null;
		}
		
		return migratableVms;
	}
	
	
	public void updateQValues(PowerHost host, PowerVm vm) {
		
	}
	
	public void reset() {
		
	}


}
