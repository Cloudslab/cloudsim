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
 * RamProvisioner is an abstract class that represents the provisioning policy of memory to virtual
 * machines inside a Host. When extending this class, care must be taken to guarantee that the field
 * availableMemory will always contain the amount of free memory available for future allocations.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public abstract class RamProvisioner {

	/** The ram. */
	private int ram;

	/** The available ram. */
	private int availableRam;

	/**
	 * Creates the new RamProvisioner.
	 * 
	 * @param ram the ram
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
	 * @param vm virtual machine for which the RAM are being allocated
	 * @param ram the RAM
	 * 
	 * @return $true if the RAM could be allocated; $false otherwise
	 * 
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocateRamForVm(Vm vm, int ram);

	/**
	 * Gets the allocated RAM for VM.
	 * 
	 * @param vm the VM
	 * 
	 * @return the allocated RAM for vm
	 */
	public abstract int getAllocatedRamForVm(Vm vm);

	/**
	 * Releases BW used by a VM.
	 * 
	 * @param vm the vm
	 * 
	 * @pre $none
	 * @post none
	 */
	public abstract void deallocateRamForVm(Vm vm);

	/**
	 * Releases BW used by a all VMs.
	 * 
	 * @pre $none
	 * @post none
	 */
	public void deallocateRamForAllVms() {
		setAvailableRam(getRam());
	}

	/**
	 * Checks if is suitable for vm.
	 * 
	 * @param vm the vm
	 * @param ram the ram
	 * 
	 * @return true, if is suitable for vm
	 */
	public abstract boolean isSuitableForVm(Vm vm, int ram);

	/**
	 * Gets the ram.
	 * 
	 * @return the ram
	 */
	public int getRam() {
		return ram;
	}

	/**
	 * Sets the ram.
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
	 * @return available ram
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
