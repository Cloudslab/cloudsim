/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Parallel and Distributed Systems such as Clusters and Clouds Licence: GPL -
 * http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2008, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * PowerVmAllocationPolicySingleThresholdFixed is an VMAllocationPolicy that chooses, as the host
 * for a VM, the host with the least power increase due to utilization increase.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 4.3
 * @invariant $none
 */
public class PowerVmAllocationPolicySimple extends VmAllocationPolicy {

	/** The vm table. */
	private Map<String, Host> vmTable;

	/**
	 * Instantiates a new PowerVmAllocationPolicySimple.
	 * 
	 * @param hostList the host list
	 */
	public PowerVmAllocationPolicySimple(List<? extends Host> hostList) {
		super(hostList);
		setVmTable(new HashMap<String, Host>());
	}

	/**
	 * Allocates a host for a given VM.
	 * 
	 * @param vm VM specification
	 * 
	 * @return $true if the host could be allocated; $false otherwise
	 */
	@Override
	public boolean allocateHostForVm(Vm vm) {
		return allocateHostForVm(vm, findHostForVm(vm));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Vm,
	 * org.cloudbus.cloudsim.Host)
	 */
	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		if (host == null) {
			Log.formatLine("%.2f: No suitable host found for VM #" + vm.getId() + "\n", CloudSim.clock());
			return false;
		}
		if (host.vmCreate(vm)) { // if vm has been succesfully created in the host
			getVmTable().put(vm.getUid(), host);
			Log.formatLine(
					"%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(),
					CloudSim.clock());
			return true;
		}
		Log.formatLine(
				"%.2f: Creation of VM #" + vm.getId() + " on the host #" + host.getId() + " failed\n",
				CloudSim.clock());
		return false;
	}

	/**
	 * Determines a host to allocate for the VM.
	 * 
	 * @param vm the vm
	 * 
	 * @return the host
	 */
	public PowerHost findHostForVm(Vm vm) {
		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (host.isSuitableForVm(vm)) {
				return host;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#optimizeAllocation(java.util.List)
	 */
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		// This policy doesn't optimize the VM allocation
		return null;
	}

	/**
	 * Releases the host used by a VM.
	 * 
	 * @param vm the vm
	 */
	@Override
	public void deallocateHostForVm(Vm vm) {
		Host host = getVmTable().remove(vm.getUid());
		if (host != null) {
			host.vmDestroy(vm);
		}
	}

	/**
	 * Gets the host that is executing the given VM belonging to the given user.
	 * 
	 * @param vm the vm
	 * 
	 * @return the Host with the given vmID and userID; $null if not found
	 */
	@Override
	public Host getHost(Vm vm) {
		return getVmTable().get(vm.getUid());
	}

	/**
	 * Gets the host that is executing the given VM belonging to the given user.
	 * 
	 * @param vmId the vm id
	 * @param userId the user id
	 * 
	 * @return the Host with the given vmID and userID; $null if not found
	 */
	@Override
	public Host getHost(int vmId, int userId) {
		return getVmTable().get(Vm.getUid(userId, vmId));
	}

	/**
	 * Sets the vm table.
	 * 
	 * @param vmTable the vm table
	 */
	public void setVmTable(Map<String, Host> vmTable) {
		this.vmTable = vmTable;
	}

	/**
	 * Gets the vm table.
	 * 
	 * @return the vm table
	 */
	public Map<String, Host> getVmTable() {
		return vmTable;
	}

}
