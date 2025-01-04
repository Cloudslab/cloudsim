/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.provisioners;

import java.util.List;
import org.cloudbus.cloudsim.DatacenterCharacteristics;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.GuestEntity;

/**
/**
 * PeProvisioner is an abstract class that represents the provisioning policy used by a host
 * to allocate its PEs to virtual machines inside it. It gets a physical PE
 * and manage it in order to provide this PE as virtual PEs for VMs.
 * In that way, a given PE might be shared among different VMs.
 * Each host's PE has to have its own instance of a PeProvisioner.
 * When extending this class, care must be taken to guarantee that the field
 * availableMips will always contain the amount of free mips available for future allocations.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public abstract class PeProvisioner {

	/** The total mips capacity of the PE that the provisioner can allocate to VMs. */
	private double mips;

	/** The available mips. */
	private double availableMips;

	/**
	 * Creates a new PeProvisioner.
	 * 
	 * @param mips The total mips capacity of the PE that the provisioner can allocate to VMs
	 * 
	 * @pre mips>=0
	 * @post $none
	 */
	public PeProvisioner(double mips) {
		setMips(mips);
		setAvailableMips(mips);
	}

	/**
	 * Allocates a new virtual PE with a specific capacity for a given VM.
	 * The virtual PE to be added will use the total or partial mips capacity
	 * of the physical PE.
	 *
	 * @param guest the virtual machine for which the new virtual PE is being allocated
	 * @param mips  the mips to be allocated to the virtual PE of the given VM
	 * @return $true if the virtual PE could be allocated; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocateMipsForGuest(GuestEntity guest, double mips);

	@Deprecated
	public boolean allocateMipsForVm(Vm vm, double mips) { return allocateMipsForGuest(vm, mips); }

	/**
	 * Allocates a new virtual PE with a specific capacity for a given VM id.
	 * 
	 * @param vmUid the virtual machine for which the new virtual PE is being allocated
	 * @param mips the mips to be allocated to the virtual PE of the given VM
	 * 
	 * @return $true if the virtual PE could be allocated; $false otherwise
	 * @pre $none
	 * @post $none
         * @see #allocateMipsForGuest(GuestEntity, double)
	 */
	public abstract boolean allocateMipsForGuest(String vmUid, double mips);

	@Deprecated
	public boolean allocateMipsForVm(String vmUid, double mips) { return allocateMipsForGuest(vmUid, mips);}

	/**
	 * Allocates a new set of virtual PE with a specific capacity, replacing the old set (if any), for a given VM.
	 *
	 * @param guest the virtual machine for which the new virtual PE is being allocated
	 * @param mips  the list of mips capacity of each virtual PE to be allocated to the VM
	 * @return $true if the set of virtual PEs could be allocated; $false otherwise
	 * @pre $none
	 * @post $none
	 * //@TODO In this case, each PE can have a different capacity, what
	 * in many places this situation is not considered, such as
	 * in the {@link Vm}, {@link Pe} and {@link DatacenterCharacteristics}
	 * classes.
	 */
	public abstract boolean allocateMipsForGuest(GuestEntity guest, List<Double> mips);

	@Deprecated
	public boolean allocateMipsForVm(Vm vm, List<Double> mips) { return allocateMipsForGuest(vm, mips); }

	/**
	 * Gets the list of allocated virtual PEs' MIPS for a given VM.
	 *
	 * @param guest the virtual machine the get the list of allocated virtual PEs' MIPS
	 * @return list of allocated virtual PEs' MIPS
	 * @pre $none
	 * @post $none
	 */
	public abstract List<Double> getAllocatedMipsForGuest(GuestEntity guest);

	@Deprecated
	public List<Double> getAllocatedMipsForVm(Vm vm) { return getAllocatedMipsForGuest(vm); }

	/**
	 * Gets total allocated MIPS for a given VM for all PEs.
	 *
	 * @param guest the virtual machine the get the total allocated MIPS capacity
	 * @return total allocated MIPS
	 * @pre $none
	 * @post $none
	 */
	public abstract double getTotalAllocatedMipsForGuest(GuestEntity guest);

	@Deprecated
	public double getTotalAllocatedMipsForVm(Vm vm) { return getTotalAllocatedMipsForGuest(vm); }

	/**
	 * Gets the MIPS capacity of a virtual Pe allocated to a given VM.
	 *
	 * @param guest virtual machine to get a given virtual PE capacity
	 * @param peId  the virtual pe id
	 * @return allocated MIPS for the virtual PE
	 * @pre $none
	 * @post $none
	 */
	public abstract double getAllocatedMipsForGuestByVirtualPeId(GuestEntity guest, int peId);

	@Deprecated
	public double getAllocatedMipsForVmByVirtualPeId(Vm vm, int peId) { return getAllocatedMipsForGuestByVirtualPeId(vm, peId); }

	/**
	 * Releases all virtual PEs allocated to a given VM.
	 *
	 * @param guest the vm
	 * @pre $none
	 * @post none
	 */
	public abstract void deallocateMipsForGuest(GuestEntity guest);

	@Deprecated
	public void deallocateMipsForVm(Vm vm) { deallocateMipsForGuest(vm);}

	/**
	 * Releases all virtual PEs allocated to all VMs.
	 * 
	 * @pre $none
	 * @post none
	 */
	public void deallocateMipsForAllGuests() {
		setAvailableMips(getMips());
	}

	@Deprecated
	public void deallocateMipsForAllVms() { deallocateMipsForAllGuests(); }

	/**
	 * Gets the MIPS.
	 * 
	 * @return the MIPS
	 */
	public double getMips() {
		return mips;
	}

	/**
	 * Sets the MIPS.
	 * 
	 * @param mips the MIPS to set
	 */
	public void setMips(double mips) {
		this.mips = mips;
	}

	/**
	 * Gets the available MIPS in the PE.
	 * 
	 * @return available MIPS
	 * 
	 * @pre $none
	 * @post $none
	 */
	public double getAvailableMips() {
		return availableMips;
	}

	/**
	 * Sets the available MIPS in the PE.
	 * 
	 * @param availableMips the availableMips to set
	 */
	protected void setAvailableMips(double availableMips) {
		this.availableMips = availableMips;
	}

	/**
	 * Gets the total allocated MIPS.
	 * 
	 * @return the total allocated MIPS
	 */
	public double getTotalAllocatedMips() {
		double totalAllocatedMips = getMips() - getAvailableMips();
		if (totalAllocatedMips > 0) {
			return totalAllocatedMips;
		}
		return 0;
	}

	/**
	 * Gets the utilization of the Pe in percents.
	 * 
	 * @return the utilization
	 */
	public double getUtilization() {
		return getTotalAllocatedMips() / getMips();
	}

}
