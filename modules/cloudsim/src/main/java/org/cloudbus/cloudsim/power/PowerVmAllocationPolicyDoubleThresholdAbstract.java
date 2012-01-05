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

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * The Class PowerVmAllocationPolicyDoubleThresholdAbstract.
 */
public class PowerVmAllocationPolicyDoubleThresholdAbstract extends VmAllocationPolicySimple {

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

	/** The utilization threshold lower. */
	private double utilizationThresholdLower;

	/** The utilization threshold upper. */
	private double utilizationThresholdUpper;

	/**
	 * Instantiates a new vM provisioner mpp.
	 * 
	 * @param list the list
	 * @param utilizationThresholdLower the utilization threshold lower
	 * @param utilizationThresholdUpper the utilization threshold upper
	 */
	public PowerVmAllocationPolicyDoubleThresholdAbstract(
			List<? extends PowerHost> list,
			double utilizationThresholdLower,
			double utilizationThresholdUpper) {
		super(list);
		setSavedAllocation(new ArrayList<Map<String, Object>>());
		setUtilizationThresholdLower(utilizationThresholdLower);
		setUtilizationThresholdUpper(utilizationThresholdUpper);
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

		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (host.isSuitableForVm(vm)) {
				if (getMaxUtilizationAfterAllocation(host, vm) > getUtilizationThresholdUpper()) {
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
		PowerHost allocatedHost = findHostForVm(vm);
		if (allocatedHost != null && allocatedHost.vmCreate(vm)) { // if vm has been succesfully
																	// created in the host
			getVmTable().put(vm.getUid(), allocatedHost);
			Log.formatLine(
					"%.2f: VM #" + vm.getId() + " has been allocated to the host #" + allocatedHost.getId()
							+ "\n",
					CloudSim.clock());
			return true;
		}
		return false;
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
			PowerHost host = (PowerHost) getVmTable().remove(vm.getUid());
			if (host != null) {
				host.vmDestroy(vm);
			}
		}
	}

	/**
	 * Optimize allocation of the VMs according to current utilization.
	 * 
	 * @param vmList the vm list
	 * 
	 * @return the array list< hash map< string, object>>
	 */
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		Log.printLine("optimizeAllocation() method must be overriden by a child class");
		return null;
	}

	/**
	 * Save allocation.
	 * 
	 * @param vmList the vm list
	 */
	protected void saveAllocation(List<? extends Vm> vmList) {
		getSavedAllocation().clear();
		for (Vm vm : vmList) {
			Map<String, Object> map = new HashMap<String, Object>();
			// Log.printLine("Trying to save VM #" + vm.getId());
			map.put("vm", vm);
			map.put("host", vm.getHost());
			getSavedAllocation().add(map);
			// we do not destroy current allocation
			// vm.getHost().vmDestroy(vm);
			// getVmTable().remove(vm.getUid());
		}
	}

	/**
	 * Restore allocation.
	 * 
	 * @param vmsToRestore the vms to restore
	 * @param list
	 */
	protected void restoreAllocation(List<Vm> vmsToRestore, List<Host> hostList) {
		// getVmTable().clear();
		for (Host host : hostList) {
			host.vmDestroyAll();
			host.reallocateMigratingInVms();
		}
		/*
		 * for (Map<String, Object> map : getSavedAllocation()) { //Vm vm = (Vm) map.get("vm");
		 * PowerHost host = (PowerHost) map.get("host"); //PowerHost host = (PowerHost)
		 * vm.getHost(); if (host != null) { host.vmDestroyAll(); // vm.getHost().vmDestroy(vm); }
		 * //PowerHost host = (PowerHost) map.get("host"); //host.vmDestroy(vm);
		 * //host.vmDestroyAll(); }
		 */
		for (Map<String, Object> map : getSavedAllocation()) {
			Vm vm = (Vm) map.get("vm");
			PowerHost host = (PowerHost) map.get("host");
			// Log.printLine("Trying to restore VM #" + vm.getId() + " on host #" + host.getId());
			if (!vmsToRestore.contains(vm)) {
				// Log.printLine("VM #" + vm.getVmId() +
				// " skipped because it's not in the toRestore list");
				continue;
			}
			if (!host.vmCreate(vm)) {
				Log.printLine("failed");
			}
			getVmTable().put(vm.getUid(), host);
			Log.printLine("Restored VM #" + vm.getId() + " on host #" + host.getId());

			// host.vmDestroy(vm);
			// if (!host.vmCreate(vm)) {
			// host.vmCreateBestEffort(vm);
			// }
			// getVmTable().put(vm.getUid(), host);
		}
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
	 * Sets the utilization threshold lower.
	 * 
	 * @param utilizationThresholdLower the new utilization threshold lower
	 */
	public void setUtilizationThresholdLower(double utilizationThresholdLower) {
		this.utilizationThresholdLower = utilizationThresholdLower;
	}

	/**
	 * Gets the utilization threshold lower.
	 * 
	 * @return the utilization threshold lower
	 */
	public double getUtilizationThresholdLower() {
		return utilizationThresholdLower;
	}

	/**
	 * Sets the utilization threshold upper.
	 * 
	 * @param utilizationThresholdUpper the new utilization threshold upper
	 */
	public void setUtilizationThresholdUpper(double utilizationThresholdUpper) {
		this.utilizationThresholdUpper = utilizationThresholdUpper;
	}

	/**
	 * Gets the utilization threshold upper.
	 * 
	 * @return the utilization threshold upper
	 */
	public double getUtilizationThresholdUpper() {
		return utilizationThresholdUpper;
	}

}
