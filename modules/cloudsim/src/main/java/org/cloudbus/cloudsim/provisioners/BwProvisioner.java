/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.provisioners;

import org.cloudbus.cloudsim.Vm;

/**
 * BwProvisioner is an abstract class that represents the provisioning policy used by a host
 * to allocate bandwidth (bw) to virtual machines inside it. 
 * Each host has to have its own instance of a BwProvisioner.
 * When extending this class, care must be taken to guarantee that
 * the field availableBw will always contain the amount of free bandwidth available for future
 * allocations.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public abstract class BwProvisioner {

	/** The total bandwidth capacity from the host that the provisioner can allocate to VMs. */
	private long bw;

	/** The available bandwidth. */
	private long availableBw;

	/**
	 * Creates the new BwProvisioner.
	 * 
	 * @param bw The total bandwidth capacity from the host that the provisioner can allocate to VMs.
	 * 
	 * @pre bw >= 0
	 * @post $none
	 */
	public BwProvisioner(long bw) {
		setBw(bw);
		setAvailableBw(bw);
	}

	/**
	 * Allocates BW for a given VM.
	 * 
	 * @param vm the virtual machine for which the bw are being allocated
	 * @param bw the bw to be allocated to the VM
	 * 
	 * @return $true if the bw could be allocated; $false otherwise
	 * 
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocateBwForVm(Vm vm, long bw);

	/**
	 * Gets the allocated BW for VM.
	 * 
	 * @param vm the VM
	 * 
	 * @return the allocated BW for vm
	 */
	public abstract long getAllocatedBwForVm(Vm vm);

	/**
	 * Releases BW used by a VM.
	 * 
	 * @param vm the vm
	 * 
	 * @pre $none
	 * @post none
	 */
	public abstract void deallocateBwForVm(Vm vm);

	/**
	 * Releases BW used by all VMs.
	 * 
	 * @pre $none
	 * @post none
	 */
	public void deallocateBwForAllVms() {
		setAvailableBw(getBw());
	}

	/**
	 * Checks if it is possible to change the current allocated BW for the VM
         * to a new amount, depending on the available BW.
	 * 
	 * @param vm the vm to check if there is enough available BW on the host to 
         * change the VM allocated BW
	 * @param bw the new total amount of BW for the VM.
	 * 
	 * @return true, if is suitable for vm
	 */
	public abstract boolean isSuitableForVm(Vm vm, long bw);

	/**
	 * Gets the bw capacity.
	 * 
	 * @return the bw capacity
	 */
	public long getBw() {
		return bw;
	}

	/**
	 * Sets the bw capacity.
	 * 
	 * @param bw the new bw capacity
	 */
	protected void setBw(long bw) {
		this.bw = bw;
	}

	/**
	 * Gets the available BW in the host.
	 * 
	 * @return available bw
	 * 
	 * @pre $none
	 * @post $none
	 */
	public long getAvailableBw() {
		return availableBw;
	}

	/**
	 * Gets the amount of used BW in the host.
	 * 
	 * @return used bw
	 * 
	 * @pre $none
	 * @post $none
	 */
	public long getUsedBw() {
		return bw - availableBw;
	}

	/**
	 * Sets the available bw.
	 * 
	 * @param availableBw the new available bw
	 */
	protected void setAvailableBw(long availableBw) {
		this.availableBw = availableBw;
	}

}
