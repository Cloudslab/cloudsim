/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;

/**
 * VmAllocationPolicySimple is an VmAllocationPolicy that chooses, as the host for a VM, the host
 * with less PEs in use. It is therefore a Worst Fit policy, allocating VMs into the 
 * host with most available PE.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 */
public class VmAllocationPolicySimple extends VmAllocationPolicy {

	/** The map between each VM and its allocated host.
         * The map key is a VM UID and the value is the allocated host for that VM. */
	private Map<String, HostEntity> vmTable;

	/** The map between each VM and the number of Pes used. 
         * The map key is a VM UID and the value is the number of used Pes for that VM. */
	private Map<String, Integer> usedPes;

	/** The number of free Pes for each host from {@link #getHostList() }. */
	private List<Integer> freePes;

	/**
	 * Creates a new VmAllocationPolicySimple object.
	 * 
	 * @param list the list of hosts
	 * @pre $none
	 * @post $none
	 */
	public VmAllocationPolicySimple(List<? extends Host> list) {
		super(list);
		setFreePes(new ArrayList<>());
		for (HostEntity host : getHostList()) {
			getFreePes().add(host.getNumberOfPes());
		}
		setVmTable(new HashMap<>());
		setUsedPes(new HashMap<>());
	}

	/**
     * Allocates the host with less PEs in use for a given VM.
     *
     * @param guest {@inheritDoc}
     * @return {@inheritDoc}
     * @pre $none
     * @post $none
     */
	@Override
	public boolean allocateHostForGuest(GuestEntity guest) {
		int tries = 0;
		List<Integer> freePesTmp = new ArrayList<>(getFreePes());

		if (!getVmTable().containsKey(guest.getUid())) { // if this vm was not created
			do {// we still trying until we find a host or until we try all of them
				int moreFree = Integer.MIN_VALUE;
				int idx = -1;

				// we want the host with less pes in use
				for (int i = 0; i < freePesTmp.size(); i++) {
					if (freePesTmp.get(i) > moreFree) {
						moreFree = freePesTmp.get(i);
						idx = i;
					}
				}

				HostEntity host = getHostList().get(idx);
				if (allocateHostForGuest(guest, host)) { // if vm were succesfully created in the host
					freePesTmp.clear();
					return true;
				} else {
					freePesTmp.set(idx, Integer.MIN_VALUE);
				}
				tries++;
			} while (tries < getFreePes().size());

		}

		freePesTmp.clear();
		return false;
	}

	@Override
	public boolean allocateHostForGuest(GuestEntity guest, HostEntity host) {
		if (host.guestCreate(guest)) { // if vm has been succesfully created in the host
			getVmTable().put(guest.getUid(), host);

			int requiredPes = guest.getNumberOfPes();
			int idx = getHostList().indexOf(host);
			getUsedPes().put(guest.getUid(), requiredPes);
			getFreePes().set(idx, getFreePes().get(idx) - requiredPes);

			Log.formatLine(
					"%.2f: VM #" + guest.getId() + " has been allocated to the host #" + host.getId(),
					CloudSim.clock());
			return true;
		}

		return false;
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends GuestEntity> vmList) { return null; }

	@Override
	public void deallocateHostForGuest(GuestEntity guest) {
		HostEntity host = getVmTable().remove(guest.getUid());
		int idx = getHostList().indexOf(host);
		int pes = getUsedPes().remove(guest.getUid());
		if (host != null) {
			host.guestDestroy(guest);
			getFreePes().set(idx, getFreePes().get(idx) + pes);
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
	public Map<String, HostEntity> getVmTable() {
		return vmTable;
	}

	/**
	 * Sets the vm table.
	 * 
	 * @param vmTable the vm table
	 */
	protected void setVmTable(Map<String, HostEntity> vmTable) {
		this.vmTable = vmTable;
	}

	/**
	 * Gets the used pes.
	 * 
	 * @return the used pes
	 */
	protected Map<String, Integer> getUsedPes() {
		return usedPes;
	}

	/**
	 * Sets the used pes.
	 * 
	 * @param usedPes the used pes
	 */
	protected void setUsedPes(Map<String, Integer> usedPes) {
		this.usedPes = usedPes;
	}

	/**
	 * Gets the free pes.
	 * 
	 * @return the free pes
	 */
	protected List<Integer> getFreePes() {
		return freePes;
	}

	/**
	 * Sets the free pes.
	 * 
	 * @param freePes the new free pes
	 */
	protected void setFreePes(List<Integer> freePes) {
		this.freePes = freePes;
	}
}
