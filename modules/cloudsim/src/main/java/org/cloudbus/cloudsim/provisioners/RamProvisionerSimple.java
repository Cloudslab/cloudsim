/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.provisioners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.GuestEntity;

/**
 * RamProvisionerSimple is an extension of {@link RamProvisioner} which uses a best-effort policy to
 * allocate memory to VMs: if there is available ram on the host, it allocates; otherwise, it fails. 
 * Each host has to have its own instance of a RamProvisioner.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class RamProvisionerSimple extends RamProvisioner {

	/** The RAM map, where each key is a VM id and each value
         * is the amount of RAM allocated to that VM. */
	private Map<String, Integer> ramTable;

	/**
	 * Instantiates a new ram provisioner simple.
	 * 
	 * @param availableRam The total ram capacity from the host that the provisioner can allocate to VMs. 
	 */
	public RamProvisionerSimple(int availableRam) {
		super(availableRam);
		setRamTable(new HashMap<>());
	}

	@Override
	public boolean allocateRamForGuest(GuestEntity guest, int ram) {
		int maxRam = guest.getRam();
                /* If the requested amount of RAM to be allocated to the VM is greater than
                the amount of VM is in fact requiring, allocate only the
                amount defined in the Vm requirements.*/
		if (ram >= maxRam) {
			ram = maxRam;
		}

		deallocateRamForGuest(guest);

		if (getAvailableRam() >= ram) {
			setAvailableRam(getAvailableRam() - ram);
			getRamTable().put(guest.getUid(), ram);
			guest.setCurrentAllocatedRam(getAllocatedRamForGuest(guest));
			return true;
		}

		guest.setCurrentAllocatedRam(getAllocatedRamForGuest(guest));

		return false;
	}

	@Override
	public int getAllocatedRamForGuest(GuestEntity guest) {
		if (getRamTable().containsKey(guest.getUid())) {
			return getRamTable().get(guest.getUid());
		}
		return 0;
	}

	@Override
	public void deallocateRamForGuest(GuestEntity guest) {
		int allocatedRam = getAllocatedRamForGuest(guest);
		if (allocatedRam > 0) {
			int amountFreed = getRamTable().remove(guest.getUid());
			setAvailableRam(getAvailableRam() + amountFreed);
			guest.setCurrentAllocatedRam(0);
		}
	}

	@Override
	public void deallocateRamForAllGuests() {
		super.deallocateRamForAllGuests();
		getRamTable().clear();
	}

	@Override
	public boolean isSuitableForGuest(GuestEntity guest, int ram) {
		int allocatedRam = getAllocatedRamForGuest(guest);
		boolean result = allocateRamForGuest(guest, ram);
		deallocateRamForGuest(guest);
		if (allocatedRam > 0) {
			allocateRamForGuest(guest, allocatedRam);
		}
		return result;
	}

	/**
	 * Gets the map between VMs and allocated ram.
	 * 
	 * @return the ram map
	 */
	protected Map<String, Integer> getRamTable() {
		return ramTable;
	}

	/**
	 * Sets the map between VMs and allocated ram.
	 * 
	 * @param ramTable the ram map
	 */
	protected void setRamTable(Map<String, Integer> ramTable) {
		this.ramTable = ramTable;
	}

}
