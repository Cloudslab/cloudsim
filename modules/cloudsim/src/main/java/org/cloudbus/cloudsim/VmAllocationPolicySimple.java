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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
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
	 * The map key is a VM UID and the value is the allocated host for that VM.
	 */
	@Getter @Setter(AccessLevel.PROTECTED)
	private Map<String, HostEntity> guestTable;

	/** The map between each VM and the number of Pes used. 
         * The map key is a VM UID and the value is the number of used Pes for that VM. */
	@Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
	private Map<String, Integer> usedPes;

	/** The number of free Pes for each host from {@link #getHostList() }. */
	@Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
	private List<Integer> freePes;

	/**
	 * Creates a new VmAllocationPolicySimple object.
	 * 
	 * @param list the list of hosts
	 * @pre $none
	 * @post $none
	 */
	public VmAllocationPolicySimple(List<? extends HostEntity> list) {
		super(list);
		setFreePes(new ArrayList<>());
		for (HostEntity host : getHostList()) {
			getFreePes().add(host.getNumberOfPes());
		}
		setGuestTable(new HashMap<>());
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

		if (!getGuestTable().containsKey(guest.getUid())) { // if this vm was not created
			if (guest.getHost() != null) {
                return allocateHostForGuest(guest, guest.getHost());
			}

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
				if (allocateHostForGuest(guest, host)) { //guest is successfully created in the host
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
		String datacenterName = host.getDatacenter().getName();

		if (host == null) {
			Log.printlnConcat(CloudSim.clock()+": "+datacenterName+".vmAllocator: No suitable host found for "+guest.getClassName()+" #" + guest.getId());
			return false;
		}

		if (host == guest) { // cannot be hosted on itself (VmAbstract edge-case)
			Log.printlnConcat(CloudSim.clock()+": "+datacenterName+".vmAllocator: Allocation of "+guest.getClassName()+" #"+guest.getId()+" to "+host.getClassName()+" #"+host.getId()+" failed (cannot be allocated on itself)");
			return false;
		}

		if (host.isBeingInstantiated()){ // cannot be hosted by an unallocated host (VmAbstract edge-case)
			Log.printlnConcat(CloudSim.clock()+": "+datacenterName+".vmAllocator: Allocation of "+guest.getClassName()+" #"+guest.getId()+" to "+host.getClassName()+" #"+host.getId()+" failed because the host entity is not instantiated");
			return false;
		}

		if (host.guestCreate(guest)) { // if vm has been successfully created in the host
			getGuestTable().put(guest.getUid(), host);

			int requiredPes = guest.getNumberOfPes();
			int idx = getHostList().indexOf(host);
			getUsedPes().put(guest.getUid(), requiredPes);
			getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
			Log.printlnConcat(CloudSim.clock()+": "+datacenterName+".vmAllocator: .vmAllocator]: "+guest.getClassName()+" #" + guest.getId() + " has been allocated to "+host.getClassName()+" #" + host.getId());
			return true;
		}

		return false;
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends GuestEntity> vmList) { return null; }

	@Override
	public void deallocateHostForGuest(GuestEntity guest) {
		HostEntity host = getGuestTable().remove(guest.getUid());
		int idx = getHostList().indexOf(host);
		int pes = getUsedPes().remove(guest.getUid());
		if (host != null) {
			host.guestDestroy(guest);
			getFreePes().set(idx, getFreePes().get(idx) + pes);
		}
	}

	@Override
	public HostEntity getHost(GuestEntity guest) {
		return getGuestTable().get(guest.getUid());
	}

	@Override
	public HostEntity getHost(int vmId, int userId) {
		return getGuestTable().get(Vm.getUid(userId, vmId));
	}
}
