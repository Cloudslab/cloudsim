/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Parallel and Distributed Systems such as Clusters and Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2008, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.lists.PowerVmList;

/**
 * PowerVmAllocationPolicySingleThreshold is an VMAllocationPolicy that
 * chooses, as the host for a VM, the host with
 * the least power increase due to utilization
 * increase.
 *
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 4.3
 * @invariant $none
 */
public class PowerVmAllocationPolicySingleThreshold extends VmAllocationPolicySimple {

	/** The hosts PEs. */
	protected Map<String, Integer> hostsPes;

	/** The hosts utilization. */
	protected Map<Integer, Double> hostsUtilization;

	/** The hosts memory. */
	protected Map<Integer, Integer> hostsRam;

	/** The hosts bw. */
	protected Map<Integer, Long> hostsBw;

	/** The old allocation. */
	private List<Map<String, Object>> savedAllocation;

	/** The utilization threshold. */
	private double utilizationThreshold;

	/**
	 * Instantiates a new vM provisioner mpp.
	 *
	 * @param list the list
	 * @param utilizationThreshold the utilization bound
	 */
	public PowerVmAllocationPolicySingleThreshold(List<? extends PowerHost> list, double utilizationThreshold) {
		super(list);
		setSavedAllocation(new ArrayList<Map<String,Object>>());
		setUtilizationThreshold(utilizationThreshold);
	}

	/**
	 * Determines a host to allocate for the VM.
	 *
	 * @param vm the vm
	 *
	 * @return the host
	 */
	public PowerHost findHostForVm(Vm vm) {
		double minPower = Double.MAX_VALUE;
		PowerHost allocatedHost = null;

		for (PowerHost host : this.<PowerHost>getHostList()) {
			if (host.isSuitableForVm(vm)) {
				double maxUtilization = getMaxUtilizationAfterAllocation(host, vm);
				if ((!vm.isRecentlyCreated() && maxUtilization > getUtilizationThreshold()) ||
						(vm.isRecentlyCreated() && maxUtilization > 1.0)) {
					continue;
				}
				try {
					double powerAfterAllocation = getPowerAfterAllocation(host, vm);
					if (powerAfterAllocation != -1) {
						double powerDiff = powerAfterAllocation - host.getPower();
						if (powerDiff < minPower) {
							minPower = powerDiff;
							allocatedHost = host;
						}
					}
				} catch (Exception e) {
				}
			}
		}

		return allocatedHost;
	}

	/**
	 * Allocates a host for a given VM.
	 *
	 * @param vm VM specification
	 *
	 * @return $true if the host could be allocated; $false otherwise
	 *
	 * @pre $none
	 * @post $none
	 */
	@Override
	public boolean allocateHostForVm(Vm vm) {
		boolean result = false;

		PowerHost allocatedHost = findHostForVm(vm);

		try {
			result = allocatedHost.vmCreate(vm);
		} catch (Exception e) {
			result = false;
		}

		if (result) { //if vm has been succesfully created in the host
			getVmTable().put(vm.getUid(), allocatedHost);
			if (!Log.isDisabled()) {
				Log.print(String.format("%.2f: VM #" + vm.getId() + " has been sent to the host #" + allocatedHost.getId() + "\n", CloudSim.clock()));
			}
		}

		return result;
	}

	/**
	 * Releases the host used by a VM.
	 *
	 * @param vm the vm
	 *
	 * @pre $none
	 * @post none
	 */
	@Override
	public void deallocateHostForVm(Vm vm) {
		if (getVmTable().containsKey(vm.getUid())) {
			Host host = getVmTable().remove(vm.getUid());
			if (host != null) {
				host.vmDestroy(vm);
			}
		}
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

		for (Vm vm : vmTmpList) {
			//Log.printLine("VM #" + vm.getId() + " utilization: " + String.format("%.2f;", vm.getTotalUtilization(CloudSim.clock()) * 100));
			if (vm.isRecentlyCreated()) {
				continue;
			}

			PowerHost oldHost = (PowerHost) getVmTable().get(vm.getUid());
			//PowerHost oldHost = vm.getHost();
			PowerHost allocatedHost = findHostForVm(vm);
			if (allocatedHost != null) {
				Log.printLine("VM #" + vm.getId() + " allocated to host #" + allocatedHost.getId());
				allocatedHost.vmCreate(vm);

				if (!allocatedHost.equals(oldHost)) {
					Map<String, Object> migrate = new HashMap<String, Object>();
					migrate.put("vm", vm);
					migrate.put("host", allocatedHost);
					migrationMap.add(migrate);
					getVmTable().put(vm.getUid(), allocatedHost);

					vmsToRestore.remove(vm);
					//Log.printLine("Skip restore of VM #" + vm.getVmId());
				}
			}
		}

		restoreAllocation(vmsToRestore);

		return migrationMap;
	}

