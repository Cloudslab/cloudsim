/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.provisioners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.GuestEntity;

/**
 * PeProvisionerSimple is an extension of {@link PeProvisioner} which uses a best-effort policy to
 * allocate virtual PEs to VMs: 
 * if there is available mips on the physical PE, it allocates to a virtual PE; otherwise, it fails. 
 * Each host's PE has to have its own instance of a PeProvisioner.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PeProvisionerSimple extends PeProvisioner {

	/** The PE map, where each key is a VM id and each value
         * is the list of in terms of their allocated amount of MIPS to that VM. */
	private Map<String, List<Double>> peTable;

	/**
	 * Instantiates a new pe provisioner simple.
	 * 
	 * @param availableMips The total mips capacity of the PE that the provisioner can allocate to VMs. 
	 * 
	 * @pre $none
	 * @post $none
	 */
	public PeProvisionerSimple(double availableMips) {
		super(availableMips);
		setPeTable(new HashMap<String, ArrayList<Double>>());
	}

	@Override
	public boolean allocateMipsForGuest(GuestEntity guest, double mips) {
		return allocateMipsForGuest(guest.getUid(), mips);
	}

	@Override
	public boolean allocateMipsForGuest(String vmUid, double mips) {
		if (getAvailableMips() < mips) {
			return false;
		}

        List<Double> allocatedMips = getPeTable().computeIfAbsent(vmUid, k -> new ArrayList<>());

        allocatedMips.add(mips);
		setAvailableMips(getAvailableMips() - mips);

		return true;
	}

	@Override
	public boolean allocateMipsForGuest(GuestEntity guest, List<Double> mips) {
		deallocateMipsForGuest(guest);
		for (double _mips : mips) {
			if (!allocateMipsForGuest(guest.getUid(), _mips)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void deallocateMipsForAllGuests() {
		super.deallocateMipsForAllGuests();
		getPeTable().clear();
	}

	@Override
	public double getAllocatedMipsForGuestByVirtualPeId(GuestEntity guest, int peId) {
		List<Double> allocatedMips = getAllocatedMipsForGuest(guest);
		if (allocatedMips != null && peId < allocatedMips.size()) {
			return allocatedMips.get(peId);
		}
		return 0;
	}

	@Override
	public List<Double> getAllocatedMipsForGuest(GuestEntity guest) {
		if (getPeTable().containsKey(guest.getUid())) {
			return getPeTable().get(guest.getUid());
		}
		return null;
	}

	@Override
	public double getTotalAllocatedMipsForGuest(GuestEntity guest) {
		List<Double> allocatedMips = getAllocatedMipsForGuest(guest);

		if (allocatedMips != null) {
			double totalAllocatedMips = 0.0;
			for (double mips : allocatedMips) {
				totalAllocatedMips += mips;
			}
			return totalAllocatedMips;
		}
		return 0;
	}

	@Override
	public void deallocateMipsForGuest(GuestEntity guest) {
		List<Double> allocatedMips = getAllocatedMipsForGuest(guest);

		if (allocatedMips != null) {
			for (double mips : allocatedMips) {
				setAvailableMips(getAvailableMips() + mips);
			}
			getPeTable().remove(guest.getUid());
		}
	}

	/**
	 * Gets the pe map.
	 * 
	 * @return the pe map
	 */
	protected Map<String, List<Double>> getPeTable() {
		return peTable;
	}

	/**
	 * Sets the pe map.
	 * 
	 * @param peTable the peTable to set
	 */
	@SuppressWarnings("unchecked")
	protected void setPeTable(Map<String, ? extends List<Double>> peTable) {
		this.peTable = (Map<String, List<Double>>) peTable;
	}
}
