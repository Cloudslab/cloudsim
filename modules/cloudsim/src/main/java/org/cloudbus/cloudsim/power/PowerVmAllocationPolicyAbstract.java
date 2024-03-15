/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
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
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;

/**
 * An abstract power-aware VM allocation policy.
 * 
 * <br/>If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:<br/>
 * 
 * <ul>
 * <li><a href="http://dx.doi.org/10.1002/cpe.1867">Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley &amp; Sons, Ltd, New York, USA, 2012</a>
 * </ul>
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 */
public abstract class PowerVmAllocationPolicyAbstract extends VmAllocationPolicy {

	/** The map map where each key is a VM id and
         * each value is the host where the VM is placed. */
	private final Map<String, Host> vmTable = new HashMap<>();

	/**
	 * Instantiates a new PowerVmAllocationPolicyAbstract.
	 * 
	 * @param list the list
	 */
	public PowerVmAllocationPolicyAbstract(List<? extends Host> list) {
		super(list);
	}

	@Override
	public boolean allocateHostForGuest(GuestEntity guest) {
		return allocateHostForGuest(guest, findHostForVm(guest));
	}

	public boolean allocateHostForGuest(GuestEntity guest, HostEntity host) {
		if (host == null) {
			Log.formatLine("%.2f: No suitable host found for VM #" + guest.getId() + "\n", CloudSim.clock());
			return false;
		}
		if (host.guestCreate(guest)) { // if vm has been succesfully created in the host
			getVmTable().put(guest.getUid(), (Host) host); // TODO: Remo Andreoli: change all to HostEntity
			Log.formatLine(
					"%.2f: VM #" + guest.getId() + " has been allocated to the host #" + host.getId(),
					CloudSim.clock());
			return true;
		}
		Log.formatLine(
				"%.2f: Creation of VM #" + guest.getId() + " on the host #" + host.getId() + " failed\n",
				CloudSim.clock());
		return false;
	}

	/**
	 * Finds the first host that has enough resources to host a given VM.
	 * 
	 * @param vm the vm to find a host for it
	 * @return the first host found that can host the VM
	 */
	public PowerHost findHostForVm(GuestEntity vm) {
		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (host.isSuitableForGuest(vm)) {
				return host;
			}
		}
		return null;
	}

	@Override
	public void deallocateHostForGuest(GuestEntity guest) {
		Host host = getVmTable().remove(guest.getUid());
		if (host != null) {
			host.guestDestroy(guest);
		}
	}

	@Override
	public HostEntity getHost(GuestEntity guest) {
		return getVmTable().get(guest.getUid());
	}

	@Override
	public HostEntity getHost(int vmId, int userId) {
		return getVmTable().get(Vm.getUid(userId, vmId));
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
