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

import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.ContainerVm;

/**
 * ContainerAllocationPolicy is an abstract class that represents the provisioning policy of vms to
 * ContainerContainerGoogle in a Datacentre.
 * 
 * @author Sareh Fotuhi Piraghaj
 * @since CloudSim Toolkit 3.0
 */


public abstract class ContainerAllocationPolicy {
		/**
		 * The Vm list.
		 */
		private List<? extends ContainerVm> containerVmList;

		/**
		 * Allocates a new VmAllocationPolicy object.
		 *
		 * @pre $none
		 * @post $none
		 */
		public ContainerAllocationPolicy() {
			setContainerVmList(new ArrayList<ContainerVm>());
		}

		/**
		 * Allocates a host for a given VM. The host to be allocated is the one that was already
		 * reserved.
		 *
		 * @param container virtual machine which the host is reserved to
		 * @return $true if the host could be allocated; $false otherwise
		 * @pre $none
		 * @post $none
		 */
		public abstract boolean allocateVmForContainer(Container container,List<ContainerVm> containerVmList);

		/**
		 * Allocates a specified host for a given VM.
		 *
		 * @param vm virtual machine which the host is reserved to
		 * @return $true if the host could be allocated; $false otherwise
		 * @pre $none
		 * @post $none
		 */
		public abstract boolean allocateVmForContainer(Container container, ContainerVm vm);

		/**
		 * Optimize allocation of the VMs according to current utilization.
		 *
		 //     * @param vmList           the vm list
		 //     * @param utilizationBound the utilization bound
		 //     * @param time             the time
		 * @return the array list< hash map< string, object>>
		 */
		public abstract List<Map<String, Object>> optimizeAllocation(List<? extends Container> containerList);

		/**
		 * Releases the host used by a VM.
		 *
		 * @param container the container
		 * @pre $none
		 * @post $none
		 */
		public abstract void deallocateVmForContainer(Container container);

		/**
		 * Get the host that is executing the given VM belonging to the given user.
		 *
		 * @param container the container
		 * @return the Host with the given vmID and userID; $null if not found
		 * @pre $none
		 * @post $none
		 */
		public abstract ContainerVm getContainerVm(Container container);

		/**
		 * Get the host that is executing the given VM belonging to the given user.
		 *
		 * @param containerId   the vm id
		 * @param userId the user id
		 * @return the Host with the given vmID and userID; $null if not found
		 * @pre $none
		 * @post $none
		 */
		public abstract ContainerVm getContainerVm(int containerId, int userId);

		/**
		 * Sets the host list.
		 *
		 * @param containerVmList the new host list
		 */
		protected void setContainerVmList(List<? extends ContainerVm> containerVmList) {
			this.containerVmList = containerVmList;
		}

		/**
		 * Gets the host list.
		 *
		 * @return the host list
		 */
		@SuppressWarnings("unchecked")
		public <T extends ContainerVm> List<T> getContainerVmList() {
			return (List<T>) this.containerVmList;
		}

	}



