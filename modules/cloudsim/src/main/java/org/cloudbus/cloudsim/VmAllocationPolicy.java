/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VmAllocationPolicy is an abstract class that represents the provisioning policy of hosts to
 * virtual machines in a Datacenter. It allocates hosts for placing VMs. 
 * It supports two-stage commit of reservation of hosts: first, we
 * reserve the host and, once committed by the user, it is effectively allocated to he/she.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 */
public abstract class VmAllocationPolicy {

	/** The host list. */
	private List<? extends HostEntity> hostList;

	/** The map between each guest and its allocated host.
	 * The map key is a guest UID and the value is the allocated host for that VM.
	 */
	@Getter @Setter(AccessLevel.PROTECTED)
	private Map<String, HostEntity> guestTable;

	/**
	 * Creates a new VmAllocationPolicy object.
	 * 
	 * @param list Machines available in a {@link Datacenter}
	 * @pre $none
	 * @post $none
	 */
	public VmAllocationPolicy(List<? extends HostEntity> list) {
		setHostList(list);
		setGuestTable(new HashMap<>());
	}

	/**
	 * Allocates a host for a given VM.
	 *
	 * @param guest the VM to allocate a host to
	 * @return $true if the host could be allocated; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean allocateHostForGuest(GuestEntity guest) {
		return allocateHostForGuest(guest, findHostForGuest(guest));
	}

	/**
	 * Allocates a specified host for a given VM.
	 *
	 * @param guest guest entity which the host entity is reserved to
	 * @param host  host entity to allocate the given guest entity
	 * @return $true if the host could be allocated; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean allocateHostForGuest(GuestEntity guest, HostEntity host) {
		String datacenterName = host.getDatacenter().getName();

		if (host == guest) { // cannot be hosted on itself (VirtualEntity edge-case)
			Log.printlnConcat(CloudSim.clock()+": "+datacenterName+".vmAllocator: Allocation of "+guest.getClassName()+" #"+guest.getId()+" to "+host.getClassName()+" #"+host.getId()+" failed (cannot be allocated on itself)");
			return false;
		}

		if (host.isBeingInstantiated()){ // cannot be hosted by an unallocated host (VirtualEntity edge-case)
			Log.printlnConcat(CloudSim.clock()+": "+datacenterName+".vmAllocator: Allocation of "+guest.getClassName()+" #"+guest.getId()+" to "+host.getClassName()+" #"+host.getId()+" failed because the host entity is not instantiated");
			return false;
		}

		if (host.guestCreate(guest)) { // if vm has been successfully created in the host
			getGuestTable().put(guest.getUid(), host);
			Log.printlnConcat(CloudSim.clock()+": "+datacenterName+".vmAllocator: .vmAllocator]: "+guest.getClassName()+" #" + guest.getId() + " has been allocated to "+host.getClassName()+" #" + host.getId());
			return true;
		}

		return false;
	}

	/**
	 * Optimize allocation of the VMs according to current utilization.
	 * 
	 * @param vmList the vm list
	 * @return the array list< hash map< string, object>>
         * 
         * //TODO It returns a list of maps, where each map key is a string
         * and stores an object. What in fact are the keys and values of this
         * Map? Neither this class or its subclasses implement the method
         * or have clear documentation. The only sublcass is the {@link VmAllocationPolicySimple}. 
         * 
	 */
	public List<Map<String, Object>> optimizeAllocation(List<? extends GuestEntity> vmList) { return new ArrayList<>(); }

	/**
	 * Releases the host used by a VM.
	 *
	 * @param guest the vm to get its host released
	 * @pre $none
	 * @post $none
	 */
	public void deallocateHostForGuest(GuestEntity guest) {
		HostEntity host = getGuestTable().remove(guest.getUid());
		if (host != null) {
			host.guestDestroy(guest);
		}
	}

	/**
	 * Find host for guest entity.
	 *
	 * @param guest the guest
	 * @return the host
	 */
	public abstract HostEntity findHostForGuest(GuestEntity guest);

	/**
	 * Get the host that is executing the given VM.
	 *
	 * @param guest the vm
	 * @return the Host with the given vmID; $null if not found
	 * @pre $none
	 * @post $none
	 */
	public HostEntity getHost(GuestEntity guest) { return getGuestTable().get(guest.getUid()); }

	/**
	 * Get the host that is executing the given VM belonging to the given user.
	 *
	 * @param vmId   the vm id
	 * @param userId the user id
	 * @return the Host with the given vmID and userID; $null if not found
	 * @pre $none
	 * @post $none
	 */
	public HostEntity getHost(int vmId, int userId) { return getGuestTable().get(GuestEntity.getUid(userId, vmId)); };

	/**
	 * Sets the host list.
	 * 
	 * @param hostList the new host list
	 */
	protected void setHostList(List<? extends HostEntity> hostList) {
		this.hostList = hostList;
	}

	/**
	 * Gets the host list.
	 * 
	 * @return the host list
	 */
	@SuppressWarnings("unchecked")
	public <T extends HostEntity> List<T> getHostList() {
		return (List<T>) hostList;
	}

}
