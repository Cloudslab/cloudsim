/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Parallel and Distributed Systems such as Clusters and Clouds Licence: GPL -
 * http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2008, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.lists.PowerVmList;

// TODO: rewrite this as a child of PolicyMigrationAbstract, remove doubleThresholdAbstract, single
// threshold and policy simple

/**
 * PowerVmAllocationPolicySingleThresholdFixed is an VMAllocationPolicy that chooses, as the host
 * for a VM, the host with the least power increase due to utilization increase.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 4.3
 * @invariant $none
 */
public class PowerVmAllocationPolicySingleThresholdFixed extends
		PowerVmAllocationPolicyDoubleThresholdAbstract {

	/**
	 * Instantiates a new vM provisioner mpp.
	 * 
	 * @param list the list
	 * @param utilizationThreshold the utilization bound
	 */
	public PowerVmAllocationPolicySingleThresholdFixed(
			List<? extends PowerHost> list,
			double utilizationThreshold) {
		super(list, 0, utilizationThreshold);
	}

	/**
	 * Optimize allocation of the VMs according to current utilization.
	 * 
	 * @param vmList the vm list
	 * @param utilizationThreshold the utilization bound
	 * 
	 * @return the array list< hash map< string, object>>
	 */
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		List<Map<String, Object>> migrationMap = new ArrayList<Map<String, Object>>();
		if (vmList.isEmpty()) {
			return migrationMap;
		}
		saveAllocation(vmList);
		List<Vm> vmsToRestore = new ArrayList<Vm>();
		vmsToRestore.addAll(vmList);

		List<Vm> vmTmpList = new ArrayList<Vm>();
		vmTmpList.addAll(vmList);
		PowerVmList.sortByCpuUtilization(vmTmpList);

		for (PowerHost host : this.<PowerHost> getHostList()) {
			// host.vmDestroyAll();
			host.reallocateMigratingInVms();
		}

		for (Vm vm : vmTmpList) {
			// Log.printLine("VM #" + vm.getId() + " utilization: " + String.format("%.2f;",
			// vm.getTotalUtilization(CloudSim.clock()) * 100));
			if (vm.isInMigration()) {
				continue;
			}

			/**
			 * TODO: some VMs are still not migrated to the destination but, but migration takes
			 * long time and before the end new optimization happens. Some new Vms are allocated to
			 * that host, because there is no knowledge that some VMs are being migrated to that
			 * host. Therefore, some allocations fail.
			 */
			PowerHost oldHost = (PowerHost) getVmTable().get(vm.getUid());
			// if (oldHost.getId() == 375 && vm.getId() == 383) {
			// Log.printLine("found");
			// }
			// PowerHost oldHost = vm.getHost();
			PowerHost allocatedHost = findHostForVm(vm);
			// if (allocatedHost.getId() == 235 && vm.getId() == 383) {
			// Log.printLine("found");
			// }
			if (allocatedHost != null) {
				// if (vm.getId() == 60 && allocatedHost.getId() == 51) {
				// Log.printLine(vm.getId());
				// }
				Log.printLine("VM #" + vm.getId() + " allocated to host #" + allocatedHost.getId());
				allocatedHost.vmCreate(vm);

				if (allocatedHost.getId() != oldHost.getId()) {
					Map<String, Object> migrate = new HashMap<String, Object>();
					migrate.put("vm", vm);
					migrate.put("host", allocatedHost);
					migrationMap.add(migrate);
					// getVmTable().put(vm.getUid(), allocatedHost);

					// vmsToRestore.remove(vm);
					// Log.printLine("Skip restore of VM #" + vm.getId());
				}
			}
		}

		restoreAllocation(vmsToRestore, getHostList());

		return migrationMap;
	}

}
