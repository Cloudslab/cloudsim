/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.container.resourceAllocators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;

/**
 * ContainerAllocationPolicy is an abstract class that represents the provisioning policy of vms to
 * ContainerContainerGoogle in a Datacentre.
 * 
 * @author Sareh Fotuhi Piraghaj
 * @author Remo Andreoli
 * @since CloudSim Toolkit 3.0
 */


public abstract class ContainerAllocationPolicy {
		/**
		 * The Vm list.
		 */
		private List<? extends HostEntity> containerVmList;

		/**
		 * Creates a new VmAllocationPolicy object.
		 *
		 * @param list Host entities available in a {@link ContainerDatacenter}
		 * @pre $none
		 * @post $none
		 */
		public ContainerAllocationPolicy(List<? extends HostEntity> list) {
			setHostList(list);
		}

		/**
		 * Allocates a host for a given guest entity.
		 *
		 * @param guest the guest entity to allocate a host to
		 * @return $true if the host could be allocated; $false otherwise
		 * @pre $none
		 * @post $none
		 */
		public abstract boolean allocateHostForGuest(GuestEntity guest);

		/**
		 * Allocates a specified host for a given guest entity.
		 *
		 * @param guest
		 * @param host  host entity which the guest is reserved to
		 * @return $true if the host could be allocated; $false otherwise
		 * @pre $none
		 * @post $none
		 */
		public abstract boolean allocateHostForGuest(GuestEntity guest, HostEntity host);

		/**
		 * Optimize allocation of the VMs according to current utilization.
		 *
		 //     * @param vmList           the vm list
		 //     * @param utilizationBound the utilization bound
		 //     * @param time             the time
		 * @return the array list< hash map< string, object>>
		 */
		public abstract List<Map<String, Object>> optimizeAllocation(List<? extends GuestEntity> containerList);

		/**
		 * Releases the host used by a VM.
		 *
		 * @param guest the container
		 * @pre $none
		 * @post $none
		 */
		public abstract void deallocateHostForGuest(GuestEntity guest);

		/**
		 * Find host for guest entity.
		 *
		 * @param guest the guest
		 * @return the host
		 */
		public HostEntity findHostForGuest(GuestEntity guest) {
			for (HostEntity host : getHostList()) {
				if (host.isSuitableForGuest(guest)) {
					return host;
				}
			}
			return null;
		}

		/**
		 * Get the host that is executing the given VM belonging to the given user.
		 *
		 * @param guest the container
		 * @return the Host with the given vmID and userID; $null if not found
		 * @pre $none
		 * @post $none
		 */
		public abstract HostEntity getHost(GuestEntity guest);

		/**
		 * Get the host that is executing the given VM belonging to the given user.
		 *
		 * @param containerId the vm id
		 * @param userId      the user id
		 * @return the Host with the given vmID and userID; $null if not found
		 * @pre $none
		 * @post $none
		 */
		public abstract HostEntity getHost(int containerId, int userId);

		/**
		 * Sets the host list.
		 *
		 * @param containerVmList the new host list
		 */
		protected void setHostList(List<? extends HostEntity> containerVmList) {
			this.containerVmList = containerVmList;
		}

		/**
		 * Gets the host list.
		 *
		 * @return the host list
		 */
		@SuppressWarnings("unchecked")
		public <T extends HostEntity> List<T> getHostList() {
			return (List<T>) this.containerVmList;
		}

	}
