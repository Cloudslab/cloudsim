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
 * RamProvisioner is an abstract class that represents the provisioning policy used by a host
 * to allocate memory to virtual machines inside it. 
 * Each host has to have its own instance of a RamProvisioner.
 * When extending this class, care must be taken to guarantee that the field
 * availableMemory will always contain the amount of free memory available for future allocations.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public abstract class RamProvisioner {

	/** The total ram capacity from the host that the provisioner can allocate to VMs. */
	private int ram;

	/** The available ram. */
	private int availableRam;

	/**
	 * Creates the new RamProvisioner.
	 * 
	 * @param ram The total ram capacity from the host that the provisioner can allocate to VMs.
	 * 
	 * @pre ram>=0
	 * @post $none
	 */
	public RamProvisioner(int ram) {
		setRam(ram);
		setAvailableRam(ram);
	}

	/**
	 * Allocates RAM for a given VM.
	 * 
	 * @param vm the virtual machine for which the RAM is being allocated
	 * @param ram the RAM to be allocated to the VM
	 * 
	 * @return $true if the RAM could be allocated; $false otherwise
	 * 
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocateRamForVm(Vm vm, int ram);

	/**
	 * Gets the allocated RAM for a given VM.
	 * 
	 * @param vm the VM
	 * 
	 * @return the allocated RAM for the vm
	 */
	public abstract int getAllocatedRamForVm(Vm vm);

	/**
	 * Releases RAM used by a VM.
	 * 
	 * @param vm the vm
	 * 
	 * @pre $none
	 * @post none
	 */
	public abstract void deallocateRamForVm(Vm vm);

	/**
	 * Releases RAM used by all VMs.
	 * 
	 * @pre $none
	 * @post none
	 */
	public void deallocateRamForAllVms() {
		setAvailableRam(getRam());
	}

	/**
	 * Checks if it is possible to change the current allocated RAM for the VM
         * to a new amount, depending on the available RAM.
	 * 
	 * @param vm the vm to check if there is enough available RAM on the host to 
         * change the VM allocated RAM
	 * @param ram the new total amount of RAM for the VM.
	 * 
	 * @return true, if is suitable for vm
	 */
	public abstract boolean isSuitableForVm(Vm vm, int ram);

	/**
	 * Gets the ram capacity.
	 * 
	 * @return the ram
	 */
	public int getRam() {
		return ram;
	}

	/**
	 * Sets the ram capacity.
	 * 
	 * @param ram the ram to set
	 */
	protected void setRam(int ram) {
		this.ram = ram;
	}

	/**
	 * Gets the amount of used RAM in the host.
	 * 
	 * @return used ram
	 * 
	 * @pre $none
	 * @post $none
	 */
	public int getUsedRam() {
		return ram - availableRam;
	}

	/**
	 * Gets the available RAM in the host.
	 * 
	 * @return th available ram
	 * 
	 * @pre $none
	 * @post $none
	 */
	public int getAvailableRam() {
		return availableRam;
	}

	/**
	 * Sets the available ram.
	 * 
	 * @param availableRam the availableRam to set
	 */
	protected void setAvailableRam(int availableRam) {
		this.availableRam = availableRam;
	}

}
