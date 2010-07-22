/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Parallel and Distributed Systems such as Clusters and Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2008, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.predicates.PredicateType;

/**
 * CloudSim Datacentre class is a CloudResource whose hostList
 * are virtualized. It deals with processing of VM queries (i.e., handling
 * of VMs) instead of processing Cloudlet-related queries. So, even though an
 * AllocPolicy will be instantiated (in the init() method of the superclass,
 * it will not be used, as processing of cloudlets are handled by the CloudletScheduler
 * and processing of VirtualMachines are handled by the VMAllocationPolicy.
 *
 * @author       Rodrigo N. Calheiros
 * @since        CloudSim Toolkit 4.3
 * @invariant $none
 */
public class PowerDatacenterNonPowerAware extends PowerDatacenter {

	/**
	 * Instantiates a new datacenter.
	 *
	 * @param name the name
	 * @param characteristics the res config
	 * @param schedulingInterval the scheduling interval
	 * @param utilizationBound the utilization bound
	 * @param vmAllocationPolicy the vm provisioner
	 * @param storageList the storage list
	 *
	 * @throws Exception the exception
	 */
	public PowerDatacenterNonPowerAware(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
	}

	/**
	 * Updates processing of each cloudlet running in this PowerDatacenter. It is necessary because
	 * Hosts and VirtualMachines are simple objects, not entities. So, they don't receive events
	 * and updating cloudlets inside them must be called from the outside.
	 *
	 * @pre $none
	 * @post $none
	 */
	@Override
	protected void updateCloudletProcessing() {
//		if (isInMigration()) {
//			CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
//			schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
//			return;
//		}
		if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
			CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
			schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			return;
		}
		double currentTime = CloudSim.clock();
		double timeframePower = 0.0;

		if (currentTime > getLastProcessTime()) {
			double timeDiff = currentTime - getLastProcessTime();
			double minTime = Double.MAX_VALUE;

			Log.printLine("\n");

			for (PowerHost host : this.<PowerHost>getHostList()) {
				if (!Log.isDisabled()) {
					Log.print(String.format("%.2f: Host #%d\n", CloudSim.clock(), host.getId()));
				}

				double hostPower = 0.0;

				try {
					//hostPower = host.getPower() * timeDiff;
					hostPower = host.getMaxPower() * timeDiff;
					timeframePower += hostPower;
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (!Log.isDisabled()) {
					Log.print(String.format("%.2f: Host #%d utilization is %.2f%%\n", CloudSim.clock(), host.getId(), host.getUtilizationOfCpu() * 100));
					Log.print(String.format("%.2f: Host #%d energy is %.2f W*sec\n", CloudSim.clock(), host.getId(), hostPower));
				}
			}

			if (!Log.isDisabled()) {
				Log.print(String.format("\n%.2f: Consumed energy is %.2f W*sec\n\n", CloudSim.clock(), timeframePower));
			}

			Log.printLine("\n\n--------------------------------------------------------------\n\n");

			for (PowerHost host : this.<PowerHost>getHostList()) {
				if (!Log.isDisabled()) {
					Log.print(String.format("\n%.2f: Host #%d\n", CloudSim.clock(), host.getId()));
				}

				double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing
				if (time < minTime) {
					minTime = time;
				}
			}

			setPower(getPower() + timeframePower);

			/** Remove completed VMs **/
			for (PowerHost host : this.<PowerHost>getHostList()) {
				for (Vm vm : host.getCompletedVms()) {
					getVmAllocationPolicy().deallocateHostForVm(vm);
					getVmList().remove(vm);
					Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
				}
			}

			Log.printLine();

			if (!isDisableMigrations()) {
				List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocation(getVmList());

				for (Map<String, Object> migrate : migrationMap) {
					Vm vm = (Vm) migrate.get("vm");
					PowerHost targetHost = (PowerHost) migrate.get("host");
					PowerHost oldHost = (PowerHost) vm.getHost();

					targetHost.addMigratingInVm(vm);

					if (!Log.isDisabled()) {
						if (oldHost == null) {
							Log.print(String.format("%.2f: Migration of VM #%d to Host #%d is started\n", CloudSim.clock(), vm.getId(), targetHost.getId()));
						} else {
							Log.print(String.format("%.2f: Migration of VM #%d from Host #%d to Host #%d is started\n", CloudSim.clock(), vm.getId(), oldHost.getId(), targetHost.getId()));
						}
					}

					incrementMigrationCount();

					vm.setInMigration(true);

					/** VM migration delay = RAM / bandwidth + C    (C = 10 sec) **/
					send(getId(), (double) vm.getRam() / (vm.getBw() / 8000) + 10, CloudSimTags.VM_MIGRATE, migrate);
				}
			}

			// schedules an event to the next time
			if (minTime != Double.MAX_VALUE) {
//				if (minTime > currentTime + 0.01 && minTime < getSchedulingInterval()) {
//					send(getId(), minTime - currentTime, CloudSimTags.VM_DATACENTER_EVENT);
//				} else {
					CloudSim.cancelAll(getId(), CloudSim.SIM_ANY);
					send(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
//				}
			}

			setLastProcessTime(currentTime);
		}
	}

}