	/**
	 * Save allocation.
	 *
	 * @param vmList the vm list
	 */
	protected void saveAllocation(List<? extends Vm> vmList) {
		for (Vm vm : vmList) {
			if (vm != null) {
				Map<String, Object> map = new HashMap<String, Object>();
				//Log.printLine("Trying to save VM #" + vm.getId());
				map.put("vm", vm);
				map.put("host", vm.getHost());
				getSavedAllocation().add(map);
				vm.getHost().vmDestroy(vm);
				//getVmTable().remove(vm.getUid());
			}
		}
	}

	/**
	 * Restore allocation.
	 *
	 * @param vmsToRestore the vms to restore
	 */
	protected void restoreAllocation(List<Vm> vmsToRestore) {
		for (Map<String, Object> map : getSavedAllocation()) {
			Vm vm = (Vm) map.get("vm");
			PowerHost host = (PowerHost) vm.getHost();
			if (host != null) {
				vm.getHost().vmDestroy(vm);
			}
			//PowerHost host = (PowerHost) map.get("host");
			//host.vmDestroy(vm);
			//host.vmDestroyAll();
		}
		for (Map<String, Object> map : getSavedAllocation()) {
			Vm vm = (Vm) map.get("vm");
			PowerHost host = (PowerHost) map.get("host");
			//Log.printLine("Trying to restore VM #" + vm.getVmId() + " on host #" + host.getMachineID());
			if (!vmsToRestore.contains(vm)) {
				//Log.printLine("VM #" + vm.getVmId() + " skipped because it's not in the toRestore list");
				continue;
			}
			//host.vmDestroy(vm);
//			if (!host.vmCreate(vm)) {
//				host.vmCreateBestEffort(vm);
//			}
			//host.vmCreateBestEffort(vm);
			host.vmCreate(vm);
			getVmTable().put(vm.getUid(), host);
		}
		setSavedAllocation(new ArrayList<Map<String, Object>>());
	}

	/**
	 * Gets the power after allocation.
	 *
	 * @param host the host
	 * @param vm the vm
	 *
	 * @return the power after allocation
	 */
	protected double getPowerAfterAllocation(PowerHost host, Vm vm) {
		List<Double> allocatedMipsForVm = null;
		PowerHost allocatedHost = (PowerHost) vm.getHost();

		if (allocatedHost != null) {
			allocatedMipsForVm = allocatedHost.getAllocatedMipsForVm(vm);
		}

		if (!host.allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
			return -1;
		}

		double power = host.getPower();

		host.deallocatePesForVm(vm);

		if (allocatedHost != null && allocatedMipsForVm != null) {
			vm.getHost().allocatePesForVm(vm, allocatedMipsForVm);
		}

		return power;
	}

	/**
	 * Gets the power after allocation.
	 *
	 * @param host the host
	 * @param vm the vm
	 *
	 * @return the power after allocation
	 */
	protected double getMaxUtilizationAfterAllocation(PowerHost host, Vm vm) {
		List<Double> allocatedMipsForVm = null;
		PowerHost allocatedHost = (PowerHost) vm.getHost();

		if (allocatedHost != null) {
			allocatedMipsForVm = vm.getHost().getAllocatedMipsForVm(vm);
		}

		if (!host.allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
			return -1;
		}

		double maxUtilization = host.getMaxUtilizationAmongVmsPes(vm);

		host.deallocatePesForVm(vm);

		if (allocatedHost != null && allocatedMipsForVm != null) {
			vm.getHost().allocatePesForVm(vm, allocatedMipsForVm);
		}

		return maxUtilization;
	}

	/**
	 * Gets the saved allocation.
	 *
	 * @return the saved allocation
	 */
	protected List<Map<String, Object>> getSavedAllocation() {
		return savedAllocation;
	}

	/**
	 * Sets the saved allocation.
	 *
	 * @param savedAllocation the saved allocation
	 */
	protected void setSavedAllocation(List<Map<String, Object>> savedAllocation) {
		this.savedAllocation = savedAllocation;
	}

	/**
	 * Gets the utilization bound.
	 *
	 * @return the utilization bound
	 */
	protected double getUtilizationThreshold() {
		return utilizationThreshold;
	}

	/**
	 * Sets the utilization bound.
	 *
	 * @param utilizationThreshold the new utilization bound
	 */
	protected void setUtilizationThreshold(double utilizationThreshold) {
		this.utilizationThreshold = utilizationThreshold;
	}

}
