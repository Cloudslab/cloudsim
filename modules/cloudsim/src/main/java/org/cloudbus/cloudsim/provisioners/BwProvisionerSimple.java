/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.provisioners;

import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.core.GuestEntity;

/**
 * BwProvisionerSimple is an extension of {@link BwProvisioner} which uses a best-effort policy to
 * allocate bandwidth (bw) to VMs: 
 * if there is available bw on the host, it allocates; otherwise, it fails. 
 * Each host has to have its own instance of a RamProvisioner.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class BwProvisionerSimple extends BwProvisioner {

	/** The BW map, where each key is a VM id and each value
         * is the amount of BW allocated to that VM. */
	private Map<String, Long> bwTable;

	/**
	 * Instantiates a new bw provisioner simple.
	 * 
	 * @param bw The total bw capacity from the host that the provisioner can allocate to VMs. 
	 */
	public BwProvisionerSimple(long bw) {
		super(bw);
		setBwTable(new HashMap<>());
	}

	@Override
	public boolean allocateBwForGuest(GuestEntity guest, long bw) {
		long old_bw = getAllocatedBwForGuest(guest);

		if (getAvailableBw() + old_bw >= bw) {
			setAvailableBw(getAvailableBw() + old_bw - bw);
			bwTable.put(guest.getUid(), bw);
			guest.setCurrentAllocatedBw(bw);
			return true;
		}

		return false;
	}

	@Override
	public long getAllocatedBwForGuest(GuestEntity guest) {
		Long bw = bwTable.get(guest.getUid());
		if (bw != null)
			return bw;
		else
			return 0;
	}

	@Override
	public void deallocateBwForGuest(GuestEntity guest) {
		Long allocatedBw = bwTable.remove(guest.getUid());
		if (allocatedBw != null)
			setAvailableBw(getAvailableBw() + allocatedBw);
		guest.setCurrentAllocatedBw(0);
	}

	@Override
	public void deallocateBwForAllGuests() {
		super.deallocateBwForAllGuests();
		getBwTable().clear();
	}

	@Override
	public boolean isSuitableForGuest(GuestEntity guest, long bw) {
		long allocatedBw = getAllocatedBwForGuest(guest);
		boolean result = allocateBwForGuest(guest, bw);
		deallocateBwForGuest(guest);
		if (allocatedBw > 0) {
			allocateBwForGuest(guest, allocatedBw);
		}
		return result;
	}

	/**
	 * Gets the map between VMs and allocated bw.
	 * 
	 * @return the bw map
	 */
	protected Map<String, Long> getBwTable() {
		return bwTable;
	}

	/**
	 * Sets the map between VMs and allocated bw.
	 * 
	 * @param bwTable the bw map
	 */
	protected void setBwTable(Map<String, Long> bwTable) {
		this.bwTable = bwTable;
	}

}
